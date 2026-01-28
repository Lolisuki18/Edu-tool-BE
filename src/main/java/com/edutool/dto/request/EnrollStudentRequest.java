package com.edutool.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnrollStudentRequest {
    
    @NotNull(message = "Student ID is required")
    private Integer studentId;
    
    @NotNull(message = "Course ID is required")
    private Integer courseId;
}
