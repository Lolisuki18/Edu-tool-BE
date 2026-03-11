package com.edutool.service;

import com.edutool.dto.request.CommitReportRequest;
import com.edutool.dto.response.CommitReportUrlResponse;
import com.edutool.exception.ResourceNotFoundException;
import com.edutool.model.CommitReport;
import com.edutool.model.Project;
import com.edutool.repository.CommitReportRepository;
import com.edutool.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommitReportService {

    private final CommitReportRepository commitReportRepository;
    private final ProjectRepository projectRepository;

    public CommitReportUrlResponse saveReportUrl(CommitReportRequest request) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + request.getProjectId()));

        CommitReport report = new CommitReport();
        report.setProject(project);
        report.setStorageUrl(request.getStorageUrl());
        report.setStorageKey(request.getStorageKey());
        report.setStorageId(request.getStorageId());

        if (request.getSince() != null && !request.getSince().isBlank()) {
            report.setSinceDate(LocalDate.parse(request.getSince()));
        }
        if (request.getUntil() != null && !request.getUntil().isBlank()) {
            report.setUntilDate(LocalDate.parse(request.getUntil()));
        }

        CommitReport saved = commitReportRepository.save(report);
        return CommitReportUrlResponse.fromEntity(saved);
    }

    public List<CommitReportUrlResponse> getReportsByProject(Integer projectId) {
        projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));

        return commitReportRepository
                .findByProject_ProjectIdOrderByCreatedAtDesc(projectId)
                .stream()
                .map(CommitReportUrlResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public void deleteReport(Integer projectId, Integer commitReportId) {
        CommitReport report = commitReportRepository.findById(commitReportId)
                .orElseThrow(() -> new ResourceNotFoundException("Commit report not found: " + commitReportId));
        if (!report.getProject().getProjectId().equals(projectId)) {
            throw new ResourceNotFoundException("Commit report does not belong to project: " + projectId);
        }
        commitReportRepository.delete(report);
    }
}
