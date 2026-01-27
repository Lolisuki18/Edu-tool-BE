package com.edutool.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateStudentRequest {

    @NotBlank(message = "Student code is required")
    @Size(min = 3, max = 50, message = "Student code must be between 3 and 50 characters")
    private String studentCode;

    @NotNull(message = "User ID is required")
    private Long userId;

    @Size(max = 100, message = "GitHub username must not exceed 100 characters")
    private String githubUsername;
}
