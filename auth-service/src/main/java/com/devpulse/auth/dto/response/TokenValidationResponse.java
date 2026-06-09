package com.devpulse.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response for token validation endpoint.
 * Other services call /auth/validate to verify a JWT.
 *
 * WHY A DEDICATED VALIDATE ENDPOINT:
 * Other services (ingestion, analytics) receive requests
 * with JWT tokens. Instead of each service having JWT
 * validation logic, they call this endpoint.
 * Auth logic stays in one place.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenValidationResponse {

    private boolean valid;
    private Long userId;
    private String email;
    private String role;

}
