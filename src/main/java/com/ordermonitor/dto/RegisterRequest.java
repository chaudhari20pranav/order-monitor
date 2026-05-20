package com.ordermonitor.dto;

import jakarta.validation.constraints.*;

/**
 * DTO for user registration.
 */
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

    public RegisterRequest() {}

    public RegisterRequest(String fullName, String email, String password, String role) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public String getFullName()          { return fullName; }
    public void setFullName(String v)    { this.fullName = v; }

    public String getEmail()             { return email; }
    public void setEmail(String v)       { this.email = v; }

    public String getPassword()          { return password; }
    public void setPassword(String v)    { this.password = v; }

    public String getRole()              { return role; }
    public void setRole(String v)        { this.role = v; }
}