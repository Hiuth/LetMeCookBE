package com.example.letmecookbe.mapper;

import com.example.letmecookbe.dto.request.UserInfoCreationRequest;
import com.example.letmecookbe.dto.request.UserInfoUpdateRequest;
import com.example.letmecookbe.dto.response.UserInfoResponse;
import com.example.letmecookbe.entity.UserInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.ArrayList;


@Mapper(componentModel = "spring")
public interface UserInfoMapper {
    UserInfo toUserInfo(UserInfoCreationRequest request);

    @Mapping(source = "account.id", target = "accountId")
    UserInfoResponse toUserInfoResponse(UserInfo userInfo);

    default void updateUserInfo(UserInfoUpdateRequest request, @MappingTarget UserInfo userInfo) {
        if (request.getSex() != null && !request.getSex().trim().isEmpty()) {
            userInfo.setSex(request.getSex());
        }
        if (request.getAge() > 0) {
            userInfo.setAge(request.getAge());
        }
        if (request.getHeight() > 0) {
            userInfo.setHeight(request.getHeight());
        }
        if (request.getWeight() > 0) {
            userInfo.setWeight(request.getWeight());
        }
        if (request.getDietTypes() != null && !request.getDietTypes().isEmpty()) {
            userInfo.setDietTypes(new ArrayList<>(request.getDietTypes()));
        }
    }
}