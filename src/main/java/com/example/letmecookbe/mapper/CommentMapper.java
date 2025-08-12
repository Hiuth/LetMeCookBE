package com.example.letmecookbe.mapper;
import com.example.letmecookbe.dto.request.CommentRequest;
import com.example.letmecookbe.dto.response.CommentResponse;
import com.example.letmecookbe.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;
@Mapper(componentModel = "spring") // MapStruct mapper, là Spring Component
public interface CommentMapper {
    // Chuyển Comment Entity -> CommentResponse DTO
    @Mapping(source = "account.id", target = "accountId")
    @Mapping(source = "account.username", target = "username")
    @Mapping(source = "recipe.id", target = "recipeId")
    @Mapping(source = "recipe.title", target = "recipeTitle")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "userInfo.avatar", target = "userAvatar")
    @Mapping(source = "recipe.img", target = "recipeImage")
    CommentResponse toCommentResponse(Comment comment);


    Comment toComment(CommentRequest request);

    // Chuyển List<Comment> Entities -> List<CommentResponse> DTOs
    List<CommentResponse> toCommentResponseList(List<Comment> comments);
}