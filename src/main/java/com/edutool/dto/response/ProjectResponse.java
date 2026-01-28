package com.edutool.dto.response;

import com.edutool.model.Project;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {
    
    private Integer projectId;
    private String projectCode;
    private String projectName;
    private Integer courseId;
    private String courseCode;
    private String courseName;
    private String description;
    private String technologies;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;
    private Long memberCount; // Số lượng sinh viên trong project
    
    public static ProjectResponse fromEntity(Project project) {
        ProjectResponse response = new ProjectResponse();
        response.setProjectId(project.getProjectId());
        response.setProjectCode(project.getProjectCode());
        response.setProjectName(project.getProjectName());
        response.setDescription(project.getDescription());
        response.setTechnologies(project.getTechnologies());
        response.setCreatedAt(project.getCreatedAt());
        response.setDeletedAt(project.getDeletedAt());
        
        if (project.getCourse() != null) {
            response.setCourseId(project.getCourse().getCourseId());
            response.setCourseCode(project.getCourse().getCourseCode());
            response.setCourseName(project.getCourse().getCourseName());
        }
        
        return response;
    }
    
    public static ProjectResponse fromEntityWithMemberCount(Project project, Long memberCount) {
        ProjectResponse response = fromEntity(project);
        response.setMemberCount(memberCount);
        return response;
    }
}
