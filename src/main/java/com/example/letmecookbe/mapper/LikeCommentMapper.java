package com.example.letmecookbe.mapper;

import com.example.letmecookbe.dto.request.LikeCommentRequest;
import com.example.letmecookbe.dto.response.LikeCommentResponse;
import com.example.letmecookbe.entity.LikeComment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LikeCommentMapper {
    LikeComment toLikeComment(LikeCommentRequest likeCommentRequest);

    @Mapping(source = "comment.id", target = "commentId")
    @Mapping(source = "account.id", target = "accountId")
    @Mapping(source = "account.username",target ="accountName")
    LikeCommentResponse toLikeCommentResponse(LikeComment likeComment);
}


