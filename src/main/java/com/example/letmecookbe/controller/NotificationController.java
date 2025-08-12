package com.example.letmecookbe.controller;
import lombok.extern.slf4j.Slf4j;
import com.example.letmecookbe.dto.request.NotificationRequest;
import com.example.letmecookbe.dto.response.NotificationResponse;
import com.example.letmecookbe.dto.response.ApiResponse; // Import ApiResponse
import com.example.letmecookbe.entity.Account;
import com.example.letmecookbe.entity.Notification;
import com.example.letmecookbe.exception.AppException;
import com.example.letmecookbe.exception.ErrorCode;
import com.example.letmecookbe.mapper.NotificationMapper;
import com.example.letmecookbe.repository.AccountRepository;
import com.example.letmecookbe.repository.NotificationRepository;
import com.example.letmecookbe.service.AuthService;
import com.example.letmecookbe.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;
    private  final AuthService authService;
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
// --- ENDPOINT CHO MỤC ĐÍCH TEST HOẶC ADMIN TẠO THÔNG BÁO ---
// Phân quyền sẽ được xử lý hoàn toàn trong NotificationService.sendPublicNotificationTest
    @PostMapping("/test/public")
    public ResponseEntity<ApiResponse> sendPublicNotificationTest(@RequestBody NotificationRequest request) {
        request.setRecipientUsername(null); // Đảm bảo không có recipientUsername cho thông báo công khai
        NotificationResponse response = notificationService.sendPublicNotificationTest(request);
        return ResponseEntity.ok(ApiResponse.builder()
                .code(HttpStatus.OK.value())
                .message("Public notification sent successfully!")
                .result(response) // ĐÃ THAY ĐỔI TỪ .data(response) SANG .result(response)
                .build());
    }



// Phân quyền sẽ được xử lý hoàn toàn trong NotificationService.sendPrivateNotificationTest
    @PostMapping("/test/private/{recipientUsername}")
    public ResponseEntity<ApiResponse> sendPrivateNotificationTest(
            @PathVariable String recipientUsername,
            @RequestBody NotificationRequest request) {
        request.setRecipientUsername(recipientUsername); // Đảm bảo recipientUsername trong request trùng với path variable
        NotificationResponse response = notificationService.sendPrivateNotificationTest(recipientUsername, request);
        return ResponseEntity.ok(ApiResponse.builder()
                .code(HttpStatus.OK.value())
                .message("Private notification sent successfully!")
                .result(response) // ĐÃ THAY ĐỔI TỪ .data(response) SANG .result(response)
                .build());

    }



// --- ENDPOINT CHO NGƯỜI DÙNG THƯỜNG ---

// Phân quyền sẽ được xử lý hoàn toàn trong NotificationService.getNotificationsForUser
@GetMapping
public ResponseEntity<ApiResponse> getMyNotifications(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
) {
    Page<NotificationResponse> notifications = notificationService.getNotificationsForUser(page, size);
    return ResponseEntity.ok(ApiResponse.builder()
            .code(HttpStatus.OK.value())
            .message("Notifications retrieved successfully!")
            .result(notifications)
            .build());
}




// Phân quyền sẽ được xử lý hoàn toàn trong NotificationService.getNotificationsForUser (với logic admin)

    @GetMapping("/{username}")
    public ResponseEntity<ApiResponse> getNotificationsByUsername(@PathVariable String username) {
        List<NotificationResponse> notifications = notificationService.getNotificationsByUsername(username);
        return ResponseEntity.ok(ApiResponse.builder()
                .code(HttpStatus.OK.value())
                .message("Notifications for user " + username + " retrieved successfully!")
                .result(notifications)
                .build());
    }




// Phân quyền sẽ được xử lý hoàn toàn trong NotificationService.markNotificationAsRead

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse> markNotificationAsRead(@PathVariable String   notificationId) {
        boolean success = notificationService.markNotificationAsRead(notificationId);
        if (success) {
            return ResponseEntity.ok(ApiResponse.builder()
                    .code(HttpStatus.OK.value())
                    .message("Notification marked as read successfully!")
                    .build());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.builder()
                    .code(HttpStatus.NOT_FOUND.value())
                    .message("Notification not found or already read.")
                    .build());
        }

    }



