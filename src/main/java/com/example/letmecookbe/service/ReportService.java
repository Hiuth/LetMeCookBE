package com.example.letmecookbe.service;
import com.example.letmecookbe.enums.NotificationType;
import com.example.letmecookbe.dto.request.ReportRequest;
import com.example.letmecookbe.dto.request.ReportStatusUpdateRequest;
import com.example.letmecookbe.dto.response.ReportResponse;
import com.example.letmecookbe.entity.Account;
import com.example.letmecookbe.entity.Report;
import com.example.letmecookbe.entity.Recipe;
import com.example.letmecookbe.entity.Comment;
import com.example.letmecookbe.exception.AppException;
import com.example.letmecookbe.exception.ErrorCode;
import com.example.letmecookbe.mapper.ReportMapper;
import com.example.letmecookbe.repository.AccountRepository;
import com.example.letmecookbe.repository.ReportRepository;
import com.example.letmecookbe.repository.RecipeRepository;
import com.example.letmecookbe.repository.CommentRepository;
import com.example.letmecookbe.enums.ReportStatus;
import com.example.letmecookbe.enums.ReportType;
import com.example.letmecookbe.enums.ReportSeverity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile; // <-- Thêm import này

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {
    private final NotificationService notificationService;
    private final ReportRepository reportRepository;
    private final AccountRepository accountRepository;
    private final RecipeRepository recipeRepository;
    private final CommentRepository commentRepository;
    private final ReportMapper reportMapper;
    private final AuthService authService;
    private final FileStorageService fileStorageService; // <-- Inject FileStorageService trở lại

    /**
     * Tạo một báo cáo mới.
     * Người dùng (ROLE_USER) có thể tạo báo cáo về công thức, bình luận hoặc người dùng khác.
     */
    @PreAuthorize("hasRole('USER')")
    public ReportResponse createReport(ReportRequest request) {
        Account currentUser = authService.getCurrentAccount();
        Account reportedAccountForEntity = null;

        // Xử lý báo cáo công thức
        if (request.getReportType() == ReportType.REPORT_RECIPE) {
            Recipe recipe = recipeRepository.findById(request.getReportedItemId())
                    .orElseThrow(() -> new AppException(ErrorCode.RECIPE_NOT_FOUND));

            if (recipe.getAccount().getId().equals(currentUser.getId())) {
                throw new AppException(ErrorCode.CANNOT_REPORT_OWN_CONTENT);
            }
            reportedAccountForEntity = recipe.getAccount();
        }

        // Xử lý báo cáo bình luận
        else if (request.getReportType() == ReportType.REPORT_COMMENT) {
            Comment comment = commentRepository.findById(request.getReportedItemId())
                    .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_EXIST));

            if (comment.getAccount().getId().equals(currentUser.getId())) {
                throw new AppException(ErrorCode.CANNOT_REPORT_OWN_CONTENT);
            }
            reportedAccountForEntity = comment.getAccount();
        }

        // Xử lý báo cáo người dùng
        else if (request.getReportType() == ReportType.REPORT_USER) {
            Account reportedUser = accountRepository.findById(request.getReportedItemId())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

            if (reportedUser.getId().equals(currentUser.getId())) {
                throw new AppException(ErrorCode.CANNOT_REPORT_OWN_ACCOUNT);
            }
            reportedAccountForEntity = reportedUser;
        }

        // Loại báo cáo không hợp lệ
        else {
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        // Upload ảnh bằng chứng nếu có
        String evidenceImageUrl = null;
        if (request.getEvidenceImage() != null && !request.getEvidenceImage().isEmpty()) {
            try {
                evidenceImageUrl = fileStorageService.uploadFile(request.getEvidenceImage());
            } catch (AppException e) {
                log.error("❌ Failed to upload evidence image: {}", e.getMessage());
                throw e;
            } catch (Exception e) {
                log.error("❌ Unexpected error during file upload: {}", e.getMessage());
                throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
            }
        }

        // Tạo report
        Report report = reportMapper.toEntity(request);
        report.setReporterAccount(currentUser);
        report.setReportedAccount(reportedAccountForEntity);
        report.setStatus(ReportStatus.PENDING);
        report.setSeverity(determineSeverity(request.getReason(), request.getReportType()));
        report.setEvidenceImageUrl(evidenceImageUrl);

        Report savedReport = reportRepository.save(report);
        log.info("✅ Report created successfully for item ID: {} with type: {} by user: {}",
                request.getReportedItemId(), request.getReportType(), currentUser.getEmail());

        // ✅ Gửi thông báo đến ADMIN
        List<Account> adminAccounts = accountRepository.findAllByRoles_Name("ADMIN");

        String reportTypeDescription;
        String reportedTarget = "";

        switch (request.getReportType()) {
            case REPORT_COMMENT -> {
                reportTypeDescription = "bình luận";
                Comment comment = commentRepository.findById(request.getReportedItemId())
                        .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_EXIST));
                reportedTarget = "bình luận của người dùng " + comment.getAccount().getUsername()
                        + ": \"" + comment.getCommentText() + "\"";
            }
            case REPORT_RECIPE -> {
                reportTypeDescription = "công thức";
                Recipe recipe = recipeRepository.findById(request.getReportedItemId())
                        .orElseThrow(() -> new AppException(ErrorCode.RECIPE_NOT_FOUND));
                reportedTarget = "công thức \"" + recipe.getTitle() + "\" của " + recipe.getAccount().getUsername();
            }
            case REPORT_USER -> {
                reportTypeDescription = "người dùng";
                Account reportedUser = accountRepository.findById(request.getReportedItemId())
                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
                reportedTarget = "người dùng " + reportedUser.getUsername();
            }
            default -> reportTypeDescription = "nội dung không xác định";
        }

        String adminTitle = "🚨 Báo cáo mới từ người dùng";
        String adminContent = "Người dùng " + currentUser.getUsername() +
                " vừa gửi báo cáo về " + reportTypeDescription +
                " liên quan đến " + reportedTarget +
                " với nội dung: " + request.getReason();

        for (Account admin : adminAccounts) {
            // Không gửi thông báo cho admin nếu họ cũng là người gửi report
            if (admin.getId().equals(currentUser.getId())) {
                continue;
            }
            // Gửi riêng tư cho từng admin
            notificationService.createTypedNotification(
                    currentUser,
                    admin,
                    NotificationType.REPORT,
                    adminTitle,
                    adminContent
            );
        }

