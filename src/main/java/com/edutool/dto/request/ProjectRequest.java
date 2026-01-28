package com.edutool.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectRequest {
    
    @NotBlank(message = "Project code is required")
    private String projectCode;
    
    @NotBlank(message = "Project name is required")
    private String projectName;
    
    @NotNull(message = "Course ID is required")
    private Integer courseId;
    
    private String description;
    
    private String technologies;
}
