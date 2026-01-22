package com.edutool.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "srs_documents")
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

