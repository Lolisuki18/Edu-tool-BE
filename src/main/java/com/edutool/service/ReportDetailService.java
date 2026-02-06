package com.edutool.service;

import com.edutool.dto.request.ReportDetailRequest;
import com.edutool.dto.response.ReportDetailResponse;
import com.edutool.exception.ResourceNotFoundException;
import com.edutool.exception.ValidationException;
import com.edutool.model.PeriodicReport;
import com.edutool.model.Project;
import com.edutool.model.ReportDetail;
import com.edutool.model.Student;
import com.edutool.repository.PeriodicReportRepository;
import com.edutool.repository.ProjectRepository;
import com.edutool.repository.ReportDetailRepository;
import com.edutool.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReportDetailService {

    private final ReportDetailRepository reportDetailRepository;
    private final PeriodicReportRepository periodicReportRepository;
    private final ProjectRepository projectRepository;
    private final StudentRepository studentRepository;

    /**
     * Helper: Kiểm tra quyền truy cập cho student
     */
    private boolean isAdminOrLecturer() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")
                        || auth.getAuthority().equals("ROLE_LECTURER"));
    }

    /**
     * Helper: Lấy username hiện tại
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : null;
    }

    /**
     * Tạo report detail mới
     */
    @Transactional
    public ReportDetailResponse createReportDetail(ReportDetailRequest request) {
        // Validate periodic report tồn tại
        PeriodicReport periodicReport = periodicReportRepository.findById(request.getPeriodicReportId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Periodic report not found with ID: " + request.getPeriodicReportId()));

        // Validate project tồn tại
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Project not found with ID: " + request.getProjectId()));

        // Validate student tồn tại
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Student not found with ID: " + request.getStudentId()));

        // Security: Student chỉ có thể tạo report cho chính mình
        if (!isAdminOrLecturer()) {
            String currentUsername = getCurrentUsername();
            if (student.getUser() != null && !student.getUser().getEmail().equals(currentUsername)) {
                throw new AccessDeniedException("You can only create report details for yourself");
            }
        }

        // Kiểm tra xem student đã submit report detail cho periodic report này chưa
        if (reportDetailRepository.existsByPeriodicReport_ReportIdAndStudent_StudentId(
                request.getPeriodicReportId(), request.getStudentId())) {
            throw new ValidationException("Student has already submitted a report detail for this periodic report");
        }

        // Tạo report detail
        ReportDetail reportDetail = new ReportDetail();
        reportDetail.setPeriodicReport(periodicReport);
        reportDetail.setProject(project);
        reportDetail.setStudent(student);
        reportDetail.setTitle(request.getTitle());
        reportDetail.setContent(request.getContent());
        reportDetail.setAttachmentUrl(request.getAttachmentUrl());
        reportDetail.setScore(request.getScore());
        reportDetail.setFeedback(request.getFeedback());
        reportDetail.setStatus(request.getStatus() != null ? request.getStatus() : "DRAFT");

        if ("SUBMITTED".equals(reportDetail.getStatus()) || "LATE".equals(reportDetail.getStatus())) {
            reportDetail.setSubmittedAt(LocalDateTime.now());
        }

        ReportDetail savedReportDetail = reportDetailRepository.save(reportDetail);

        return ReportDetailResponse.fromEntity(savedReportDetail);
    }

    /**
     * Lấy report detail theo ID
     */
    public ReportDetailResponse getReportDetailById(Integer reportDetailId) {
        ReportDetail reportDetail = reportDetailRepository.findById(reportDetailId)
                .orElseThrow(() -> new ResourceNotFoundException("Report detail not found with ID: " + reportDetailId));

        return ReportDetailResponse.fromEntity(reportDetail);
    }

    /**
     * Lấy tất cả report details (phân trang)
     */
    public Page<ReportDetailResponse> getAllReportDetails(Pageable pageable) {
        Page<ReportDetail> reportDetails = reportDetailRepository.findAll(pageable);

        return reportDetails.map(ReportDetailResponse::fromEntity);
    }

    /**
     * Lấy report details theo periodic report ID
     */
    public Page<ReportDetailResponse> getReportDetailsByPeriodicReport(Integer reportId, Pageable pageable) {
        if (!periodicReportRepository.existsById(reportId)) {
            throw new ResourceNotFoundException("Periodic report not found with ID: " + reportId);
        }

        Page<ReportDetail> reportDetails = reportDetailRepository.findByPeriodicReport_ReportId(reportId, pageable);

        return reportDetails.map(ReportDetailResponse::fromEntity);
    }

    /**
     * Lấy report details theo student ID
     */
    public Page<ReportDetailResponse> getReportDetailsByStudent(Integer studentId, Pageable pageable) {
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student not found with ID: " + studentId);
        }

        Page<ReportDetail> reportDetails = reportDetailRepository.findByStudent_StudentId(studentId, pageable);

        return reportDetails.map(ReportDetailResponse::fromEntity);
    }

    /**
     * Lấy report details theo project ID
     */
    public Page<ReportDetailResponse> getReportDetailsByProject(Integer projectId, Pageable pageable) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found with ID: " + projectId);
        }

        Page<ReportDetail> reportDetails = reportDetailRepository.findByProject_ProjectId(projectId, pageable);

        return reportDetails.map(ReportDetailResponse::fromEntity);
    }

    /**
     * Lấy report details theo status
     */
    public Page<ReportDetailResponse> getReportDetailsByStatus(String status, Pageable pageable) {
        Page<ReportDetail> reportDetails = reportDetailRepository.findByStatus(status.toUpperCase(), pageable);

        return reportDetails.map(ReportDetailResponse::fromEntity);
    }

    /**
     * Update report detail
     */
    @Transactional
    public ReportDetailResponse updateReportDetail(Integer reportDetailId, ReportDetailRequest request) {
        ReportDetail reportDetail = reportDetailRepository.findById(reportDetailId)
                .orElseThrow(() -> new ResourceNotFoundException("Report detail not found with ID: " + reportDetailId));

        // Security: Student chỉ có thể update report của chính mình
        if (!isAdminOrLecturer()) {
            String currentUsername = getCurrentUsername();
            if (reportDetail.getStudent() != null
                    && reportDetail.getStudent().getUser() != null
                    && !reportDetail.getStudent().getUser().getEmail().equals(currentUsername)) {
                throw new AccessDeniedException("You can only update your own report details");
            }
        }

        // Check duplicate nếu thay đổi student hoặc periodic report
        if (!reportDetail.getStudent().getStudentId().equals(request.getStudentId())
                || !reportDetail.getPeriodicReport().getReportId().equals(request.getPeriodicReportId())) {

            if (reportDetailRepository.existsByPeriodicReport_ReportIdAndStudent_StudentId(
                    request.getPeriodicReportId(), request.getStudentId())) {
                throw new ValidationException("Student has already submitted a report detail for this periodic report");
            }
        }

        // Validate periodic report nếu thay đổi
        if (!reportDetail.getPeriodicReport().getReportId().equals(request.getPeriodicReportId())) {
            PeriodicReport periodicReport = periodicReportRepository.findById(request.getPeriodicReportId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Periodic report not found with ID: " + request.getPeriodicReportId()));
            reportDetail.setPeriodicReport(periodicReport);
        }

        // Validate project nếu thay đổi
        if (!reportDetail.getProject().getProjectId().equals(request.getProjectId())) {
            Project project = projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Project not found with ID: " + request.getProjectId()));
            reportDetail.setProject(project);
        }

        // Validate student nếu thay đổi
        if (!reportDetail.getStudent().getStudentId().equals(request.getStudentId())) {
            Student student = studentRepository.findById(request.getStudentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Student not found with ID: " + request.getStudentId()));
            reportDetail.setStudent(student);
        }

        // Update các field khác
        reportDetail.setTitle(request.getTitle());
        reportDetail.setContent(request.getContent());
        reportDetail.setAttachmentUrl(request.getAttachmentUrl());
        reportDetail.setScore(request.getScore());
        reportDetail.setFeedback(request.getFeedback());

        // Update status và submittedAt
        String oldStatus = reportDetail.getStatus();
        reportDetail.setStatus(request.getStatus() != null ? request.getStatus() : "DRAFT");

        if (("SUBMITTED".equals(reportDetail.getStatus()) || "LATE".equals(reportDetail.getStatus()))
                && !"SUBMITTED".equals(oldStatus) && !"LATE".equals(oldStatus)) {
            reportDetail.setSubmittedAt(LocalDateTime.now());
        }

        ReportDetail updatedReportDetail = reportDetailRepository.save(reportDetail);

        return ReportDetailResponse.fromEntity(updatedReportDetail);
    }

    /**
     * Delete report detail
     */
    @Transactional
    public void deleteReportDetail(Integer reportDetailId) {
        if (!reportDetailRepository.existsById(reportDetailId)) {
            throw new ResourceNotFoundException("Report detail not found with ID: " + reportDetailId);
        }

        reportDetailRepository.deleteById(reportDetailId);
    }
}
