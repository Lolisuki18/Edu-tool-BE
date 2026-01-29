package com.edutool.controller;

import com.edutool.dto.request.PeriodicReportRequest;
import com.edutool.dto.response.BaseResponse;
import com.edutool.dto.response.PeriodicReportResponse;
import com.edutool.service.PeriodicReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

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
    @Operation(summary = "Lấy tất cả periodic report")
    public ResponseEntity<BaseResponse<List<PeriodicReportResponse>>> getAllPeriodicReports() {
        
        List<PeriodicReportResponse> response = periodicReportService.getAllPeriodicReports();
        
        return ResponseEntity.ok(BaseResponse.success("Periodic reports retrieved successfully", response));
    }
    
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER', 'STUDENT')")
    @Operation(summary = "Lấy tất cả periodic report của course")
    public ResponseEntity<BaseResponse<List<PeriodicReportResponse>>> getPeriodicReportsByCourse(
            @PathVariable Integer courseId) {
        
        List<PeriodicReportResponse> response = periodicReportService.getPeriodicReportsByCourse(courseId);
        
        return ResponseEntity.ok(BaseResponse.success("Periodic reports retrieved successfully", response));
    }
    
    @GetMapping("/course/{courseId}/date-range")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER', 'STUDENT')")
    @Operation(summary = "Lấy periodic report theo khoảng thời gian")
    public ResponseEntity<BaseResponse<List<PeriodicReportResponse>>> getPeriodicReportsByDateRange(
            @PathVariable Integer courseId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {
        
        List<PeriodicReportResponse> response = periodicReportService.getPeriodicReportsByDateRange(
            courseId, fromDate, toDate);
        
        return ResponseEntity.ok(BaseResponse.success("Periodic reports retrieved successfully", response));
    }
    
    @GetMapping("/course/{courseId}/open")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER', 'STUDENT')")
    @Operation(summary = "Lấy periodic report đang mở (có thể submit)")
    public ResponseEntity<BaseResponse<List<PeriodicReportResponse>>> getOpenReportsByCourse(
            @PathVariable Integer courseId) {
        
        List<PeriodicReportResponse> response = periodicReportService.getOpenReportsByCourse(courseId);
        
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
    
    @PostMapping("/{reportId}/restore")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    @Operation(summary = "Khôi phục periodic report (chuyển status sang ACTIVE)")
    public ResponseEntity<BaseResponse<PeriodicReportResponse>> restorePeriodicReport(
            @PathVariable Integer reportId) {
        
        PeriodicReportResponse response = periodicReportService.restorePeriodicReport(reportId);
        
        return ResponseEntity.ok(BaseResponse.success("Periodic report restored successfully", response));
    }
    
    @GetMapping("/inactive")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lấy danh sách periodic report đã xóa (INACTIVE - chỉ admin)")
    public ResponseEntity<BaseResponse<List<PeriodicReportResponse>>> getInactivePeriodicReports() {
        
        List<PeriodicReportResponse> response = periodicReportService.getInactivePeriodicReports();
        
        return ResponseEntity.ok(BaseResponse.success("Inactive periodic reports retrieved successfully", response));
    }
}
