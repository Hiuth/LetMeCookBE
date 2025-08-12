package com.example.letmecookbe.mapper;

import com.example.letmecookbe.dto.request.NotificationRequest;
import com.example.letmecookbe.dto.response.NotificationResponse;
import com.example.letmecookbe.entity.Account;
import com.example.letmecookbe.entity.Notification;
import com.example.letmecookbe.enums.NotificationType;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "date", expression = "java(java.time.LocalDate.now())") // Use fully qualified nam
    @Mapping(target = "time", expression = "java(java.time.LocalTime.now())")
    @Mapping(source = "request.title", target = "title")
    @Mapping(source = "request.message", target = "content")
    @Mapping(source = "request.type", target = "notificationType", qualifiedByName = "mapStringToNotificationType")
    @Mapping(target = "senderAccount", source = "senderAccount")
    @Mapping(target = "recipientAccount", ignore = true)
    @Mapping(target = "readStatus", constant = "false")
    @Mapping(target = "dismissed", constant = "false") // <-- Mặc định là false khi tạo mới thông báo
    Notification toEntity(NotificationRequest request, Account senderAccount);


    @Mapping(source = "id", target = "id")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "content", target = "message")
    @Mapping(source = "notificationType", target = "notificationType")
    @Mapping(source = "senderAccount", target = "senderUsername", qualifiedByName = "mapAccountToUsername")
    @Mapping(source = "recipientAccount", target = "recipientUsername", qualifiedByName = "mapAccountToUsername")
    @Mapping(source = "date", target = "date")
    @Mapping(source = "time", target = "time")
    @Mapping(source = "readStatus", target = "readStatus")
    @Mapping(source = "dismissed", target = "dismissed") // <-- Ánh xạ trực tiếp từ entity sang response
    NotificationResponse toResponse(Notification notification);


    @Named("mapAccountToUsername")
    default String mapAccountToUsername(Account account) {
        return Optional.ofNullable(account)
                .map(Account::getUsername)
                .orElse(null);
    }

    @Named("mapStringToNotificationType")
    default NotificationType mapStringToNotificationType(String type) {
        if (type == null) {
            // Xử lý trường hợp null, có thể trả về null, hoặc một giá trị mặc định,
            // hoặc throw một ngoại lệ khác nếu type không được phép là null.
            // Ví dụ:
            throw new IllegalArgumentException("NotificationType string cannot be null.");
        }
        try {
            // Chuyển đổi chuỗi sang chữ hoa trước khi so khớp với enum
            return NotificationType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Sửa thông báo lỗi để nó liệt kê tất cả các giá trị enum hiện có
            throw new IllegalArgumentException(
                    "Invalid NotificationType string: " + type +
                            ". Must be one of: " + java.util.Arrays.toString(NotificationType.values()), e);
        }
    }

    List<NotificationResponse> toResponseList(List<Notification> notifications);
}