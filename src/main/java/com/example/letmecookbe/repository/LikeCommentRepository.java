package com.example.letmecookbe.repository;

import com.example.letmecookbe.entity.Comment;
import com.example.letmecookbe.entity.LikeComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface LikeCommentRepository extends JpaRepository<LikeComment, String> {
    boolean existsByCommentIdAndAccountId(String commentId, String accountId);

    void deleteByCommentIdAndAccountId(String commentId, String accountId);

    List<LikeComment> findAllByAccountId(String accountId);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM LikeComment lc WHERE lc.comment.id = :commentId")
    void deleteAllByCommentId(String commentId);

    LikeComment findByCommentId(String commentId);

    long countByCommentId(String commentId);

}
