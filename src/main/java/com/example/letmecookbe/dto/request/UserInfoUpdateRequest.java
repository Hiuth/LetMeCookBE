package com.example.letmecookbe.dto.request;

import com.example.letmecookbe.enums.DietType;
import jakarta.validation.constraints.AssertTrue;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserInfoUpdateRequest {
    int height;
    int weight;
    String sex;
    int age;
    List<DietType> dietTypes;
}
