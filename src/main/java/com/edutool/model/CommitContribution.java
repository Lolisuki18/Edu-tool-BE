package com.edutool.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "commit_contributions", indexes = {
    @Index(name = "idx_contribution_student_id", columnList = "student_id"),
    @Index(name = "idx_contribution_repo_id", columnList = "repo_id"),
    @Index(name = "idx_commit_date", columnList = "commitDate"),
    @Index(name = "idx_student_repo", columnList = "student_id, repo_id")
})
@Getter
@Setter
@NoArgsConstructor
public class CommitContribution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer contributionId;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "repo_id")
    private GithubRepository repository;

    private String githubAuthor;
    private String authorEmail;
    private Integer weekNumber;
    private Integer year;
    private Integer totalCommits;
    private Integer additions;
    private Integer deletions;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "contribution")
    private List<CommitDetail> commitDetails;
}

