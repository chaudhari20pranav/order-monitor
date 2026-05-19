package com.ordermonitor.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * DTO for user registration.
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "A valid email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Role is required")
    @Pattern(regexp = "ADMIN|SUBSCRIBER", message = "Role must be ADMIN or SUBSCRIBER")
    private String role;
}
