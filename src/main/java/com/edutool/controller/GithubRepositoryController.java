package com.edutool.controller;

import com.edutool.dto.request.GithubRepositoryRequest;
import com.edutool.dto.request.CommitReportRequest;
import com.edutool.dto.response.BaseResponse;
import com.edutool.dto.response.CommitReportResponse;
import com.edutool.dto.response.CommitReportUrlResponse;
import com.edutool.dto.response.GroupRepositoryResponse;
import com.edutool.dto.response.GithubRepositoryResponse;
import com.edutool.service.CommitReportService;
import com.edutool.service.GithubApiService;
import com.edutool.service.GithubRepositoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/github/repositories")
@RequiredArgsConstructor
@Tag(name = "GitHub Repository", description = "APIs quản lý GitHub Repository của project")
public class GithubRepositoryController {

    private final GithubRepositoryService repositoryService;
    private final GithubApiService githubApiService;
    private final CommitReportService commitReportService;

    // -------------------------------------------------------------------------
    //  CRUD
    // -------------------------------------------------------------------------

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER', 'STUDENT')")
    @Operation(summary = "Nộp GitHub repository cho project",
               description = "Sinh viên / Giảng viên / Admin nộp link GitHub repository cho project.")
    public ResponseEntity<BaseResponse<GithubRepositoryResponse>> createRepository(
            @Valid @RequestBody GithubRepositoryRequest request) {

        GithubRepositoryResponse response = repositoryService.createRepository(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success("Repository submitted successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER', 'STUDENT')")
    @Operation(summary = "Lấy danh sách repositories",
               description = "Lấy repositories theo projectId hoặc courseId.")
    public ResponseEntity<BaseResponse<List<GithubRepositoryResponse>>> getRepositories(
            @Parameter(description = "ID của project") @RequestParam(required = false) Integer projectId,
            @Parameter(description = "ID của course")  @RequestParam(required = false) Integer courseId) {

        List<GithubRepositoryResponse> repos;
        if (projectId != null) {
            repos = repositoryService.getRepositoriesByProject(projectId);
        } else if (courseId != null) {
            repos = repositoryService.getRepositoriesByCourse(courseId);
        } else {
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error(400, "Provide either projectId or courseId"));
        }

        return ResponseEntity.ok(BaseResponse.success(
                "Retrieved " + repos.size() + " repositories", repos));
    }

    @GetMapping("/{repoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER', 'STUDENT')")
    @Operation(summary = "Lấy thông tin repository theo ID")
    public ResponseEntity<BaseResponse<GithubRepositoryResponse>> getRepositoryById(
            @PathVariable Integer repoId) {

        GithubRepositoryResponse response = repositoryService.getRepositoryById(repoId);
        return ResponseEntity.ok(BaseResponse.success("Repository retrieved successfully", response));
    }

    @PutMapping("/{repoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER', 'STUDENT')")
    @Operation(summary = "Cập nhật thông tin repository")
    public ResponseEntity<BaseResponse<GithubRepositoryResponse>> updateRepository(
            @PathVariable Integer repoId,
            @Valid @RequestBody GithubRepositoryRequest request) {

        GithubRepositoryResponse response = repositoryService.updateRepository(repoId, request);
        return ResponseEntity.ok(BaseResponse.success("Repository updated successfully", response));
    }

    @PatchMapping("/{repoId}/select")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    @Operation(summary = "Chọn repository để track commits",
               description = "Admin/Lecturer chỉ định repository nào sẽ được dùng để lấy commit report. " +
                              "Chỉ có 1 repo được chọn trong cùng một project.")
    public ResponseEntity<BaseResponse<GithubRepositoryResponse>> selectRepository(
            @PathVariable Integer repoId) {

        GithubRepositoryResponse response = repositoryService.selectRepository(repoId);
        return ResponseEntity.ok(BaseResponse.success("Repository selected for tracking", response));
    }

    @DeleteMapping("/{repoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    @Operation(summary = "Xóa repository")
    public ResponseEntity<BaseResponse<Void>> deleteRepository(
            @PathVariable Integer repoId) {

        repositoryService.deleteRepository(repoId);
        return ResponseEntity.ok(BaseResponse.success("Repository deleted successfully", null));
    }

    // -------------------------------------------------------------------------
    //  Grouped view – for "Quản lý Repository" page
    // -------------------------------------------------------------------------

    @GetMapping("/course/{courseId}/groups")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER', 'STUDENT')")
    @Operation(summary = "Lấy repositories được nhóm theo nhóm (group) trong một môn học",
               description = "Trả về danh sách các nhóm trong course, mỗi nhóm chứa: " +
                              "thông tin project, danh sách thành viên và danh sách repo đã nộp. " +
                              "Dùng cho trang Quản lý Repository ở frontend.")
    public ResponseEntity<BaseResponse<List<GroupRepositoryResponse>>> getGroupedRepositories(
            @PathVariable Integer courseId) {

        List<GroupRepositoryResponse> groups = repositoryService.getGroupedByCourse(courseId);
        return ResponseEntity.ok(BaseResponse.success(
                "Retrieved " + groups.size() + " groups", groups));
    }

    // -------------------------------------------------------------------------
    //  Report – CSV download
    // -------------------------------------------------------------------------

    @GetMapping("/project/{projectId}/report/csv")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    @Operation(summary = "Xuất báo cáo commit CSV",
               description = "Gọi GitHub public API để lấy commit statistics của toàn bộ sinh viên trong project, " +
                              "sau đó xuất ra file CSV để frontend upload lên Supabase Storage. " +
                              "Project phải có ít nhất 1 repository được đánh dấu 'selected'.")
    public ResponseEntity<InputStreamResource> generateCsvReport(
            @PathVariable Integer projectId,
            @Parameter(description = "Từ ngày (yyyy-MM-dd), bỏ trống = không giới hạn")
            @RequestParam(required = false) String since,
            @Parameter(description = "Đến ngày (yyyy-MM-dd), bỏ trống = không giới hạn")
            @RequestParam(required = false) String until) {

        ByteArrayInputStream csvStream = githubApiService.generateCommitCsvReport(
                projectId, since, until);

        String filename = String.format("commit-report-project-%d-%s.csv",
                projectId, LocalDate.now().format(DateTimeFormatter.ISO_DATE));

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(new InputStreamResource(csvStream));
    }

    @GetMapping("/project/{projectId}/report/json")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    @Operation(summary = "Xuất báo cáo commit JSON",
               description = "Trả về dữ liệu commit statistics dạng JSON thay vì CSV, " +
                              "giúp frontend dễ dàng hiển thị và xử lý dữ liệu.")
    public ResponseEntity<BaseResponse<CommitReportResponse>> generateJsonReport(
            @PathVariable Integer projectId,
            @Parameter(description = "Từ ngày (yyyy-MM-dd), bỏ trống = không giới hạn")
            @RequestParam(required = false) String since,
            @Parameter(description = "Đến ngày (yyyy-MM-dd), bỏ trống = không giới hạn")
            @RequestParam(required = false) String until) {

        CommitReportResponse report = githubApiService.generateCommitJsonReport(
                projectId, since, until);

        return ResponseEntity.ok(BaseResponse.success("Report generated successfully", report));
    }

    // -------------------------------------------------------------------------
    //  Commit Report Storage URL
    // -------------------------------------------------------------------------

    @PostMapping("/project/{projectId}/report/storage-url")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    @Operation(summary = "Lưu URL báo cáo commit từ Supabase",
               description = "Sau khi FE upload file CSV lên Supabase Storage, gọi API này để lưu URL vào DB.")
    public ResponseEntity<BaseResponse<CommitReportUrlResponse>> saveReportUrl(
            @PathVariable Integer projectId,
            @Valid @RequestBody CommitReportRequest request) {

        request.setProjectId(projectId);
        CommitReportUrlResponse response = commitReportService.saveReportUrl(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success("Report URL saved successfully", response));
    }

    @GetMapping("/project/{projectId}/report/storage-url")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    @Operation(summary = "Lấy danh sách URL báo cáo commit đã lưu",
               description = "Trả về lịch sử tất cả các báo cáo CSV đã upload lên Supabase cho project này.")
    public ResponseEntity<BaseResponse<List<CommitReportUrlResponse>>> getReportUrls(
            @PathVariable Integer projectId) {

        List<CommitReportUrlResponse> responses = commitReportService.getReportsByProject(projectId);
        return ResponseEntity.ok(BaseResponse.success(
                "Retrieved " + responses.size() + " report(s)", responses));
    }

    @DeleteMapping("/project/{projectId}/report/storage-url/{commitReportId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LECTURER')")
    @Operation(summary = "Xóa URL báo cáo commit",
               description = "Xóa một bản ghi commit report đã lưu theo ID.")
    public ResponseEntity<BaseResponse<Void>> deleteReportUrl(
            @PathVariable Integer projectId,
            @PathVariable Integer commitReportId) {
        commitReportService.deleteReport(projectId, commitReportId);
        return ResponseEntity.ok(BaseResponse.success("Report URL deleted successfully", null));
    }
}
