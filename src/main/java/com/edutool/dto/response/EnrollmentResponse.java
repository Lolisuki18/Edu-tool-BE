package com.edutool.dto.response;

import com.edutool.model.CourseEnrollment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentResponse {
    
    private Integer enrollmentId;
    private Integer studentId;
    private String studentCode;
    private String studentName;
    private Integer courseId;
    private String courseCode;
    private String courseName;
    private Integer projectId;
    private String projectCode;
    private String projectName;
    private String roleInProject;
    private Integer groupNumber;
    private LocalDateTime enrolledAt;
    private LocalDateTime deletedAt;
    private LocalDateTime removedFromProjectAt;
    
    public static EnrollmentResponse fromEntity(CourseEnrollment enrollment) {
        EnrollmentResponse response = new EnrollmentResponse();
        response.setEnrollmentId(enrollment.getEnrollmentId());
        
        if (enrollment.getStudent() != null) {
            response.setStudentId(enrollment.getStudent().getStudentId());
            response.setStudentCode(enrollment.getStudent().getStudentCode());
            if (enrollment.getStudent().getUser() != null) {
                response.setStudentName(enrollment.getStudent().getUser().getFullName());
            }
        }
        
        if (enrollment.getCourse() != null) {
            response.setCourseId(enrollment.getCourse().getCourseId());
            response.setCourseCode(enrollment.getCourse().getCourseCode());
            response.setCourseName(enrollment.getCourse().getCourseName());
        }
        
        if (enrollment.getProject() != null) {
            response.setProjectId(enrollment.getProject().getProjectId());
            response.setProjectCode(enrollment.getProject().getProjectCode());
            response.setProjectName(enrollment.getProject().getProjectName());
        }
        
        response.setRoleInProject(enrollment.getRoleInProject());
        response.setGroupNumber(enrollment.getGroupNumber());
        response.setEnrolledAt(enrollment.getEnrolledAt());
        response.setDeletedAt(enrollment.getDeletedAt());
        response.setRemovedFromProjectAt(enrollment.getRemovedFromProjectAt());
        
        return response;
    }
}
