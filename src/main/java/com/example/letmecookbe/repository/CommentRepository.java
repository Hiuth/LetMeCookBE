package com.example.letmecookbe.repository;

import com.example.letmecookbe.entity.Comment;
import com.example.letmecookbe.enums.CommentStatus; // CRUCIAL: Import the enum!
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, String>, JpaSpecificationExecutor<Comment> {
    Page<Comment> findByRecipe_Id(String recipeId, Pageable pageable);
    Page<Comment> findByStatus(CommentStatus status, Pageable pageable); // Use the imported enum!
    Page<Comment> findByStatusAndRecipe_Id(CommentStatus status, String recipeId, Pageable pageable); // Use the imported enum!
    Page<Comment> findByCommentTextContainingIgnoreCase(String commentText, Pageable pageable);
    long countByStatus(CommentStatus status);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.account.id = :accountId")
    int countCommentsByAccountId(String accountId);

    @Modifying
    @Query("DELETE FROM Comment c WHERE c.recipe.id = :recipeId")
    void deleteByRecipeId(String recipeId);

    Page<Comment> findByAccountId(String accountId, Pageable pageable);

    List<Comment> findByRecipeId(String recipeId);
}