package com.edutool.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateLecturerRequest {

    @NotBlank(message = "Staff code is required")
    @Size(min = 3, max = 50, message = "Staff code must be between 3 and 50 characters")
    private String staffCode;

    @NotBlank(message = "User ID is required")
    private Long userId;
}
