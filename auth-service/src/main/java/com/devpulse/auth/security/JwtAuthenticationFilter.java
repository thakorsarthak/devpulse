package com.devpulse.auth.security;

import ch.qos.logback.core.util.StringUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT Authentication Filter
 *
 * Runs once per request (OncePerRequestFilter).
 * Intercepts every request and checks for a valid JWT.
 *
 * FILTER CHAIN FLOW:
 * Request → JwtAuthenticationFilter → SecurityConfig rules → Controller
 *
 * If token is valid:
 *   → Sets authentication in SecurityContext
 *   → Spring Security treats request as authenticated
 *
 * If token is missing or invalid:
 *   → SecurityContext stays empty
 *   → Spring Security blocks request if endpoint requires auth
 *
 * WHY OncePerRequestFilter:
 * Guarantees this filter runs exactly once per request,
 * even in complex filter chain scenarios with forwards/includes.
 *
 * JWT Filter converts a valid JWT into a Spring Security Authentication object for the current request.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractTokenFromRequest(request);

        if(StringUtils.hasText(token) && tokenProvider.validateToken(token)){

            Long userId = tokenProvider.getUserIdFromToken(token);
            String email = tokenProvider.getEmailFromToken(token);
            String role = tokenProvider.getRoleFromToken(token);

            // build authentication object with role as granted authorities
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            List.of(new SimpleGrantedAuthority(role)));
            /** Spring Security understands permissions through GrantedAuthority. So */

            // Store in securityContext - marks this request as authenticated
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("Authenticated user: {} with role: {}", email,role);
        }

        // Always continue filter chain
        // If no token, SecurityContext is empty
        // Spring Security handles the 401 response
        filterChain.doFilter(request,response);
    }

    /**
     * Extract Bearer token from Authorization header.
     * Expected format: "Authorization: Bearer <token>"
     */
    private String extractTokenFromRequest(HttpServletRequest request){
        String bearerToken = request.getHeader("Authorization");
        if(StringUtils.hasText(bearerToken)
            && bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7);
        }
        return null;
    }
}
