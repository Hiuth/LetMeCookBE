package com.example.letmecookbe.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.domain.Pageable;
import com.example.letmecookbe.entity.Account;
import com.example.letmecookbe.entity.Notification;
import com.example.letmecookbe.dto.request.NotificationRequest;
import com.example.letmecookbe.dto.response.NotificationResponse;
import com.example.letmecookbe.enums.NotificationStatus;
import com.example.letmecookbe.enums.NotificationType;
import com.example.letmecookbe.mapper.NotificationMapper;
import com.example.letmecookbe.repository.AccountRepository;
import com.example.letmecookbe.repository.NotificationRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.letmecookbe.exception.AppException;
import com.example.letmecookbe.exception.ErrorCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationService {
        AuthService authService;
     SimpMessagingTemplate messagingTemplate;
     NotificationRepository notificationRepository;
     AccountRepository accountRepository;
     NotificationMapper notificationMapper;
// --- CÁC PHƯƠNG THỨC TEST DÙNG CHO ADMIN (TẠO DỮ LIỆU) ---

    /**
     * Gửi thông báo công khai. Chỉ ADMIN có quyền 'NOTIFICATION_CREATE_PUBLIC' mới được phép.
     *
     * @param requestDTO DTO chứa thông tin thông báo (title, message, type).
     * @return {@link NotificationResponse} của thông báo đã gửi.
     */
    @Transactional
    @PreAuthorize("hasAuthority('NOTIFICATION_CREATE_PUBLIC')")
    public NotificationResponse sendPublicNotificationTest(NotificationRequest requestDTO) {
        // Lấy người gửi (Admin hoặc người đang đăng nhập)
        Account senderAccount = getCurrentUserAccount();
        if (senderAccount == null) {
            throw new SecurityException("No authenticated user found to send public notification");
        }
        log.info("Sending public notification from admin: {}", senderAccount.getUsername());

        // Tạo thông báo công khai, không gán recipientAccount cụ thể
        Notification notification = notificationMapper.toEntity(requestDTO, senderAccount);
        notification.setRecipientAccount(null); // Thông báo công khai cho tất cả
        notification.setNotificationType(NotificationType.PUBLIC); // Sử dụng enum NotificationType
        // Xử lý setRead (nếu có phương thức khác)
        notification.setReadStatus(false);
        notification.setDismissed(false);
        Notification savedNotification = notificationRepository.save(notification);
        NotificationResponse responseDTO = notificationMapper.toResponse(savedNotification);

        // Gửi thông báo qua WebSocket cho tất cả người dùng
        messagingTemplate.convertAndSend("/topic/notifications", responseDTO);
        log.info("Sent public notification: {}", responseDTO.getTitle());

        return responseDTO;
    }

    /**
     * Gửi thông báo riêng tư cho một người dùng cụ thể. Chỉ ADMIN có quyền 'NOTIFICATION_CREATE_PRIVATE' mới được phép.
     *
     * @param recipientUsername Username của người nhận thông báo.
     * @param requestDTO        DTO chứa thông tin thông báo (title, message, type).
     * @return {@link NotificationResponse} của thông báo đã gửi.
     * @throws AppException Nếu người nhận không tồn tại (ErrorCode.ACCOUNT_NOT_FOUND).
     */
    @Transactional
    @PreAuthorize("hasAuthority('NOTIFICATION_CREATE_PRIVATE')")
    public NotificationResponse sendPrivateNotificationTest(String recipientUsername, NotificationRequest requestDTO) {
        String trimmedRecipientUsername = recipientUsername != null ? recipientUsername.trim() : null;
        log.info("DEBUG: Attempting to send private notification to recipient username: '{}'", trimmedRecipientUsername);

        Account recipientAccount = accountRepository.findByUsername(trimmedRecipientUsername)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

        Account senderAccount = getCurrentUserAccount();
        log.info("Sending private notification from admin ({}) to recipient: {}", senderAccount.getUsername(), recipientAccount.getUsername());

        Notification notification = notificationMapper.toEntity(requestDTO, senderAccount);
        notification.setRecipientAccount(recipientAccount);
        Notification savedNotification = notificationRepository.save(notification);
        NotificationResponse responseDTO = notificationMapper.toResponse(savedNotification);

        messagingTemplate.convertAndSendToUser(
                responseDTO.getRecipientUsername(),
                "/queue/notifications",
                responseDTO
        );
        log.info("Sent private notification to {}: {}", responseDTO.getRecipientUsername(), responseDTO.getTitle());
        return responseDTO;
    }

// --- CÁC PHƯƠNG THỨC NGHIỆP VỤ CHÍNH ---

    /**
     * Phương thức nội bộ để tạo và gửi thông báo từ hệ thống hoặc các service backend khác.
     * KHÔNG NÊN ĐƯỢC GỌI TRỰC TIẾP TỪ CONTROLLER BỞI NGƯỜI DÙNG.
     *
     * @param requestDTO    DTO chứa thông tin thông báo (title, message, type, recipientUsername).
     * @param senderAccount {@link Account} của người gửi (có thể là null nếu từ hệ thống).
     * @return {@link NotificationResponse} của thông báo đã tạo.
     * @throws AppException Nếu người nhận không tồn tại (ErrorCode.ACCOUNT_NOT_FOUND).
     */
    @Transactional
    public NotificationResponse createAndSendNotificationInternal(NotificationRequest requestDTO, Account senderAccount) {
        Notification notification = notificationMapper.toEntity(requestDTO, senderAccount);

        String recipientUsername = requestDTO.getRecipientUsername() != null ? requestDTO.getRecipientUsername().trim() : null;

        if (recipientUsername == null || recipientUsername.isEmpty()) {
            notification.setRecipientAccount(null); // Thông báo công khai
            log.info("Creating system/public notification: {}", notification.getTitle());
        } else {
            Account recipientAccount = accountRepository.findByUsername(recipientUsername)
                    .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
            notification.setRecipientAccount(recipientAccount);
            log.info("Creating private notification for {}: {}", recipientUsername, notification.getTitle());
        }

        Notification savedNotification = notificationRepository.save(notification);
        NotificationResponse responseDTO = notificationMapper.toResponse(savedNotification);

        // Gửi qua WebSocket
        if (responseDTO.getRecipientUsername() != null && !responseDTO.getRecipientUsername().isEmpty()) {
            // Gửi riêng tư đến người dùng cụ thể
            messagingTemplate.convertAndSendToUser(
                    responseDTO.getRecipientUsername(),
                    "/queue/notifications", // Kênh riêng tư
                    responseDTO
            );
            log.info("Sent private notification internally to {}: {}", responseDTO.getRecipientUsername(), responseDTO.getTitle());
        } else {
            // Gửi công khai đến tất cả người dùng
            messagingTemplate.convertAndSend("/topic/notifications", responseDTO); // Kênh công khai
            log.info("Sent public notification internally: {}", responseDTO.getTitle());
        }
        return responseDTO;
    }
    public List<NotificationResponse> getNotificationsByUsername(String username) {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

        List<Notification> all = new ArrayList<>();
        all.addAll(notificationRepository.findByRecipientAccountOrderByDateDescTimeDesc(account));
        all.addAll(notificationRepository.findBySenderAccountAndRecipientAccountIsNotNullOrderByDateDescTimeDesc(account));
        all.addAll(notificationRepository.findByRecipientAccountIsNullOrderByDateDescTimeDesc());

        all.sort(Comparator
                .comparing(Notification::getDate, Comparator.reverseOrder())
                .thenComparing(Notification::getTime, Comparator.reverseOrder()));

        List<Notification> visible = all.stream().filter(n -> !n.isDismissed()).toList();
        return notificationMapper.toResponseList(visible);
    }

    /**
     * Lấy tất cả thông báo (riêng tư và công khai) cho người dùng hiện tại (đang đăng nhập)
     * và lọc ra các thông báo chưa bị ẩn (dismissed = false).
     * Yêu cầu quyền 'NOTIFICATION_READ' hoặc 'ADMIN_VIEW_ALL_NOTIFICATIONS'.
     *
     * @return Danh sách {@link NotificationResponse} của các thông báo chưa bị ẩn.
     * @throws AppException Nếu tài khoản người dùng hiện tại không tồn tại (ErrorCode.ACCOUNT_NOT_FOUND).
     */
    public Page<NotificationResponse> getNotificationsForUser(int page, int size) {
        Account userAccount = getCurrentUserAccount();
        String emailOfCurrentUser = userAccount.getEmail();
        log.info("DEBUG: Fetching notifications for current authenticated user with email: '{}'", emailOfCurrentUser);

        List<Notification> allNotifications = new ArrayList<>();

        // 1. Lấy thông báo riêng tư mà user là người nhận (Inbox)
        List<Notification> receivedPrivateNotifications =
                notificationRepository.findByRecipientAccountOrderByDateDescTimeDesc(userAccount);
        log.info("Found {} private notifications received by user: {}", receivedPrivateNotifications.size(), emailOfCurrentUser);
        allNotifications.addAll(receivedPrivateNotifications);

        // 2. Lấy tất cả thông báo công khai
        List<Notification> publicNotifications =
                notificationRepository.findByRecipientAccountIsNullOrderByDateDescTimeDesc();
        log.info("Found {} public notifications.", publicNotifications.size());
        allNotifications.addAll(publicNotifications);

        // Sắp xếp và lọc như cũ...
        allNotifications.sort(Comparator
                .comparing(Notification::getDate, Comparator.reverseOrder())
                .thenComparing(Notification::getTime, Comparator.reverseOrder())
        );

        List<Notification> visibleNotifications = allNotifications.stream()
                .filter(notification -> !notification.isDismissed())
                .collect(Collectors.toList());

        int start = page * size;
        int end = Math.min(start + size, visibleNotifications.size());

        if (start > visibleNotifications.size()) {
            log.warn("Page {} vượt quá tổng số thông báo hiển thị. Trả về danh sách rỗng.", page);
            return new PageImpl<>(Collections.emptyList(), PageRequest.of(page, size), visibleNotifications.size());
        }
        List<Notification> pagedNotifications = visibleNotifications.subList(start, end);
        List<NotificationResponse> pagedResponse = notificationMapper.toResponseList(pagedNotifications);
        return new PageImpl<>(pagedResponse, PageRequest.of(page, size), visibleNotifications.size());
    }
    @PreAuthorize("hasAuthority('NOTIFICATION_READ')")
    public Page<NotificationResponse> getDismissedNotifications(int page, int size) {
        Account currentUser = authService.getCurrentAccount();
        log.info("📌 [DEBUG] getDismissedNotifications - current user ID: {}, email: {}", currentUser.getId(), currentUser.getEmail());

        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending().and(Sort.by("time").descending()));

        Page<Notification> pageData = notificationRepository
                .findByRecipientAccountAndDismissedTrue(currentUser, pageable);
        return pageData.map(notificationMapper::toResponse);
    }


    /**
     * Đánh dấu một thông báo cụ thể là đã đọc.
     * Yêu cầu quyền 'NOTIFICATION_UPDATE_STATUS' và người dùng phải là chủ sở hữu của thông báo đó.
     */
    @Transactional
    @PreAuthorize("hasAuthority('NOTIFICATION_UPDATE_STATUS') and @notificationService.isOwner(#notificationId, authentication.name)")
    public boolean markNotificationAsRead(String notificationId) {
        log.info("Attempting to mark notification ID {} as read by user: {}", notificationId, SecurityContextHolder.getContext().getAuthentication().getName());
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_EXISTED));

        if (!notification.isReadStatus()) {
            notification.setReadStatus(true);
            notificationRepository.save(notification);
            log.info("Notification ID {} marked as read successfully.", notificationId);
            return true;
        }
        log.info("Notification ID {} was already read.", notificationId);
        return false;
    }

    /**
     * Đánh dấu một thông báo cụ thể là đã ẩn (dismissed).
     * Yêu cầu quyền 'NOTIFICATION_UPDATE_STATUS' và người dùng phải là chủ sở hữu của thông báo đó.
     */
    @Transactional
    @PreAuthorize("hasAuthority('NOTIFICATION_UPDATE_STATUS') and @notificationService.isOwner(#notificationId, authentication.name)")
    public boolean dismissNotification(String notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_EXISTED));

        if (notification.isDismissed()) {
            log.warn("⚠️ Notification {} already dismissed", notificationId);
            return false;
        }

        notification.setDismissed(true);
        notificationRepository.save(notification);
        log.info("🔕 Notification {} dismissed", notificationId);
        return true;
    }


    /**
     * Đánh dấu tất cả thông báo chưa đọc của người dùng hiện tại là đã đọc.
     * Yêu cầu quyền 'NOTIFICATION_UPDATE_STATUS' và người dùng phải là chủ tài khoản
     */
    @Transactional
    @PreAuthorize("hasAuthority('NOTIFICATION_UPDATE_STATUS') and #userIdentifier == authentication.name")
    public void markAllNotificationsAsRead(String userIdentifier) {
        String trimmedIdentifier = userIdentifier != null ? userIdentifier.trim() : null;
        log.info("📌 [DEBUG] Attempting to mark all notifications as read for user identifier: '{}'", trimmedIdentifier);

        if (trimmedIdentifier == null) {
            log.warn("📌 [DEBUG] User identifier is null, aborting mark all as read");
            throw new IllegalArgumentException("User identifier cannot be null");
        }

        Account userAccount = accountRepository.findAccountByEmail(trimmedIdentifier)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

        // Lấy cả thông báo riêng (recipientAccount = userAccount) và công khai (recipientAccount = null)
        List<Notification> unreadNotifications = notificationRepository.findByRecipientAccountOrRecipientIsNullAndReadStatusFalse(userAccount);

        if (unreadNotifications.isEmpty()) {
            log.info("📌 [DEBUG] No unread notifications found for user: {}", trimmedIdentifier);
            return;
        }

        unreadNotifications.forEach(n -> n.setReadStatus(true));
        notificationRepository.saveAll(unreadNotifications);
        log.info("📌 [DEBUG] Marked {} unread notifications as read for user: {}", unreadNotifications.size(), trimmedIdentifier);
    }

    /**
     * Xóa một thông báo cụ thể.
     * Yêu cầu quyền 'NOTIFICATION_DELETE' và người dùng phải là chủ sở hữu của thông báo đó
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN')") // CHỈ ADMIN MỚI ĐƯỢC XÓA TỪNG THÔNG BÁO
    public boolean deleteNotification(String notificationId) {
        log.info("Attempting to delete notification ID {} by Admin: {}", notificationId, SecurityContextHolder.getContext().getAuthentication().getName());
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_EXISTED));
        notificationRepository.delete(notification);
        log.info("Notification ID {} deleted successfully by Admin.", notificationId);
        return true;
    }

    /**
     * Xóa tất cả thông báo của người dùng hiện tại.
     * Yêu cầu quyền 'NOTIFICATION_DELETE' và người dùng phải là chủ tài khoản.
     *
     * @param username Tên người dùng (phải là của người dùng đang đăng nhập).
     * @throws AppException Nếu tài khoản không tồn tại (ErrorCode.ACCOUNT_NOT_FOUND).
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteAllNotificationsForUser(String username) {
        String trimmedUsername = username != null ? username.trim() : null;
        log.info("Attempting to delete all notifications for user: '{}' by Admin: {}", trimmedUsername, SecurityContextHolder.getContext().getAuthentication().getName());

        Account userAccount = accountRepository.findByUsername(trimmedUsername)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
        notificationRepository.deleteByRecipientAccount(userAccount);
        log.info("All notifications deleted for user: {} by Admin.", trimmedUsername);
    }

// --- PHƯƠNG THỨC TRỢ GIÚP VÀ PHÂN QUYỀN NỘI BỘ ---

    /**
     * Phương thức trợ giúp để lấy {@link Account} của người dùng hiện đang đăng nhập từ SecurityContextHolder.
     * Thường được gọi nội bộ bởi các service.
     *
     * @return {@link Account} của người dùng đang đăng nhập.
     * @throws AppException Nếu tài khoản không được tìm thấy (ErrorCode.ACCOUNT_NOT_FOUND).
     */
    private Account getCurrentUserAccount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentEmail = authentication.getName(); // Lấy email từ authentication.getName()
        String trimmedCurrentEmail = currentEmail.trim();

        log.info("DEBUG: getCurrentUserAccount - Email from token (authentication.getName()): '{}'", trimmedCurrentEmail);

        // Sử dụng findByEmail thay vì findByUsername
        return accountRepository.findAccountByEmail(trimmedCurrentEmail)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
    }
    public NotificationResponse sendPublicNotificationToAll(NotificationRequest requestDTO) {
        Account senderAccount = getCurrentUserAccount(); // admin hoặc hệ thống
        log.info("📣 Sending public notification from: {}", senderAccount.getUsername());

        List<Account> allUsers = accountRepository.findAll();
        for (Account user : allUsers) {
            Notification notification = Notification.builder()
                    .title(requestDTO.getTitle())
                    .content(requestDTO.getMessage())
                    .notificationType(NotificationType.PUBLIC)
                    .senderAccount(senderAccount)
                    .recipientAccount(user) // 👈 GÁN NGƯỜI NHẬN RÕ RÀNG
                    .readStatus(false)
                    .dismissed(false)
                    .date(LocalDate.now())
                    .time(LocalTime.now())
                    .build();

            notificationRepository.save(notification);

            // Gửi qua WebSocket
            messagingTemplate.convertAndSendToUser(
                    user.getUsername(),
                    "/queue/notifications",
                    notificationMapper.toResponse(notification)
            );
        }

        return NotificationResponse.builder()
                .title(requestDTO.getTitle())
                .message("thông báo công khai đến tất cả người dùng.")
                .notificationType(NotificationType.PUBLIC)
                .build();
    }

    /**
     * Kiểm tra xem người dùng có phải là chủ sở hữu của thông báo hoặc có quyền admin để thao tác không.
     * Được gọi bởi @PreAuthorize.
     *
     * @param notificationId ID của thông báo.
     * @return {@code true} nếu người dùng là chủ sở hữu hoặc admin, {@code false} nếu không.
     */
    public boolean isOwner(String notificationId, String userIdentifier) {
        String trimmedIdentifier = userIdentifier != null ? userIdentifier.trim() : null;
        log.info("DEBUG: Checking ownership for notification ID {} by user identifier: '{}'", notificationId, trimmedIdentifier);

        Optional<Account> userAccountOpt = accountRepository.findAccountByEmail(trimmedIdentifier);
        if (userAccountOpt.isEmpty()) {
            log.warn("isOwner check failed: User account '{}' not found.", trimmedIdentifier);
            return false;
        }
        Account userAccount = userAccountOpt.get();

        boolean isAdmin = userAccount.getRoles().stream().anyMatch(role -> role.getName().equals("ADMIN"));
        log.info("DEBUG: User '{}' is Admin: {}", userAccount.getUsername(), isAdmin);

        Optional<Notification> notificationOpt = notificationRepository.findById(String.valueOf(notificationId));
        if (notificationOpt.isEmpty()) {
            log.warn("isOwner check failed: Notification ID {} not found.", notificationId);
            return false;
        }
        Notification notification = notificationOpt.get();

        // If ADMIN, always true
        if (isAdmin) {
            log.info("isOwner check passed: User '{}' is ADMIN. Allowing access to notification ID {}.", userAccount.getUsername(), notificationId);
            return true;
        }

        // For private notifications, user must be the recipient
        if (notification.getRecipientAccount() != null) {
            boolean isRecipient = notification.getRecipientAccount().getId().equals(userAccount.getId());
            log.info("isOwner check for private notification (ID {}) to recipient '{}' by user '{}': isRecipient={}",
                    notificationId, notification.getRecipientAccount().getUsername(), userAccount.getUsername(), isRecipient);
            return isRecipient;
        } else {
            // This is a PUBLIC notification (recipientAccount is null)
            // Allowing access here because the user has NOTIFICATION_UPDATE_STATUS
            // and this is a public notification they are trying to read/update.
            log.info("isOwner check for public notification (ID {}). Allowing non-admin user '{}' with NOTIFICATION_UPDATE_STATUS to interact.", notificationId, userAccount.getUsername());
            return true;
        }
    }
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Integer> getNotificationStats() {
        Account currentUser = authService.getCurrentAccount();
        List<Object[]> result = notificationRepository.countUnreadNotificationsByType(currentUser);

        Map<String, Integer> stats = new HashMap<>();
        for (Object[] row : result) {
            NotificationType type = (NotificationType) row[0];
            Long count = (Long) row[1];
            stats.put(type.name(), count.intValue());
        }
        System.out.println("🚀 Push WebSocket với stats: " + stats);
        // ✅ Gửi trực tiếp lên WebSocket (broadcast đến tất cả admin đang mở dashboard)
        messagingTemplate.convertAndSend("/topic/notifications", stats);

        return stats;
    }



    @Transactional
    @PreAuthorize("hasAuthority('NOTIFICATION_UPDATE_STATUS') and @notificationService.isOwner(#notificationId, authentication.name)")
    public boolean unhideNotification(String notificationId) {
        log.info("Attempting to unhide notification ID {} by user: {}", notificationId, SecurityContextHolder.getContext().getAuthentication().getName());
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_EXISTED));

        if (notification.isDismissed()) {
            notification.setDismissed(false);
            Notification savedNotification = notificationRepository.save(notification);
            log.info("Notification ID {} unhidden successfully.", notificationId);

            NotificationResponse responseDTO = notificationMapper.toResponse(savedNotification);
            if (responseDTO.getRecipientUsername() != null && !responseDTO.getRecipientUsername().isEmpty()) {
                messagingTemplate.convertAndSendToUser(
                        responseDTO.getRecipientUsername(),
                        "/queue/notifications",
                        responseDTO
                );
                log.info("Sent unhide notification to {}: {}", responseDTO.getRecipientUsername(), responseDTO.getTitle());
            } else {
                messagingTemplate.convertAndSend("/topic/notifications", responseDTO);
                log.info("Sent unhide public notification: {}", responseDTO.getTitle());
            }
            return true;
        }
        log.info("Notification ID {} was not dismissed.", notificationId);
        return false;
    }
    @Transactional
    public void createTypedNotification(Account sender, Account recipient, NotificationType type, String title, String message) {
        Account effectiveRecipient = recipient != null ? recipient : getCurrentUserAccount();
        if (effectiveRecipient == null) {
            throw new AppException(ErrorCode.ACCOUNT_NOT_FOUND);
        }

        Notification notification = new Notification();
        notification.setSenderAccount(sender);
        notification.setRecipientAccount(effectiveRecipient);
        notification.setNotificationType(type);
        notification.setTitle(title);
        notification.setContent(message);
        notification.setReadStatus(false);
        notification.setDismissed(false);
        LocalDate nowDate = LocalDate.now();
        LocalTime nowTime = LocalTime.now();
        notification.setDate(nowDate);
        notification.setTime(nowTime);
        Notification savedNotification = notificationRepository.save(notification);
        log.info("✅ Saved notification for recipient: email={}, username={}", effectiveRecipient.getEmail(), effectiveRecipient.getUsername());

        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("id", savedNotification.getId());
        notificationData.put("type", type.name());
        notificationData.put("title", title);
        notificationData.put("message", message);
        notificationData.put("recipientId", effectiveRecipient.getId()); // Thêm recipientId
        notificationData.put("recipientUsername", effectiveRecipient.getUsername()); // Giữ username
        notificationData.put("recipientEmail", effectiveRecipient.getEmail()); // Thêm email
        notificationData.put("timestamp", System.currentTimeMillis());
        notificationData.put("senderUsername", sender != null ? sender.getUsername() : null);

        messagingTemplate.convertAndSend("/topic/notifications", notificationData);
        messagingTemplate.convertAndSendToUser(
                effectiveRecipient.getEmail(), // Sử dụng email cho WebSocket
                "/queue/notifications",
                notificationData
        );
        log.info("📢 Pushed notification to /topic/notifications for type: {}", type.name());
    }

    }





