package com.edutool.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "commit_details")
@Getter
@Setter
@NoArgsConstructor
public class CommitDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer commitId;

    @ManyToOne
    @JoinColumn(name = "contribution_id")
    private CommitContribution contribution;

    private String commitHash;

    @Column(columnDefinition = "TEXT")
    private String commitMessage;

    private Integer filesChanged;
    private Integer additions;
    private Integer deletions;
    private LocalDateTime committedAt;
    private String commitUrl;
}

