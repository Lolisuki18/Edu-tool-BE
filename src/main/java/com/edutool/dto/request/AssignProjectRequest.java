package com.edutool.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignProjectRequest {
    
    @NotNull(message = "Student ID is required")
    private Integer studentId;
    
    @NotNull(message = "Project ID is required")
    private Integer projectId;
    
    private String roleInProject; // Optional: leader, member, etc.
    
    private Integer groupNumber; // Optional: group number
}
