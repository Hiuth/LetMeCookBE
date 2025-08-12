package com.example.letmecookbe.dto.response;

import com.example.letmecookbe.enums.ReportStatus;
import com.example.letmecookbe.enums.ReportType;
import com.example.letmecookbe.enums.ReportSeverity;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReportResponse {
    String id;

    // Thông tin người báo cáo
    String reporterAccountUsername;
    String reporterAccountEmail;
    String description;
    // THÊM CÁC TRƯỜNG NÀY ĐỂ LƯU THÔNG TIN CỦA NGƯỜI BỊ BÁO CÁO
    String reportedAccountUsername; // Tên người bị báo cáo
    String reportedAccountEmail;    // Email người bị báo cáo

    ReportType reportType; // Loại đối tượng bị báo cáo
    String reportedItemId; // ID của đối tượng bị báo cáo (recipe, comment, user)

    String reason; // Lý do báo cáo
    ReportSeverity severity; // Mức độ nghiêm trọng
    ReportStatus status;
    LocalDateTime createdAt;
    String adminResponse;
    String evidenceImageUrl;
    // Thông tin admin đã xử lý
    String resolvedByAdminUsername;
    String resolvedByAdminEmail;
    LocalDateTime resolvedAt;
}