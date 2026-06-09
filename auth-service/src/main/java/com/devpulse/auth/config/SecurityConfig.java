package com.devpulse.auth.config;

import com.devpulse.auth.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security Configuration
 *
 * WHY STATELESS SESSION:
 * JWT is self-contained no server-side session needed.
 * STATELESS means Spring never creates an HttpSession.
 * This makes auth-service horizontally scalable any instance can validate any token without shared state.
 *
 * WHY CSRF DISABLED:
 * CSRF attacks exploit browser session cookies.
 * We use JWT in Authorization headers, not cookies.
 * Therefore CSRF protection is unnecessary and would break our API clients.
 *
 * WHY BCrypt:
 * BCrypt is adaptive — work factor can increase as hardware gets faster.
 * It also automatically salts passwords, preventing rainbow table attacks.
 */

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{

        http
                // disable CSRF - not needed for statless JWT apis
                .csrf(AbstractHttpConfigurer::disable)

                // stateless session - no httpsession created ever
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/register",
                                "/auth/login",
                                "/auth/validate",
                                "/auth/oauth2/**",
                                "/actuator/health",
                                "/actuator/info",
                                // Swagger UI paths
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/v3/api-docs"
                        ).permitAll()
                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )

                // add the jwt filter before spring's default auth filter
                // this means jwt runs first on every request
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        // Strength 12 = 2^12 bcrypt iterations
        // Balances security vs performance
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception{
        return config.getAuthenticationManager();
    }
}
