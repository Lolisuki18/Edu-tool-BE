package com.edutool.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "github_repositories", indexes = {
    @Index(name = "idx_github_project_id", columnList = "project_id"),
    @Index(name = "idx_repo_url", columnList = "repoUrl"),
    @Index(name = "idx_repo_owner", columnList = "owner"),
    @Index(name = "idx_is_selected", columnList = "isSelected")
})
@Getter
@Setter
@NoArgsConstructor
public class GithubRepository {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer repoId;

    private String repoUrl;
    private String repoName;
    private String owner;
    private Boolean isSelected;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "repository")
    private List<CommitContribution> contributions;
}