// Phân quyền sẽ được xử lý hoàn toàn trong NotificationService.markAllNotificationsAsRead

    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse> markAllNotificationsAsRead() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        log.info("📌 [DEBUG] Attempting to mark all notifications as read for user: {}", currentUsername);
        notificationService.markAllNotificationsAsRead(currentUsername);
        return ResponseEntity.ok(ApiResponse.builder()
                .code(HttpStatus.OK.value())
                .message("All notifications marked as read successfully!")
                .build());
    }



// Phân quyền sẽ được xử lý hoàn toàn trong NotificationService.deleteNotification

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<String> deleteNotification(@PathVariable String notificationId) {
        boolean deleted = notificationService.deleteNotification(notificationId);
        if (deleted) {
            return ResponseEntity.ok("Notification ID " + notificationId + " deleted successfully by Admin.");
        } else {
            return ResponseEntity.badRequest().body("Notification ID " + notificationId + " could not be deleted by Admin.");
        }
    }

    @DeleteMapping("/all-for-user/{username}") // ĐỔI TÊN ENDPOINT ĐỂ TRÁNH NHẦM LẪN VỚI /all (nếu có)
    public ResponseEntity<String> deleteAllNotificationsForUser(@PathVariable String username) {
        notificationService.deleteAllNotificationsForUser(username);
        return ResponseEntity.ok("All notifications deleted for user: " + username + " by Admin.");
    }

// Phân quyền sẽ được xử lý hoàn toàn trong NotificationService.deleteAllNotificationsForUser

    @DeleteMapping("/all")
    public ResponseEntity<ApiResponse> deleteAllNotificationsForUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        notificationService.deleteAllNotificationsForUser(currentUsername);
        return ResponseEntity.ok(ApiResponse.builder()
                .code(HttpStatus.OK.value())
                .message("All notifications deleted successfully!")
                .build());

    }
    @PutMapping("/{notificationId}/dismiss") // <-- ENDPOINT MỚI ĐỂ ẨN THÔNG BÁO
    // @PreAuthorize("hasAuthority('NOTIFICATION_UPDATE_STATUS')") // Đã có trong Service
    public ResponseEntity<String> dismissNotification(@PathVariable String notificationId) {
        boolean dismissed = notificationService.dismissNotification(notificationId);
        if (dismissed) {
            return ResponseEntity.ok("Notification ID " + notificationId + " dismissed successfully.");
        } else {
            return ResponseEntity.badRequest().body("Notification ID " + notificationId + " was already dismissed or could not be dismissed.");
        }
    }

    @GetMapping("/dismissed")
    public Page<NotificationResponse> getDismissedNotifications(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        Account currentUser = authService.getCurrentAccount();
        log.info("📌 [DEBUG] Fetching dismissed notifications for current user: {}",
                currentUser != null ? currentUser.getEmail() : "null");

        if (currentUser == null) {
            log.warn("📌 [DEBUG] No current user found, returning empty page");
            return Page.empty();
        }

        int effectivePage = (page != null) ? page : 0;
        int effectiveSize = (size != null) ? size : 10;
        Pageable pageable = PageRequest.of(effectivePage, effectiveSize, Sort.by("date").descending().and(Sort.by("time").descending()));

        // Bao gồm cả thông báo công khai (recipientAccount = null) có dismissed = true
        Page<Notification> pageData = notificationRepository.findByRecipientAccountOrRecipientIsNullAndDismissedTrue(currentUser, pageable);
        log.info("📌 [DEBUG] Total dismissed notifications retrieved: {}", pageData.getTotalElements());

        return pageData.map(notificationMapper::toResponse);
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse> getNotificationStats() {
        Map<String, Integer> stats = notificationService.getNotificationStats();

        return ResponseEntity.ok(ApiResponse.builder()
                .code(HttpStatus.OK.value())
                .message("Thống kê số lượng thông báo chưa ẩn theo loại")
                .result(stats)
                .build());
    }


    @PutMapping("/{notificationId}/unhide")
    public ResponseEntity<String> unhideNotification(@PathVariable String notificationId) {
        boolean unhidden = notificationService.unhideNotification(notificationId);
        if (unhidden) {
            return ResponseEntity.ok("Notification ID " + notificationId + " unhidden successfully.");
        } else {
            return ResponseEntity.badRequest().body("Notification ID " + notificationId + " was not dismissed or could not be unhidden.");
        }
    }
}