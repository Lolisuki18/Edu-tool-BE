package com.edutool.controller;

import com.edutool.dto.request.PeriodicReportRequest;
import com.edutool.dto.response.BaseResponse;
import com.edutool.dto.response.PageResponse;
import com.edutool.dto.response.PeriodicReportResponse;
import com.edutool.service.PeriodicReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/periodic-reports")
@RequiredArgsConstructor
@Tag(name = "Periodic Report", description = "APIs quản lý báo cáo định kỳ")
public class PeriodicReportController {
    
    private final PeriodicReportService periodicReportService;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    @Operation(summary = "Tạo periodic report mới")
    public ResponseEntity<BaseResponse<PeriodicReportResponse>> createPeriodicReport(
            @Valid @RequestBody PeriodicReportRequest request) {
        
        PeriodicReportResponse response = periodicReportService.createPeriodicReport(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(BaseResponse.success("Periodic report created successfully", response));
    }
    
    @GetMapping("/{reportId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER', 'STUDENT')")
    @Operation(summary = "Lấy thông tin periodic report theo ID")
    public ResponseEntity<BaseResponse<PeriodicReportResponse>> getPeriodicReportById(
            @PathVariable Integer reportId) {
        
        PeriodicReportResponse response = periodicReportService.getPeriodicReportById(reportId);
        
        return ResponseEntity.ok(BaseResponse.success("Periodic report retrieved successfully", response));
    }
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER', 'STUDENT')")
    @Operation(summary = "Lấy tất cả periodic report (có phân trang)")
    public ResponseEntity<BaseResponse<PageResponse<PeriodicReportResponse>>> getAllPeriodicReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<PeriodicReportResponse> result = periodicReportService.getAllPeriodicReports(pageable);
        PageResponse<PeriodicReportResponse> response = PageResponse.of(result);
        
        return ResponseEntity.ok(BaseResponse.success("Periodic reports retrieved successfully", response));
    }
    
    @GetMapping("/courses/{courseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER', 'STUDENT')")
    @Operation(summary = "Lấy periodic report của course (optional: filter theo fromDate, toDate, có phân trang)")
    public ResponseEntity<BaseResponse<PageResponse<PeriodicReportResponse>>> getPeriodicReportsByCourse(
            @PathVariable Integer courseId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "reportFromDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<PeriodicReportResponse> result;
        if (fromDate != null && toDate != null) {
            result = periodicReportService.getPeriodicReportsByDateRange(courseId, fromDate, toDate, pageable);
        } else {
            result = periodicReportService.getPeriodicReportsByCourse(courseId, pageable);
        }
        
        PageResponse<PeriodicReportResponse> response = PageResponse.of(result);
        
        return ResponseEntity.ok(BaseResponse.success("Periodic reports retrieved successfully", response));
    }
    
    @GetMapping("/courses/{courseId}/submissions/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER', 'STUDENT')")
    @Operation(summary = "Lấy periodic report đang mở (có thể submit, có phân trang)")
    public ResponseEntity<BaseResponse<PageResponse<PeriodicReportResponse>>> getOpenReportsByCourse(
            @PathVariable Integer courseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "submitStartAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<PeriodicReportResponse> result = periodicReportService.getOpenReportsByCourse(courseId, pageable);
        PageResponse<PeriodicReportResponse> response = PageResponse.of(result);
        
        return ResponseEntity.ok(BaseResponse.success("Open periodic reports retrieved successfully", response));
    }
    
    @PutMapping("/{reportId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    @Operation(summary = "Cập nhật periodic report")
    public ResponseEntity<BaseResponse<PeriodicReportResponse>> updatePeriodicReport(
            @PathVariable Integer reportId,
            @Valid @RequestBody PeriodicReportRequest request) {
        
        PeriodicReportResponse response = periodicReportService.updatePeriodicReport(reportId, request);
        
        return ResponseEntity.ok(BaseResponse.success("Periodic report updated successfully", response));
    }
    
    @DeleteMapping("/{reportId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    @Operation(summary = "Xóa periodic report (chuyển status sang INACTIVE)")
    public ResponseEntity<BaseResponse<Void>> deletePeriodicReport(
            @PathVariable Integer reportId) {
        
        periodicReportService.deletePeriodicReport(reportId);
        
        return ResponseEntity.ok(BaseResponse.success("Periodic report deleted successfully", null));
    }
    
    @PatchMapping("/{reportId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    @Operation(summary = "Khôi phục periodic report (chuyển status sang ACTIVE)")
    public ResponseEntity<BaseResponse<PeriodicReportResponse>> restorePeriodicReport(
            @PathVariable Integer reportId) {
        
        PeriodicReportResponse response = periodicReportService.restorePeriodicReport(reportId);
        
        return ResponseEntity.ok(BaseResponse.success("Periodic report restored successfully", response));
    }
    
    @GetMapping("/inactive")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lấy danh sách periodic report đã xóa (INACTIVE - chỉ admin, có phân trang)")
    public ResponseEntity<BaseResponse<PageResponse<PeriodicReportResponse>>> getInactivePeriodicReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<PeriodicReportResponse> result = periodicReportService.getInactivePeriodicReports(pageable);
        PageResponse<PeriodicReportResponse> response = PageResponse.of(result);
        
        return ResponseEntity.ok(BaseResponse.success("Inactive periodic reports retrieved successfully", response));
    }
}
