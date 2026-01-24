package com.edutool.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRequest {
    
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Size(max = 255, message = "Full name must not exceed 255 characters")
    private String fullName;

    @Pattern(regexp = "ADMIN|LECTURER|STUDENT", message = "Role must be ADMIN, LECTURER, or STUDENT")
    private String role;

    @Pattern(regexp = "ACTIVE|INACTIVE|SUSPENDED", message = "Status must be ACTIVE, INACTIVE, or SUSPENDED")
    private String status;

    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password; // Optional: for password update
}
