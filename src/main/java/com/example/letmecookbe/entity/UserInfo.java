package com.example.letmecookbe.entity;

import com.example.letmecookbe.enums.DietType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "user_info")
public class UserInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private String id;
    String sex;
    int height;
    int weight;
    int age;
    String avatar;

    @ElementCollection(fetch = FetchType.LAZY)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_diet_types",
            joinColumns = @JoinColumn(name = "user_info_id"),
            foreignKey = @ForeignKey(name = "FK_USER_DIET_TYPES"),
            indexes = @Index(name = "IDX_USER_DIET_TYPES", columnList = "user_info_id"))
    @Column(name = "diet_type",length = 30)
    List<DietType> dietTypes;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    Account account;
}
