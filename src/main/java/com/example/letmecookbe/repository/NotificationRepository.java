// src/main/java/com/example/letmecookbe/repository/NotificationRepository.java
package com.example.letmecookbe.repository;
import org.springframework.data.domain.Page;
import com.example.letmecookbe.entity.Account;
import com.example.letmecookbe.entity.Notification;
import com.example.letmecookbe.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {
    // NotificationRepository.java
    @Query("""
    SELECT n.notificationType, COUNT(n)
    FROM Notification n
    WHERE n.dismissed = false
      AND (n.recipientAccount IS NULL OR n.recipientAccount = :user)
    GROUP BY n.notificationType
""")
    List<Object[]> countUnreadNotificationsByType(@Param("user") Account user);





    // Lấy thông báo riêng tư mà người dùng là người nhận, sắp xếp theo thời gian mới nhất
    List<Notification> findByRecipientAccountOrderByDateDescTimeDesc(Account recipientAccount);
    List<Notification> findByRecipientAccountAndDismissedFalse(Account account);

    // Lấy thông báo riêng tư mà người dùng là người gửi và có người nhận cụ thể (không phải public), sắp xếp
    List<Notification> findBySenderAccountAndRecipientAccountIsNotNullOrderByDateDescTimeDesc(Account senderAccount);
    @Query("SELECT n FROM Notification n WHERE (n.recipientAccount = :account OR n.recipientAccount IS NULL) AND n.readStatus = false")
    List<Notification> findByRecipientAccountOrRecipientIsNullAndReadStatusFalse(@Param("account") Account account);
    // Lấy thông báo công khai (không có người nhận cụ thể), sắp xếp theo thời gian mới nhất
    List<Notification> findByRecipientAccountIsNullOrderByDateDescTimeDesc();
    Page <Notification> findByRecipientAccountAndDismissedTrue(Account account, Pageable pageable);
    @Query("SELECT n FROM Notification n WHERE n.recipientAccount = :account OR n.recipientAccount IS NULL")
    Page<Notification> findByRecipientAccountOrRecipientIsNull(Account account, Pageable pageable);
    @Query("SELECT n FROM Notification n WHERE (n.recipientAccount = :account OR n.recipientAccount IS NULL) AND n.dismissed = true")
    Page<Notification> findByRecipientAccountOrRecipientIsNullAndDismissedTrue(Account account, Pageable pageable);
    // Tìm tất cả thông báo riêng tư chưa đọc mà người dùng là người nhận
    List<Notification> findByRecipientAccountAndReadStatusFalse(Account recipientAccount);
    // Xóa tất cả thông báo mà người dùng là người nhận
    void deleteByRecipientAccount(Account recipientAccount);
}