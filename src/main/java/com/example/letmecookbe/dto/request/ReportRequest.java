// src/main/java/com/example/letmecookbe/dto/request/ReportRequest.java
package com.example.letmecookbe.dto.request;

import com.example.letmecookbe.enums.ReportType; // Import enum ReportType
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReportRequest {

    @NotNull(message = "Loại báo cáo không được để trống")
    private ReportType reportType;
    @NotBlank(message = "ID của đối tượng báo cáo không được để trống")
    private String reportedItemId;
    @Size(max = 500, message = "Lý do báo cáo không được vượt quá 500 ký tự")
    private String reason;
    private String description;
    MultipartFile evidenceImage;
}