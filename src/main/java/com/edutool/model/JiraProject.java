package com.edutool.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "jira_projects")
@Getter
@Setter
@NoArgsConstructor
public class JiraProject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer jiraProjectId;

    private String jiraKey;
    private String projectName;
    private String jiraUrl;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    @OneToMany(mappedBy = "jiraProject")
    private List<SrsDocument> srsDocuments;
}

