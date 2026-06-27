package com.devpulse.starter;


import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;


/**
 * Auto-configured global exception handler.
 *
 * Installed automatically when devpulse-client-starter is on classpath
 * The consuming app's own @RestControllerAdvice takes priority because
 * we set @Order(Integer.MAX_VALUE) — we only catch what slips through
 *
 * WHY @Order(Integer.MAX_VALUE):
 * The consuming app likely has its own @RestControllerAdvice
 * We want THEIRS to run first
 * We act as a catch-all safety net for anything their handler doesn't explicitly handle
 * Lowest priority = runs last = catches the rest
 *
 * WHAT WE DO:
 * 1. Ship the error to DevPulse (async, non-blocking)
 * 2. Return a generic 500 to the client
 * 3. Never interfere with the consuming app's own error handling
 *
 * The starter installs itself at the lowest
 * handler priority so it never conflicts with the consuming
 * application's own exception handling — it only catches
 * what falls through everything else.
 */
@RestControllerAdvice
@Order(Integer.MAX_VALUE)
@RequiredArgsConstructor
public class DevPulseExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(DevPulseExceptionHandler.class);

    private final DevPulseClient devPulseClient;


    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String,String>> handleException(
            Exception ex , HttpServletRequest request){
        log.error("DevPulse caught unhandled exception on {}: {}",
                request.getRequestURI() , ex.getMessage());

        // ship to DevPulse
        devPulseClient.reportError(ex.getClass().getName() + ":" + ex.getMessage(),
                DevPulseClient.extractStackTrace(ex) ,
                "ERROR");

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "error", "Internal Server Error",
                        "path" , request.getRequestURI()
                ));
    }

}
