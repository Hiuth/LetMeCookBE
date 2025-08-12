package com.example.letmecookbe.dto.response;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentResponse {
    String id; // ID của comment
    String commentText; // Nội dung comment
    String accountId; // ID của người dùng đã tạo comment
    String username;  // Tên người dùng tạo comment (có thể lấy từ Account entity)
    String recipeId;  // ID của công thức mà comment thuộc về
    String recipeTitle;
    String recipeImage;
    int likes;
    String userAvatar;
    private String status;// Tiêu đề của công thức (có thể lấy từ Recipe entity)
    LocalDateTime createdAt; // Thời gian comment được tạo
}