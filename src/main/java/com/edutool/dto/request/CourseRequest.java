package com.edutool.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CourseRequest {
    
    @NotBlank(message = "Course code is required")
    @Size(min = 1, max = 50, message = "Course code must be between 1 and 50 characters")
    private String courseCode;
    
    @NotBlank(message = "Course name is required")
    @Size(min = 1, max = 100, message = "Course name must be between 1 and 100 characters")
    private String courseName;
    
}
