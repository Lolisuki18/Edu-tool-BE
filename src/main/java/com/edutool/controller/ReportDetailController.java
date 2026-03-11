package com.edutool.controller;

import com.edutool.dto.request.ReportDetailRequest;
import com.edutool.dto.response.BaseResponse;
import com.edutool.dto.response.PageResponse;
import com.edutool.dto.response.ReportDetailResponse;
import com.edutool.service.ReportDetailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/report-details")
@RequiredArgsConstructor
@Tag(name = "Report Detail", description = "APIs quản lý chi tiết báo cáo")
public class ReportDetailController {
    
    private final ReportDetailService reportDetailService;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER', 'STUDENT')")
    @Operation(summary = "Tạo report detail mới")
    public ResponseEntity<BaseResponse<ReportDetailResponse>> createReportDetail(
            @Valid @RequestBody ReportDetailRequest request) {
        
        ReportDetailResponse response = reportDetailService.createReportDetail(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(BaseResponse.success("Report detail created successfully", response));
    }
    
    @GetMapping("/{reportDetailId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER', 'STUDENT')")
    @Operation(summary = "Lấy thông tin report detail theo ID")
    public ResponseEntity<BaseResponse<ReportDetailResponse>> getReportDetailById(
            @PathVariable Integer reportDetailId) {
        
        ReportDetailResponse response = reportDetailService.getReportDetailById(reportDetailId);
        
        return ResponseEntity.ok(BaseResponse.success("Report detail retrieved successfully", response));
    }
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    @Operation(summary = "Lấy tất cả report details (có phân trang)")
    public ResponseEntity<BaseResponse<PageResponse<ReportDetailResponse>>> getAllReportDetails(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "lastUpdatedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<ReportDetailResponse> result = reportDetailService.getAllReportDetails(pageable);
        PageResponse<ReportDetailResponse> response = PageResponse.of(result);
        
        return ResponseEntity.ok(BaseResponse.success("Report details retrieved successfully", response));
    }
    
    @GetMapping("/periodic-reports/{reportId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER', 'STUDENT')")
    @Operation(summary = "Lấy report details theo periodic report ID (có phân trang)")
    public ResponseEntity<BaseResponse<PageResponse<ReportDetailResponse>>> getReportDetailsByPeriodicReport(
            @PathVariable Integer reportId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "lastUpdatedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<ReportDetailResponse> result = reportDetailService.getReportDetailsByPeriodicReport(reportId, pageable);
        PageResponse<ReportDetailResponse> response = PageResponse.of(result);
        
        return ResponseEntity.ok(BaseResponse.success("Report details retrieved successfully", response));
    }
    
    @GetMapping("/students/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER', 'STUDENT')")
    @Operation(summary = "Lấy report details theo student ID (có phân trang)")
    public ResponseEntity<BaseResponse<PageResponse<ReportDetailResponse>>> getReportDetailsByStudent(
            @PathVariable Integer studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "lastUpdatedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<ReportDetailResponse> result = reportDetailService.getReportDetailsByStudent(studentId, pageable);
        PageResponse<ReportDetailResponse> response = PageResponse.of(result);
        
        return ResponseEntity.ok(BaseResponse.success("Report details retrieved successfully", response));
    }
    
    @GetMapping("/projects/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER', 'STUDENT')")
    @Operation(summary = "Lấy report details theo project ID (có phân trang)")
    public ResponseEntity<BaseResponse<PageResponse<ReportDetailResponse>>> getReportDetailsByProject(
            @PathVariable Integer projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "lastUpdatedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<ReportDetailResponse> result = reportDetailService.getReportDetailsByProject(projectId, pageable);
        PageResponse<ReportDetailResponse> response = PageResponse.of(result);
        
        return ResponseEntity.ok(BaseResponse.success("Report details retrieved successfully", response));
    }
    
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    @Operation(summary = "Lấy report details theo status (có phân trang)")
    public ResponseEntity<BaseResponse<PageResponse<ReportDetailResponse>>> getReportDetailsByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "lastUpdatedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<ReportDetailResponse> result = reportDetailService.getReportDetailsByStatus(status, pageable);
        PageResponse<ReportDetailResponse> response = PageResponse.of(result);
        
        return ResponseEntity.ok(BaseResponse.success("Report details retrieved successfully", response));
    }
    
    @PutMapping("/{reportDetailId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER', 'STUDENT')")
    @Operation(summary = "Cập nhật report detail")
    public ResponseEntity<BaseResponse<ReportDetailResponse>> updateReportDetail(
            @PathVariable Integer reportDetailId,
            @Valid @RequestBody ReportDetailRequest request) {
        
        ReportDetailResponse response = reportDetailService.updateReportDetail(reportDetailId, request);
        
        return ResponseEntity.ok(BaseResponse.success("Report detail updated successfully", response));
    }
    
    @DeleteMapping("/{reportDetailId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    @Operation(summary = "Xóa report detail")
    public ResponseEntity<BaseResponse<Void>> deleteReportDetail(
            @PathVariable Integer reportDetailId) {
        
        reportDetailService.deleteReportDetail(reportDetailId);
        
        return ResponseEntity.ok(BaseResponse.success("Report detail deleted successfully", null));
    }
}
