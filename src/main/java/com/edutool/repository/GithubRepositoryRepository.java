package com.edutool.repository;

import com.edutool.model.GithubRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GithubRepositoryRepository extends JpaRepository<GithubRepository, Integer> {

    List<GithubRepository> findByProject_ProjectId(Integer projectId);

    Optional<GithubRepository> findFirstByProject_ProjectIdAndIsSelectedTrue(Integer projectId);

    boolean existsByRepoUrlAndProject_ProjectId(String repoUrl, Integer projectId);

    @Query("SELECT r FROM GithubRepository r WHERE r.project.course.courseId = :courseId ORDER BY r.createdAt DESC")
    List<GithubRepository> findByCourseId(@Param("courseId") Integer courseId);
}
