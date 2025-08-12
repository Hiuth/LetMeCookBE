package com.example.letmecookbe.dto.request;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull; // <-- Thêm nếu bạn sử dụng validation

// DTO này dùng để nhận dữ liệu từ client khi muốn tạo một thông báo mới
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest{
    @NotBlank(message = "Title is required")
    private String title;
    @NotBlank(message = "Message is required") // <-- Giữ nguyên là message
    private String message;
    @NotBlank(message = "Type is required") // <-- Giữ nguyên là type (nhưng sẽ cần xử lý chuyển đổi String sang Enum)
    private String type; // e.g., "recipe", "comment", "user", "system"
    private String recipientUsername; // Tùy chọn, nếu là thông báo riêng tư
}