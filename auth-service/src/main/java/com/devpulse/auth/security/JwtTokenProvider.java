package com.devpulse.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT Token Provider
 *
 * Handles creation and validation of JWT tokens.
 *
 * HOW JWT WORKS (explain this in interviews):
 * A JWT has 3 parts: Header.Payload.Signature
 * Header: algorithm used (HS256)
 * Payload: claims — userId, email, role, expiry
 * Signature: HMAC of header+payload using our secret key
 *
 * When a request comes in:
 * 1. We recompute the signature using our secret
 * 2. If it matches the token's signature → token is valid
 * 3. We never need to hit the database to validate a token
 *    This is WHY JWT is stateless and scalable
 *
 * SECURITY NOTE:
 * Secret must be at least 256 bits for HS256.
 * We store it in .env, never in code.
 */

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /*
     * Generate jwt token for authenticated user
     * ONLY called after successful login or OAuth
     */

    public String generateToken(Long userId, String email, String role){

        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }


    /**
     * Extract user ID from token.
     * Subject field holds userId as string.
     */
    public Long getUserIdFromToken(String token){
        String subject = parseClaims(token).getSubject();
        return Long.parseLong(subject);
    }

    /**
     * Extract email from token claims.
     */
    public String getEmailFromToken(String token){
        return parseClaims(token).get("email" , String.class);
    }

    /**
     * Extract role from token claims.
     */
    public String getRoleFromToken(String token){
        return parseClaims(token).get("role" , String.class);
    }

    /**
     * Validate token signature and expiry.
     * Returns false for expired, malformed, or tampered tokens.
     */
    public boolean validateToken(String token){
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    . build()
                    .parseSignedClaims(token);
            return true;
        }
        catch (ExpiredJwtException e){
            log.warn("JWT token expired: {}",e.getMessage());
        }catch (MalformedJwtException e){
            log.warn("JWT token malformed: {}",e.getMessage());
        } catch (SecurityException e){
            log.warn("JWT signature invalid: {}",e.getMessage());
        }catch (IllegalArgumentException e) {
            log.warn("JWT token empty or null: {}", e.getMessage());
        }
        return false;
    }

    private Claims parseClaims(String token){
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey(){
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
