package com.example.letmecookbe.controller;

import com.example.letmecookbe.dto.request.UserInfoCreationRequest;
import com.example.letmecookbe.dto.request.UserInfoUpdateRequest;
import com.example.letmecookbe.dto.response.ApiResponse;
import com.example.letmecookbe.dto.response.UserInfoResponse;
import com.example.letmecookbe.dto.response.UsernameResponse;
import com.example.letmecookbe.service.UserInfoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/user-info")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class UserInfoController {
    UserInfoService userInfoService;

    @PostMapping
    public ApiResponse<UserInfoResponse> createUserInfo(
            @RequestParam String accountId,
            @RequestBody UserInfoCreationRequest request) {
        UserInfoResponse response = userInfoService.createUserInfo(accountId, request);
        return ApiResponse.<UserInfoResponse>builder()
                .result(response)
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<UserInfoResponse> updateUserInfo(@PathVariable String id, @Valid @RequestBody UserInfoUpdateRequest request) {
        UserInfoResponse result = userInfoService.updateUserInfo(id, request);
        return ApiResponse.<UserInfoResponse>builder()
                .result(result)
                .build();
    }

    @GetMapping("/getAll")
    public ApiResponse<Page<UserInfoResponse>> getAllUserInfo(@PageableDefault(size = 3, page = 0) Pageable pageable) {
        Page<UserInfoResponse> result = userInfoService.getAllUserInfo(pageable);
        return ApiResponse.<Page<UserInfoResponse>>builder()
                .result(result)
                .build();
    }

    @GetMapping
    public ApiResponse<UserInfoResponse> getUserInfo() {
        UserInfoResponse result = userInfoService.getUserInfo();
        return ApiResponse.<UserInfoResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<UserInfoResponse> uploadAvatar(@RequestPart("avatar") MultipartFile avatar) {
        UserInfoResponse result = userInfoService.uploadAvatar(avatar);
        return ApiResponse.<UserInfoResponse>builder()
                .result(result)
                .build();
    }

    @DeleteMapping("/avatar")
    public ApiResponse<UserInfoResponse> deleteAvatar() {
        UserInfoResponse result = userInfoService.deleteAvatar();
        return ApiResponse.<UserInfoResponse>builder()
                .result(result)
                .build();
    }

    @GetMapping("/search")
    public ApiResponse<List<UsernameResponse>> searchByUsername(@RequestParam("keyword") String keyword) {
        List<UsernameResponse> result = userInfoService.searchByUsername(keyword);
        return ApiResponse.<List<UsernameResponse>>builder()
                .result(result)
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteUserInfo(@PathVariable String id) {
        userInfoService.deleteUserInfo(id);
        return ApiResponse.<String>builder()
                .result("User info with id [" + id + "] has been deleted")
                .build();
    }
}