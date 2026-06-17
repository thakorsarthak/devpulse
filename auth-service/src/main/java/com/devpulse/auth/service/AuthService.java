package com.devpulse.auth.service;

import com.devpulse.auth.dto.request.LoginRequest;
import com.devpulse.auth.dto.request.RegisterRequest;
import com.devpulse.auth.dto.response.AuthResponse;
import com.devpulse.auth.dto.response.TokenValidationResponse;
import com.devpulse.auth.entity.AuthProvider;
import com.devpulse.auth.entity.User;
import com.devpulse.auth.repository.UserRepository;
import com.devpulse.auth.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Auth service - core business logic.
 *
 * WHY @Transactional on register:
 * If anything fails after saving the user (e.g. token generation throws), the entire operation rolls back.
 * No orphaned users in the database without a valid token being issued.
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public AuthResponse register(RegisterRequest request){

        // checking duplicate email
        if(userRepository.existsByEmail(request.getEmail())){

            throw  new IllegalArgumentException("Email already registered: " + request.getEmail());
        }

        // building and saving user
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("ROLE_USER")
                .provider(AuthProvider.LOCAL)
                .enabled(true)
                .emailVerified(false)
                .build();

        User savedUser = userRepository.save(user);
        log.info("Registered new user: {}" , savedUser.getEmail());


        String token = tokenProvider.generateToken(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getRole()
        );

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .name(savedUser.getName())
                .role(savedUser.getRole())
                .build();
    }

    public AuthResponse login(LoginRequest request){

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid Email or password"));

        //verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new IllegalArgumentException("Invalid email or password");
        }

        // check account enabled
        if(!user.getEnabled()){
            throw new IllegalArgumentException("Account is disabled");
        }

        log.info("User logged in: {}", user.getEmail());

        String token = tokenProvider.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole());

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .build();
    }

    public TokenValidationResponse validateToken(String token) {
        if (!tokenProvider.validateToken(token)) {
            return TokenValidationResponse.builder()
                    .valid(false)
                    .build();
        }

        return TokenValidationResponse.builder()
                .valid(true)
                .userId(tokenProvider.getUserIdFromToken(token))
                .email(tokenProvider.getEmailFromToken(token))
                .role(tokenProvider.getRoleFromToken(token))
                .build();
    }

}
