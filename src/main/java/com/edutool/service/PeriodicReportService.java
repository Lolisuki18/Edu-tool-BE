package com.edutool.service;

import com.edutool.dto.request.PeriodicReportRequest;
import com.edutool.dto.response.PeriodicReportResponse;
import com.edutool.exception.ResourceNotFoundException;
import com.edutool.exception.ValidationException;
import com.edutool.model.Course;
import com.edutool.model.PeriodicReport;
import com.edutool.model.ReportStatus;
import com.edutool.repository.CourseRepository;
import com.edutool.repository.PeriodicReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PeriodicReportService {
    
    private final PeriodicReportRepository periodicReportRepository;
    private final CourseRepository courseRepository;
    
    /**
     * Tạo periodic report mới
     */
    @Transactional
    public PeriodicReportResponse createPeriodicReport(PeriodicReportRequest request) {
        // Validate dates
        validateDates(request);
        
        // Kiểm tra course tồn tại
        Course course = courseRepository.findById(request.getCourseId())
            .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + request.getCourseId()));
        
        // Kiểm tra overlap với các report khác
        List<PeriodicReport> overlappingReports = periodicReportRepository.findOverlappingReports(
            request.getCourseId(),
            request.getReportFromDate(),
            request.getReportToDate(),
            request.getSubmitStartAt(),
            request.getSubmitEndAt()
        );
        
        if (!overlappingReports.isEmpty()) {
            throw new ValidationException("Report dates overlap with existing periodic report(s)");
        }
        
        // Tạo periodic report
        PeriodicReport periodicReport = new PeriodicReport();
        periodicReport.setCourse(course);
        periodicReport.setReportFromDate(request.getReportFromDate());
        periodicReport.setReportToDate(request.getReportToDate());
        periodicReport.setSubmitStartAt(request.getSubmitStartAt());
        periodicReport.setSubmitEndAt(request.getSubmitEndAt());
        periodicReport.setDescription(request.getDescription());
        periodicReport.setStatus(ReportStatus.ACTIVE);
        
        PeriodicReport savedReport = periodicReportRepository.save(periodicReport);
        
        return PeriodicReportResponse.fromEntity(savedReport);
    }
    
    /**
     * Lấy thông tin periodic report theo ID
     */
    public PeriodicReportResponse getPeriodicReportById(Integer reportId) {
        PeriodicReport periodicReport = periodicReportRepository.findActiveById(reportId)
            .orElseThrow(() -> new ResourceNotFoundException("Periodic report not found with ID: " + reportId));
        
        return PeriodicReportResponse.fromEntity(periodicReport);
    }
    
    /**
     * Lấy tất cả periodic report
     */
    public Page<PeriodicReportResponse> getAllPeriodicReports(Pageable pageable) {
        Page<PeriodicReport> reports = periodicReportRepository.findAllActive(pageable);
        
        return reports.map(PeriodicReportResponse::fromEntity);
    }
    
    /**
     * Lấy tất cả periodic report của course
     */
    public Page<PeriodicReportResponse> getPeriodicReportsByCourse(Integer courseId, Pageable pageable) {
        // Kiểm tra course tồn tại
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found with ID: " + courseId);
        }
        
        Page<PeriodicReport> reports = periodicReportRepository.findByCourse_CourseId(courseId, pageable);
        
        return reports.map(PeriodicReportResponse::fromEntity);
    }
    
    /**
     * Lấy periodic report theo khoảng thời gian
     */
    public Page<PeriodicReportResponse> getPeriodicReportsByDateRange(
            Integer courseId, 
            LocalDateTime fromDate, 
            LocalDateTime toDate,
            Pageable pageable) {
        
        // Kiểm tra course tồn tại
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found with ID: " + courseId);
        }
        
        if (fromDate.isAfter(toDate)) {
            throw new ValidationException("From date must be before to date");
        }
        
        Page<PeriodicReport> reports = periodicReportRepository.findByCourseAndDateRange(
            courseId, fromDate, toDate, pageable);
        
        return reports.map(PeriodicReportResponse::fromEntity);
    }
    
    /**
     * Lấy periodic report đang mở (có thể submit)
     */
    public Page<PeriodicReportResponse> getOpenReportsByCourse(Integer courseId, Pageable pageable) {
        // Kiểm tra course tồn tại
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found with ID: " + courseId);
        }
        
        LocalDateTime now = LocalDateTime.now();
        Page<PeriodicReport> reports = periodicReportRepository.findOpenReportsByCourse(courseId, now, pageable);
        
        return reports.map(PeriodicReportResponse::fromEntity);
    }
    
    /**
     * Cập nhật periodic report
     */
    @Transactional
    public PeriodicReportResponse updatePeriodicReport(Integer reportId, PeriodicReportRequest request) {
        // Validate dates
        validateDates(request);
        
        // Tìm periodic report
        PeriodicReport periodicReport = periodicReportRepository.findActiveById(reportId)
            .orElseThrow(() -> new ResourceNotFoundException("Periodic report not found with ID: " + reportId));
        
        // Kiểm tra course tồn tại
        Course course = courseRepository.findById(request.getCourseId())
            .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + request.getCourseId()));
        
        // Kiểm tra overlap với các report khác (ngoại trừ report hiện tại)
        List<PeriodicReport> overlappingReports = periodicReportRepository.findOverlappingReports(
            request.getCourseId(),
            request.getReportFromDate(),
            request.getReportToDate(),
            request.getSubmitStartAt(),
            request.getSubmitEndAt()
        );
        
        // Loại bỏ report hiện tại khỏi danh sách overlap
        overlappingReports = overlappingReports.stream()
            .filter(report -> !report.getReportId().equals(reportId))
            .collect(Collectors.toList());
        
        if (!overlappingReports.isEmpty()) {
            throw new ValidationException("Report dates overlap with existing periodic report(s)");
        }
        
        // Cập nhật thông tin
        periodicReport.setCourse(course);
        periodicReport.setReportFromDate(request.getReportFromDate());
        periodicReport.setReportToDate(request.getReportToDate());
        periodicReport.setSubmitStartAt(request.getSubmitStartAt());
        periodicReport.setSubmitEndAt(request.getSubmitEndAt());
        periodicReport.setDescription(request.getDescription());
        
        PeriodicReport updatedReport = periodicReportRepository.save(periodicReport);
        
        return PeriodicReportResponse.fromEntity(updatedReport);
    }
    
    /**
     * Xóa periodic report (chuyển status sang INACTIVE)
     */
    @Transactional
    public void deletePeriodicReport(Integer reportId) {
        PeriodicReport periodicReport = periodicReportRepository.findActiveById(reportId)
            .orElseThrow(() -> new ResourceNotFoundException("Periodic report not found with ID: " + reportId));
        
        // Chuyển status sang INACTIVE
        periodicReport.setStatus(ReportStatus.INACTIVE);
        periodicReportRepository.save(periodicReport);
    }
    
    /**
     * Khôi phục periodic report (chuyển status sang ACTIVE)
     */
    @Transactional
    public PeriodicReportResponse restorePeriodicReport(Integer reportId) {
        PeriodicReport periodicReport = periodicReportRepository.findById(reportId)
            .orElseThrow(() -> new ResourceNotFoundException("Periodic report not found with ID: " + reportId));
        
        if (periodicReport.getStatus() == ReportStatus.ACTIVE) {
            throw new ValidationException("Periodic report is already active");
        }
        
        // Restore - chuyển về ACTIVE
        periodicReport.setStatus(ReportStatus.ACTIVE);
        PeriodicReport restoredReport = periodicReportRepository.save(periodicReport);
        
        return PeriodicReportResponse.fromEntity(restoredReport);
    }
    
    /**
     * Lấy danh sách periodic report đã xóa (INACTIVE)
     */
    public Page<PeriodicReportResponse> getInactivePeriodicReports(Pageable pageable) {
        Page<PeriodicReport> inactiveReports = periodicReportRepository.findInactive(pageable);
        
        return inactiveReports.map(PeriodicReportResponse::fromEntity);
    }
    
    /**
     * Validate các ngày tháng
     */
    private void validateDates(PeriodicReportRequest request) {
        // Kiểm tra report from date < report to date
        if (request.getReportFromDate().isAfter(request.getReportToDate())) {
            throw new ValidationException("Report from date must be before report to date");
        }
        
        // Kiểm tra submit start at < submit end at
        if (request.getSubmitStartAt().isAfter(request.getSubmitEndAt())) {
            throw new ValidationException("Submit start date must be before submit end date");
        }
        
        // Kiểm tra submit dates phải nằm sau report dates
        if (request.getSubmitStartAt().isBefore(request.getReportFromDate())) {
            throw new ValidationException("Submit start date must be after or equal to report from date");
        }
    }
}
