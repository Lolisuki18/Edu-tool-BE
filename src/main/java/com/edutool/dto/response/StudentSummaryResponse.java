package com.edutool.dto.response;

import com.edutool.model.CourseEnrollment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentSummaryResponse {

    private Integer studentId;
    private String studentCode;
    private String fullName;
    private String githubUsername;
    private String email;
    private String roleInProject;

    public static StudentSummaryResponse fromEnrollment(CourseEnrollment enrollment) {
        StudentSummaryResponse dto = new StudentSummaryResponse();
        if (enrollment.getStudent() != null) {
            dto.setStudentId(enrollment.getStudent().getStudentId());
            dto.setStudentCode(enrollment.getStudent().getStudentCode());
            dto.setGithubUsername(enrollment.getStudent().getGithubUsername());
            if (enrollment.getStudent().getUser() != null) {
                dto.setFullName(enrollment.getStudent().getUser().getFullName());
                dto.setEmail(enrollment.getStudent().getUser().getEmail());
            }
        }
        dto.setRoleInProject(enrollment.getRoleInProject());
        return dto;
    }
}
