package com.example.letmecookbe.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MainCategoryRequest {
    @NotBlank(message = "NOT_NULL")
    @Size(min = 2, message ="CATEGORYNAME_INVALID")
    String categoryName;
}
