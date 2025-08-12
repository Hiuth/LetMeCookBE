package com.example.letmecookbe.dto.response;

import com.example.letmecookbe.enums.NotificationType;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)

public class NotificationResponse {
    String id;
     String title;
     String message; // <-- ĐÃ ĐỔI TỪ 'content' SANG 'message'
    NotificationType notificationType; // <-- GIỮ NGUYÊN KIỂU ENUM
   String senderUsername;
     String recipientUsername; // Có thể null
     LocalDate date;
     LocalTime time;
    private boolean readStatus;
    private boolean dismissed;
}