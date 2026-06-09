package com.devpulse.auth.controller;


import com.devpulse.auth.dto.request.LoginRequest;
import com.devpulse.auth.dto.request.RegisterRequest;
import com.devpulse.auth.dto.response.AuthResponse;
import com.devpulse.auth.dto.response.TokenValidationResponse;
import com.devpulse.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

}
