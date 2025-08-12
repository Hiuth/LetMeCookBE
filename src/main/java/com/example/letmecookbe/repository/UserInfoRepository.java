package com.example.letmecookbe.repository;

import com.example.letmecookbe.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, String> {
    Optional<UserInfo> findByAccountId(String accountId);

    @Query("SELECT u FROM UserInfo u JOIN u.account a WHERE a.username LIKE %:keyword%")
    List<UserInfo> searchByUsername(String keyword);

    @Query("SELECT u FROM UserInfo u JOIN u.account a WHERE a.email = :email")
    UserInfo findByEmail(String email);

}