// Gửi thông báo xác nhận đến người gửi (chỉ user đó nhận được)
        notificationService.createTypedNotification(
                null,
                currentUser,
                NotificationType.REPORT,
                "📩 Báo cáo của bạn đã được gửi",
                "Chúng tôi đã nhận được báo cáo của bạn và sẽ xử lý trong thời gian sớm nhất. Cảm ơn bạn đã đóng góp!"
        );

        return reportMapper.toDto(savedReport);
    }


    /**
     * Xác định mức độ nghiêm trọng của báo cáo dựa trên lý do và loại báo cáo.
     */
    private ReportSeverity determineSeverity(String reason, ReportType reportType) {
        switch (reason) {
            case "Spam hoặc quảng cáo":
                return ReportSeverity.MEDIUM;
            case "Ngôn từ thù địch hoặc quấy rối":
                return ReportSeverity.HIGH;
            case "Vi phạm bản quyền":
                return ReportSeverity.CRITICAL;
            case "Lừa đảo hoặc gian lận":
                return ReportSeverity.CRITICAL;
            case "Thông tin sai lệch":
                if (reportType == ReportType.REPORT_RECIPE) {
                    return ReportSeverity.HIGH;
                }
                return ReportSeverity.MEDIUM;
            case "Nội dung không phù hợp":
                return ReportSeverity.LOW;
            case "Nội dung bạo lực":
                return ReportSeverity.HIGH;
            case "Khác":
                return ReportSeverity.LOW;
            default:
                log.warn("Unknown report reason: {}", reason);
                return ReportSeverity.LOW;
        }
    }

    /**
     * Lấy tất cả báo cáo, có thể lọc theo trạng thái và hỗ trợ phân trang.
     * Chỉ Admin (ROLE_ADMIN) mới có quyền truy cập.
     */
    @PreAuthorize("hasRole('ADMIN')")
    public Page<ReportResponse> getAllReports(String status, Pageable pageable) {
        Page<Report> reportsPage;
        if (status != null && !status.isEmpty()) {
            try {
                ReportStatus reportStatus = ReportStatus.valueOf(status.toUpperCase());
                reportsPage = reportRepository.findByStatus(reportStatus, pageable);
            } catch (IllegalArgumentException e) {
                throw new AppException(ErrorCode.INVALID_KEY);
            }
        } else {
            reportsPage = reportRepository.findAll(pageable);
        }
        return reportsPage.map(reportMapper::toDto);
    }

    /**
     * Lấy một báo cáo cụ thể theo ID.
     * Chỉ Admin (ROLE_ADMIN) mới có quyền truy cập.
     */
    @PreAuthorize("hasRole('ADMIN')")
    public ReportResponse getReportById(String reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new AppException(ErrorCode.REPORT_NOT_FOUND));
        return reportMapper.toDto(report);
    }

    /**
     * Cập nhật trạng thái và thêm phản hồi của Admin cho một báo cáo.
     * Chỉ Admin (ROLE_ADMIN) mới có quyền thực hiện.
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ReportResponse updateReportStatus(String reportId, ReportStatusUpdateRequest request) {
        Account currentAdminAccount = authService.getCurrentAccount();

        Report existingReport = reportRepository.findById(reportId)
                .orElseThrow(() -> new AppException(ErrorCode.REPORT_NOT_FOUND));

        reportMapper.updateReportFromStatusRequest(request, existingReport);
        existingReport.setResolvedByAdmin(currentAdminAccount);
        existingReport.setResolvedAt(LocalDateTime.now());

        Report updatedReport = reportRepository.save(existingReport);
        log.info("Report ID {} status updated to {} by admin {}", reportId, request.getNewStatus(), currentAdminAccount.getEmail());

        Account reporter = existingReport.getReporterAccount();

        if (reporter != null) {
            if (ReportStatus.RESOLVED == request.getNewStatus()) {
                log.info("📌 Sending REPORT_RESOLVED notification to {}", reporter.getEmail());
                notificationService.createTypedNotification(
                        null,
                        reporter,
                        NotificationType.REPORT_RESOLVED,
                        "✅ Báo cáo đã được xử lý",
                        "Báo cáo của bạn đã được xử lý bởi admin. Cảm ơn bạn đã đóng góp!"
                );
            } else if (ReportStatus.REJECTED == request.getNewStatus()) {
                log.info("📌 Sending REPORT_IGNORED notification to {}", reporter.getEmail());
                notificationService.createTypedNotification(
                        null,
                        reporter,
                        NotificationType.REPORT_REJECTED,
                        "⚠️ Báo cáo đã được xem xét",
                        "Báo cáo của bạn đã được admin xem xét và đánh dấu bỏ qua. Cảm ơn bạn đã đóng góp!"
                );
            }
        }

        return reportMapper.toDto(updatedReport);
    }

    /**
     * Xóa một báo cáo.
     * Chỉ Admin (ROLE_ADMIN) mới có thể xóa.
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteReport(String reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new AppException(ErrorCode.REPORT_NOT_FOUND));

        // --- Xóa ảnh bằng chứng trên Cloudinary trở lại ---
        if (report.getEvidenceImageUrl() != null && !report.getEvidenceImageUrl().isEmpty()) {
            try {
                fileStorageService.deleteFile(report.getEvidenceImageUrl());
            } catch (AppException e) {
                log.error("Failed to delete evidence image for report ID {}: {}", reportId, e.getMessage());
                // Quyết định: có nên ném lỗi tiếp hay chỉ ghi log và vẫn xóa báo cáo?
                // Hiện tại, tôi sẽ cho phép báo cáo vẫn bị xóa dù ảnh không xóa được để tránh kẹt DB.
                // Tùy thuộc vào yêu cầu nghiệp vụ của bạn.
            }
        }
        // -------------------------------------------

        reportRepository.deleteById(reportId);
        log.info("Report ID {} deleted by admin {}", reportId, authService.getCurrentAccount().getEmail());
    }
    @PreAuthorize("hasRole('ADMIN')") // Hoặc quyền phù hợp
    public long getTotalCommentReportsFromReportEntity() {
        // Sử dụng ReportType.REPORT_COMMENT vì đó là giá trị trong DB của bạn
        return reportRepository.countByReportType(ReportType.REPORT_COMMENT);
    }

}