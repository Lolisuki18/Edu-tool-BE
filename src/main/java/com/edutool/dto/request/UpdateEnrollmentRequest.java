package com.edutool.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEnrollmentRequest {
    
    private Integer projectId; // Có thể null nếu muốn bỏ project
    
    private String roleInProject; // leader, member, etc.
    
    private Integer groupNumber;
}
