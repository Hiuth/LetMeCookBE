// src/main/java/com/example/letmecookbe/controller/ReportController.java
package com.example.letmecookbe.controller;
import org.springframework.data.domain.Pageable;
import com.example.letmecookbe.dto.request.ReportRequest;
import com.example.letmecookbe.dto.request.ReportStatusUpdateRequest;
import com.example.letmecookbe.dto.response.ApiResponse;
import com.example.letmecookbe.dto.response.ReportResponse;
import com.example.letmecookbe.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page; // Import Page
import org.springframework.data.domain.Pageable; // Import Pageable
import org.springframework.data.web.PageableDefault; // Import PageableDefault
import org.springframework.data.domain.Sort; // Import Sort để định nghĩa hướng sắp xếp mặc định
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List; // Vẫn giữ import nếu bạn có các phương thức khác trả về List, dù getAllReports sẽ trả về Page

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private final ReportService reportService;

    /**
     * API: Gửi báo cáo mới (Người dùng)
     * POST /reports
     * Body: ReportRequest (reportType, reportedItemId, reason)
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ReportResponse>> createReport(@RequestBody @Valid ReportRequest request) {
        log.info("Received request to create a report: {}", request);
        ReportResponse response = reportService.createReport(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<ReportResponse>builder()
                        .code(HttpStatus.CREATED.value())
                        .message("Báo cáo đã được gửi thành công.")
                        .result(response)
                        .build()
        );
    }

    /**
     * API: Xem tất cả các báo cáo (Admin) - Đã thêm phân trang
     * GET /reports?status={status}&page={page}&size={size}&sort={sort}
     * Params: status (PENDING, RESOLVED, REJECTED), page, size, sort
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')") // Chỉ admin mới có quyền xem tất cả báo cáo
    public ResponseEntity<ApiResponse<Page<ReportResponse>>> getAllReports(
            @RequestParam(required = false) String status,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Received request to get all reports with status: {} and pageable: {}", status, pageable);
        // Cần cập nhật ReportService để phương thức này nhận thêm tham số Pageable và trả về Page
        Page<ReportResponse> reports = reportService.getAllReports(status, pageable);
        return ResponseEntity.ok(
                ApiResponse.<Page<ReportResponse>>builder()
                        .code(HttpStatus.OK.value())
                        .message("Lấy danh sách báo cáo thành công.")
                        .result(reports)
                        .build()
        );
    }


    /**
     * API: Xem chi tiết một báo cáo theo ID (Admin)
     * GET /reports/{reportId}
     */
    @GetMapping("/{reportId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ReportResponse>> getReportById(@PathVariable String reportId) {
        log.info("Received request to get report by ID: {}", reportId);
        ReportResponse report = reportService.getReportById(reportId);
        return ResponseEntity.ok(
                ApiResponse.<ReportResponse>builder()
                        .code(HttpStatus.OK.value())
                        .message("Lấy chi tiết báo cáo thành công.")
                        .result(report)
                        .build()
        );
    }

    /**
     * API: Cập nhật trạng thái và phản hồi báo cáo (Admin)
     * PUT /reports/{reportId}/status
     * Body: ReportStatusUpdateRequest (newStatus, adminResponse)
     */
    @PutMapping("/{reportId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ReportResponse>> updateReportStatus(
            @PathVariable String reportId,
            @RequestBody @Valid ReportStatusUpdateRequest request) {
        log.info("Received request to update report status for ID {}: {}", reportId, request);
        ReportResponse updatedReport = reportService.updateReportStatus(reportId, request);
        return ResponseEntity.ok(
                ApiResponse.<ReportResponse>builder()
                        .code(HttpStatus.OK.value())
                        .message("Cập nhật trạng thái báo cáo thành công.")
                        .result(updatedReport)
                        .build()
        );
    }

    /**
     * API: Xóa một báo cáo (Admin)
     * DELETE /reports/{reportId}
     */
    @DeleteMapping("/{reportId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteReport(@PathVariable String reportId) {
        log.info("Received request to delete report by ID: {}", reportId);
        reportService.deleteReport(reportId);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .code(HttpStatus.OK.value())
                        .message("Báo cáo đã được xóa thành công.")
                        .build()
        );
    }
}