package com.example.letmecookbe.dto.response;

import com.example.letmecookbe.enums.DietType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class UserInfoResponse {
    String id;
    String sex;
    int height;
    int weight;
    int age;
    String avatar;
    List<DietType> dietTypes;
    String accountId;
}