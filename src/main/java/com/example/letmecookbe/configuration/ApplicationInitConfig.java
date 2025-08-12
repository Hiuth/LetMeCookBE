package com.example.letmecookbe.configuration;

import com.example.letmecookbe.constant.PreDefinedRole;
import com.example.letmecookbe.entity.Account;
import com.example.letmecookbe.entity.Role;
import com.example.letmecookbe.entity.UserInfo; // Import UserInfo entity
import com.example.letmecookbe.repository.AccountRepository;
import com.example.letmecookbe.repository.RoleRepository;
import com.example.letmecookbe.repository.UserInfoRepository; // Import UserInfoRepository
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {

    PasswordEncoder passwordEncoder;
    // Inject UserInfoRepository here
    UserInfoRepository userInfoRepository; // <-- THÊM DÒNG NÀY VÀO constructor

    @NonFinal
    static final String ADMIN_EMAIL = "admin";

    @NonFinal
    static final String ADMIN_PASSWORD = "admin";

    @Bean
        // THÊM UserInfoRepository vào tham số của Bean method
    ApplicationRunner applicationRunner(AccountRepository accountRepository, RoleRepository roleRepository, UserInfoRepository userInfoRepository){
        return args -> {
            if (accountRepository.findAccountByEmail(ADMIN_EMAIL).isEmpty()){
                log.info("Creating default ADMIN account and roles..."); // Thay warn bằng info cho việc khởi tạo

                // Tạo vai trò USER_ROLE
                roleRepository.save(Role.builder()
                        .name(PreDefinedRole.USER_ROLE)
                        .description("User role")
                        .build());

                // Tạo vai trò ADMIN_ROLE
                Role adminRole = roleRepository.save(Role.builder()
                        .name(PreDefinedRole.ADMIN_ROLE)
                        .description("Admin role")
                        .build());

                var roles = new HashSet<Role>();
                roles.add(adminRole);

                // Tạo tài khoản Admin
                Account account  = Account.builder()
                        .email(ADMIN_EMAIL)
                        .username("admin")
                        .password(passwordEncoder.encode(ADMIN_PASSWORD))
                        .roles(roles)
                        .build();

                // Lưu tài khoản Admin
                accountRepository.save(account);

                // --- BẮT ĐẦU PHẦN THÊM MỚI: TẠO USERINFO MẶC ĐỊNH CHO ADMIN ---
                UserInfo adminUserInfo = UserInfo.builder()
                        .account(account) // Liên kết với tài khoản Admin vừa tạo
                        .age(0) // Giá trị mặc định
                        .sex("UNKNOWN") // Giá trị mặc định (hoặc "Other", tùy Enum của bạn)
                        .height(170) // Giá trị mặc định
                        .weight(0) // Giá trị mặc định
                        .avatar("default_admin_avatar.png") // Có thể là URL của avatar mặc định
                        .build();
                userInfoRepository.save(adminUserInfo);

                log.warn("Admin user 'admin' has been created with default password: admin, please change it. Default UserInfo created.");
            } else {
                log.info("Admin account 'admin' already exists. Skipping creation.");
            }
        };
    }
}