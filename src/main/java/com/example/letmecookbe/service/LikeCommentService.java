package com.example.letmecookbe.service;


import com.example.letmecookbe.dto.request.LikeCommentRequest;
import com.example.letmecookbe.dto.response.LikeCommentResponse;
import com.example.letmecookbe.entity.Account;
import com.example.letmecookbe.entity.Comment;
import com.example.letmecookbe.entity.LikeComment;
import com.example.letmecookbe.exception.AppException;
import com.example.letmecookbe.exception.ErrorCode;
import com.example.letmecookbe.mapper.LikeCommentMapper;
import com.example.letmecookbe.repository.AccountRepository;
import com.example.letmecookbe.repository.CommentRepository;
import com.example.letmecookbe.repository.LikeCommentRepository;
import com.example.letmecookbe.repository.RecipeRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LikeCommentService {
    LikeCommentRepository likeCommentRepository;
    LikeCommentMapper likeCommentMapper;
    AccountRepository accountRepository;
    private final CommentRepository commentRepository;

    private String getAccountIdFromContext() {
        var context = SecurityContextHolder.getContext();
        String email = context.getAuthentication().getName();
        Account account = accountRepository.findAccountByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
        return account.getId();
    }

    @PreAuthorize("hasAuthority('LIKE_COMMENT')")
    public LikeCommentResponse createLikeComment(String commentId, LikeCommentRequest request) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new AppException(ErrorCode.COMMENT_NOT_FOUND)
        );
        Account account = accountRepository.findById(getAccountIdFromContext()).orElseThrow(
                () -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND)
        );
        if (likeCommentRepository.existsByCommentIdAndAccountId(commentId,getAccountIdFromContext())) {
            throw new AppException(ErrorCode.LIKE_COMMENT_EXISTED);
        }
        comment.setLikes(comment.getLikes() + 1);
        commentRepository.save(comment);
        LikeComment likeComment = likeCommentMapper.toLikeComment(request);
        likeComment.setAccount(account);
        likeComment.setComment(comment);
        LikeComment savedLikeComment = likeCommentRepository.save(likeComment);
        return likeCommentMapper.toLikeCommentResponse(savedLikeComment);
    }

    @PreAuthorize("hasAuthority('GET_ALL_COMMENT_LIKE_BY_ACCOUNT')")
    public List<LikeCommentResponse> getAllLikeCommentByAccountId(){
        List<LikeComment> likeComments = likeCommentRepository.findAllByAccountId(getAccountIdFromContext());
        return likeComments.stream()
                .map(likeCommentMapper::toLikeCommentResponse)
                .toList();
    }

    @Transactional
    @PreAuthorize("hasAuthority('DISLIKE_COMMENT')")
    public String disLikeComment(String id) {
        Comment comment = commentRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.COMMENT_NOT_FOUND)
        );
        comment.setLikes(comment.getLikes() - 1);
        commentRepository.save(comment);
        likeCommentRepository.deleteByCommentIdAndAccountId(id, getAccountIdFromContext());
        return id;
    }

}