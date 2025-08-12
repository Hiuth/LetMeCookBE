package com.example.letmecookbe.entity;
import com.example.letmecookbe.enums.CommentStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String commentText;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    Account account;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", referencedColumnName = "id") // RecipeID là khóa ngoại tới Recipe
    Recipe recipe;
    LocalDateTime createdAt;
    int likes;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false) // Đảm bảo cột status không null
    CommentStatus status;

    @ManyToOne
    @JoinColumn(name = "userinfo_id", referencedColumnName = "id")
    UserInfo userInfo;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

}