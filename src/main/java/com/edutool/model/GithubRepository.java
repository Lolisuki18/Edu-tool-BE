package com.edutool.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "github_repositories")
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

