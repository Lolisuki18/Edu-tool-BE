package com.edutool.repository;

import com.edutool.model.CommitReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommitReportRepository extends JpaRepository<CommitReport, Integer> {
    List<CommitReport> findByProject_ProjectIdOrderByCreatedAtDesc(Integer projectId);
}
