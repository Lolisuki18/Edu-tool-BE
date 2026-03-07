package com.edutool.repository;

import com.edutool.model.CommitContribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommitContributionRepository extends JpaRepository<CommitContribution, Integer> {

    List<CommitContribution> findByRepository_RepoId(Integer repoId);

    List<CommitContribution> findByStudent_StudentId(Integer studentId);

    Optional<CommitContribution> findByStudent_StudentIdAndRepository_RepoIdAndWeekNumberAndYear(
            Integer studentId, Integer repoId, Integer weekNumber, Integer year);

    @Query("SELECT c FROM CommitContribution c WHERE c.repository.repoId = :repoId " +
           "ORDER BY c.year, c.weekNumber, c.student.studentCode")
    List<CommitContribution> findByRepoIdOrderByWeek(@Param("repoId") Integer repoId);
}
