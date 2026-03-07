package com.edutool.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents one nhóm (group) in a course for the "Quản lý Repository" page.
 *
 * Relationship: 1 Project = 1 Group.
 * Each group has zero or more submitted GitHub repositories and a list of members.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupRepositoryResponse {

    /** Display number used to label the group (from CourseEnrollment.groupNumber) */
    private Integer groupNumber;

    /** The underlying project that represents this group */
    private Integer projectId;
    private String projectCode;
    private String projectName;
    private String projectDescription;
    private String projectTechnologies;

    /** Course this group belongs to */
    private Integer courseId;
    private String courseCode;
    private String courseName;

    /** Number of members in this group */
    private Integer memberCount;

    /** Number of repositories submitted by this group */
    private Integer repoCount;

    /** Students enrolled in this group/project */
    private List<StudentSummaryResponse> members;

    /** All GitHub repositories submitted for this group/project */
    private List<GithubRepositoryResponse> repositories;
}
