package com.edutool.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "jira_projects", indexes = {
    @Index(name = "idx_jira_key", columnList = "jiraKey", unique = true),
    @Index(name = "idx_jira_project_id", columnList = "project_id")
})
@Getter
@Setter
@NoArgsConstructor
public class JiraProject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer jiraProjectId;

    private String jiraKey;
    private String jiraName;
    private String jiraUrl;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "jiraProject")
    private List<SrsDocument> srsDocuments;
}

