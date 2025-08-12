package com.example.letmecookbe.controller;

import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.example.letmecookbe.dto.request.LikeCommentRequest;
import com.example.letmecookbe.dto.response.ApiResponse;
import com.example.letmecookbe.dto.response.LikeCommentResponse;
import com.example.letmecookbe.service.LikeCommentService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/likeComment")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LikeCommentController {
    LikeCommentService likeCommentService;

    @PostMapping("/create/{commentId}")
    public ApiResponse<LikeCommentResponse> createLikeComment(@PathVariable String commentId,  @RequestBody @Valid LikeCommentRequest request){
        ApiResponse<LikeCommentResponse> response = new ApiResponse<>();
        response.setMessage("Create Like Comment");
        response.setResult(likeCommentService.createLikeComment(commentId,request));
        return response;
    }

    @GetMapping("/getAll")
    public ApiResponse<List<LikeCommentResponse>> getAllLikeComment(){
        ApiResponse<List<LikeCommentResponse>> response = new ApiResponse<>();
        response.setMessage("Get all like comment by comment id: ");
        response.setResult(likeCommentService.getAllLikeCommentByAccountId());
        return response;
    }

    @DeleteMapping("/dislike/{commentId}")
    public ApiResponse<String> dislikeComment(@PathVariable String commentId){
        ApiResponse<String> response = new ApiResponse<>();
        response.setMessage("Dislike Comment: "+ commentId);
        response.setResult(likeCommentService.disLikeComment(commentId));
        return response;
    }
}
