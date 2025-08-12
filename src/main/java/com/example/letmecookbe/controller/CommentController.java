package com.example.letmecookbe.controller;

import com.example.letmecookbe.service.ReportService; // ĐẢM BẢO ĐÃ IMPORT ReportService
import com.example.letmecookbe.dto.request.CommentRequest;
import com.example.letmecookbe.dto.response.ApiResponse;
import com.example.letmecookbe.dto.response.CommentResponse;
import com.example.letmecookbe.enums.CommentStatus;
import com.example.letmecookbe.service.CommentService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Sort;
import java.util.List;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentController {
    CommentService commentService;
    private final ReportService reportService; // THÊM DÒNG NÀY ĐỂ INJECT ReportService

    // --- 1. Xem tất cả Comment của một Bài đăng (GET) ---
    @GetMapping("/recipe/{recipeId}")
    public ResponseEntity<Page<CommentResponse>> getCommentsByRecipe(
            @PathVariable String recipeId,
            @PageableDefault(size = 10, page = 0, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<CommentResponse> comments = commentService.getCommentsByRecipe(recipeId, pageable);
        return ResponseEntity.ok(comments);
    }

    // --- 2. Tạo Comment (POST) ---
    @PostMapping("/{recipeId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @PathVariable String recipeId,
            @RequestBody @Valid CommentRequest request) {
        CommentResponse createdComment = commentService.createComment(recipeId, request);
        ApiResponse<CommentResponse> response = new ApiResponse<>();
        response.setMessage("Comment created successfully");
        response.setResult(createdComment);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // --- 3. Chỉnh sửa Comment (PUT) ---
    @PutMapping("/{recipeId}/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
            @PathVariable String commentId,
            @RequestBody @Valid CommentRequest request) {
        CommentResponse updatedComment = commentService.updateComment(commentId, request);
        ApiResponse<CommentResponse> response = new ApiResponse<>();
        response.setMessage("Comment updated successfully");
        response.setResult(updatedComment);
        return ResponseEntity.ok(response);
    }

    // --- 4. Xóa Comment (DELETE) ---
    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteComment(@PathVariable String commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }

    // --- 5. Xem một Comment cụ thể theo ID (GET) ---
    @GetMapping("/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponse>> getCommentById(@PathVariable String commentId) {
        CommentResponse comment = commentService.getCommentById(commentId);
        ApiResponse<CommentResponse> response = new ApiResponse<>();
        response.setMessage("Get comment by ID");
        response.setResult(comment);
        return ResponseEntity.ok(response);
    }

    // --- MỚI THÊM: 6. Xem tất cả Comment trong hệ thống (có phân trang) ---
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<Page<CommentResponse>>> getAllComments(
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) String recipeId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String date) {

        ApiResponse<Page<CommentResponse>> response = new ApiResponse<>();
        response.setMessage("Get all comments with pagination and filters");
        response.setResult(commentService.getAllComments(pageable, searchTerm, recipeId, status, date));
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{commentId}/status")
    public ResponseEntity<ApiResponse<CommentResponse>> updateCommentStatus(
            @PathVariable String commentId,
            @RequestParam("status") String newStatus) {
        CommentResponse updatedComment = commentService.updateCommentStatus(commentId, newStatus);
        ApiResponse<CommentResponse> response = new ApiResponse<>();
        response.setMessage("Comment status updated successfully");
        response.setResult(updatedComment);
        return ResponseEntity.ok(response);
    }

    // Endpoint để lấy tổng số comment theo trạng thái
    @GetMapping("/count-by-status")
    public ApiResponse<Long> countCommentsByStatus(@RequestParam CommentStatus status) {
        long count = commentService.countCommentsByStatus(status);
        return ApiResponse.<Long>builder()
                .code(1000)
                .message("Get total comments by status successfully")
                .result(count)
                .build();
    }

    // Endpoint để lấy tổng số báo cáo comment từ bảng reports
    @GetMapping("/total-comment-reports")
    public ApiResponse<Long> getTotalCommentReportsFromReportEntity() {
        // Gọi service của ReportService để lấy tổng số báo cáo comment
        long count = reportService.getTotalCommentReportsFromReportEntity();
        return ApiResponse.<Long>builder()
                .code(1000)
                .message("Get total comment reports from Report entity successfully")
                .result(count)
                .build();
    }

    @GetMapping("/getCommentByAccount")
    public ApiResponse<Page<CommentResponse>> getCommentByAccount(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size)
    {
        Pageable pageable = PageRequest.of(page, size);
        ApiResponse<Page<CommentResponse>> response = new ApiResponse<>();
        response.setMessage("Get all Comment by Account: ");
        response.setResult(commentService.getCommentsByAccountId(pageable));
        return response;
    }

    @GetMapping("/countCommentByAccount")
    public ApiResponse<Integer> countCommentByAccount()
    {
        ApiResponse<Integer> response = new ApiResponse<>();
        response.setMessage("Get total comment by Account: ");
        response.setResult(commentService.countCommentAccount());
        return response;
    }
}