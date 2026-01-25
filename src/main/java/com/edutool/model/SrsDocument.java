package com.edutool.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "srs_documents", indexes = {
    @Index(name = "idx_srs_jira_project_id", columnList = "jira_project_id"),
    @Index(name = "idx_srs_version", columnList = "version")
})
@Getter
@Setter
@NoArgsConstructor
public class SrsDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer srsId;

    @ManyToOne
    @JoinColumn(name = "jira_project_id")
    private JiraProject jiraProject;

    private String version;
    private String documentLink;

    @CreationTimestamp
    private LocalDateTime createdAt;
}

