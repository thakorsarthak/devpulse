package com.devpulse.auth.controller;


import com.devpulse.auth.dto.request.LoginRequest;
import com.devpulse.auth.dto.request.RegisterRequest;
import com.devpulse.auth.dto.response.AuthResponse;
import com.devpulse.auth.dto.response.TokenValidationResponse;
import com.devpulse.auth.entity.ServiceRegistration;
import com.devpulse.auth.entity.User;
import com.devpulse.auth.repository.ServiceRegistrationRepository;
import com.devpulse.auth.repository.UserRepository;
import com.devpulse.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Auth controller - exposes authentication endpoints.
 *
 * Base path: /auth (gateway strips /api/auth, so service sees /auth)
 *
 * Endpoints:
 * POST /auth/register  → create new account
 * POST /auth/login     → authenticate, get JWT
 * GET  /auth/validate  → validate JWT (called by other services)
 * GET  /auth/me        → get current user info from token
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Auth endpoints for DevPulse")
public class AuthController {

    private final AuthService authService;

    private final UserRepository userRepository;

    private final ServiceRegistrationRepository serviceRegistrationRepository;


    @Value("${notification.default.email:thakorsarthak2912@gmail.com}")
    private String defaultAlertEmail;

    @Value("${internal.service.secret}")
    private String internalServiceSecret;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register new user")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request){

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Login with and password")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request){

        return  ResponseEntity.ok(authService.login(request));
    }


    @GetMapping("/validate")
    @Operation(summary = "Validate JWT token - called by other services")
    public ResponseEntity<TokenValidationResponse> validate(@RequestHeader("Authorization") String authHeader){


        String token = authHeader.startsWith("Bearer ")
                ? authHeader.substring(7)
                : authHeader;

        return ResponseEntity.ok(authService.validateToken(token));
    }


    @GetMapping("/me")
    @Operation(summary = "Get current user info from token")
    public ResponseEntity<TokenValidationResponse> me(
            @RequestHeader ("Authorization") String authHeader){

        String token = authHeader.startsWith("Bearer ")
                ? authHeader.substring(7)
                : authHeader;

        return ResponseEntity.ok(authService.validateToken(token));
    }



    @GetMapping("/service-owner")
    @Operation(summary = "Get owner email for a registered service")
    public ResponseEntity<Map<String, String>> getServiceOwner(
            @RequestParam String serviceName ,
            @RequestHeader(value = "X-Internal-Secret", required = false) String secret) {

        if (!internalServiceSecret.equals(secret)) {
            return ResponseEntity.status(403)
                    .body(Map.of("error", "Forbidden - internal endpoint"));
        }

        return serviceRegistrationRepository
                .findByServiceName(serviceName)
                .map(reg -> ResponseEntity.ok(
                        Map.of(
                                "email", reg.getUser().getEmail(),
                                "name", reg.getUser().getName(),
                                "serviceName", serviceName
                        )
                ))
                .orElse(ResponseEntity.ok(
                        Map.of("email", defaultAlertEmail)
                ));
    }


    @PostMapping("/register-service")
    @Operation(summary = "Register a service under your account")
    public ResponseEntity<Map<String, Object>> registerService(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String serviceName,
            @RequestParam(defaultValue = "production") String environment) {

        String token = authHeader.startsWith("Bearer ")
                ? authHeader.substring(7) : authHeader;

        TokenValidationResponse validation =
                authService.validateToken(token);

        if (!validation.isValid()) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Invalid token"));
        }

        User user = userRepository.findById(validation.getUserId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found"));

        ServiceRegistration registration = ServiceRegistration.builder()
                .user(user)
                .serviceName(serviceName)
                .environment(environment)
                .build();

        serviceRegistrationRepository.save(registration);

        return ResponseEntity.ok(Map.of(
                "message", "Service registered successfully",
                "serviceName", serviceName,
                "owner", user.getEmail()
        ));
    }

}
