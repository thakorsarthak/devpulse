package com.devpulse.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Registration request DTO.
 *
 * WHY DTOs INSTEAD OF ENTITY DIRECTLY:
 * Never expose your entity to the API layer.
 * Entity has sensitive fields (password hash, internal IDs).
 * DTO is a clean contract between client and server.
 * You can change your entity without breaking your API.
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

}
