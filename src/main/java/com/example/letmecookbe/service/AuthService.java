package com.example.letmecookbe.service;

import com.example.letmecookbe.dto.request.AuthRequest;
import com.example.letmecookbe.dto.request.IntrospectRequest;
import com.example.letmecookbe.dto.request.LogoutRequest;
import com.example.letmecookbe.dto.request.RefreshRequest;
import com.example.letmecookbe.dto.request.GoogleSignInRequest;
import com.example.letmecookbe.dto.response.AuthResponse;
import com.example.letmecookbe.dto.response.IntrospectResponse;
import com.example.letmecookbe.entity.Account;
import com.example.letmecookbe.entity.InvalidatedToken;
import com.example.letmecookbe.enums.AccountStatus;
import com.example.letmecookbe.exception.AppException;
import com.example.letmecookbe.exception.ErrorCode;
import com.example.letmecookbe.repository.AccountRepository;
import com.example.letmecookbe.repository.InvalidatedTokenRepository;
import com.example.letmecookbe.repository.UserInfoRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthService {
    AccountRepository accountRepository;
    InvalidatedTokenRepository invalidatedTokenRepository;
    UserInfoRepository userInfoRepository;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refresh-duration}")
    protected long REFRESH_DURATION;

    @NonFinal
    @Value("${google.client-id}")
    protected String GOOGLE_CLIENT_ID;

    public IntrospectResponse introspect(IntrospectRequest request)
            throws JOSEException, ParseException {
        var token = request.getToken();
        boolean isValid = true;

        try {
            verifyToken(token, false);
        } catch (AppException e) {
            isValid = false;
        }

        return IntrospectResponse.builder()
                .valid(isValid)
                .build();
    }

    //  NEW METHOD - Authenticate without UserInfo check
    public AuthResponse authenticateForSetup(AuthRequest request) {
        String email = request.getEmail().trim();
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        var account = accountRepository.findAccountByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_EXISTED));

        // Kiểm tra trạng thái tài khoản
        if (account.getStatus() == AccountStatus.BANNED) {
            if (account.getBanEndDate() != null) {
                if (account.getBanEndDate().isBefore(LocalDateTime.now())) {
                    account.setStatus(AccountStatus.ACTIVE);
                    account.setBanEndDate(null);
                    accountRepository.save(account);
                } else {
                    throw new AppException(ErrorCode.ACCOUNT_BANNED);
                }
            } else {
                account.setStatus(AccountStatus.ACTIVE);
                accountRepository.save(account);
            }
        }

        //  SKIP UserInfo check for setup flow

        boolean authenticated = passwordEncoder.matches(request.getPassword(), account.getPassword());

        if (!authenticated) {
            log.debug("Authentication failed for email [{}]", email);
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        var token = generateSetupToken(account);
        var refreshToken = generateToken(account, true); // Keep existing refresh token

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .authenticated(true)
                .build();
    }

    private String generateSetupToken(Account account) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(account.getEmail())
                .claim("id", account.getId().toString())
                .claim("username", account.getUsername())   // ✅ Giữ dòng này
                .issuer("letmecook.com")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli() // ✅ Bỏ isRefresh
                ))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", "SETUP") // ✅ scope cứng
                .build();



        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create setup token", e);
            throw new RuntimeException(e);
        }
    }



    public AuthResponse authenticate(AuthRequest request) {
        String email = request.getEmail().trim();
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        var account = accountRepository.findAccountByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_EXISTED));

        // Kiểm tra trạng thái tài khoản
        if (account.getStatus() == AccountStatus.BANNED) {
            if (account.getBanEndDate() != null) {
                if (account.getBanEndDate().isBefore(LocalDateTime.now())) {
                    account.setStatus(AccountStatus.ACTIVE);
                    account.setBanEndDate(null);
                    accountRepository.save(account);
                } else {
                    long daysRemaining = ChronoUnit.DAYS.between(LocalDateTime.now(), account.getBanEndDate());
                    throw new AppException(ErrorCode.ACCOUNT_BANNED);
                }
            } else {
                // If banEndDate is null, treat as not banned
                account.setStatus(AccountStatus.ACTIVE);
                accountRepository.save(account);
            }
        }

        // Kiểm tra xem UserInfo đã được tạo chưa
        boolean hasUserInfo = userInfoRepository.findByAccountId(account.getId()).isPresent();
        if (!hasUserInfo) {
            throw new AppException(ErrorCode.USER_INFO_NOT_EXISTED);
        }

        boolean authenticated = passwordEncoder.matches(request.getPassword(), account.getPassword());

        if (!authenticated) {
            log.debug("Authentication failed for email [{}]", email);
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        var token = generateToken(account, false);
        var refreshToken = generateToken(account, true);
        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .authenticated(true)
                .build();
    }
    public Account getCurrentAccount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // ... logic lấy identifier từ authentication ...
        String email = authentication.getName(); // Ví dụ đơn giản, có thể phức tạp hơn với JWT
        return accountRepository.findAccountByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }
    public AuthResponse googleSignIn(GoogleSignInRequest request) throws Exception {
        // Xác minh ID Token từ Google
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(Arrays.asList(
                        "810762213774-9pdqiru4ct3s03r42phjulqqqksf89ru.apps.googleusercontent.com", // Android Client ID
                        "810762213774-j84nchg9c59vpo9p6k7mhn1qidj77cr9.apps.googleusercontent.com" // Web Client ID
                ))
                .build();

        GoogleIdToken googleIdToken = verifier.verify(request.getIdToken());
        if (googleIdToken == null) {
            throw new AppException(ErrorCode.INVALID_GOOGLE_TOKEN);
        }

        GoogleIdToken.Payload payload = googleIdToken.getPayload();
        String email = payload.getEmail();
        String userId = payload.getSubject(); // Google user ID

        // Tìm hoặc tạo tài khoản trong database
        Account account = accountRepository.findAccountByEmail(email)
                .orElseGet(() -> {
                    Account newAccount = new Account();
                    newAccount.setEmail(email);
                    newAccount.setUsername(email.split("@")[0]); // Tạm dùng phần trước @ làm username
                    newAccount.setPassword(""); // Không cần mật khẩu cho đăng nhập Google
                    newAccount.setStatus(AccountStatus.ACTIVE);
                    // Thêm các field khác nếu cần (ví dụ: roles, permissions)
                    return accountRepository.save(newAccount);
                });

        // Kiểm tra trạng thái tài khoản
        if (account.getStatus() == AccountStatus.BANNED) {
            if (account.getBanEndDate() != null && account.getBanEndDate().isBefore(LocalDateTime.now())) {
                account.setStatus(AccountStatus.ACTIVE);
                account.setBanEndDate(null);
                accountRepository.save(account);
            } else if (account.getBanEndDate() != null) {
                throw new AppException(ErrorCode.ACCOUNT_BANNED);
            } else {
                // If banEndDate is null, treat as not banned
                account.setStatus(AccountStatus.ACTIVE);
                accountRepository.save(account);
            }
        }

        // Kiểm tra xem UserInfo đã được tạo chưa
        boolean hasUserInfo = userInfoRepository.findByAccountId(account.getId()).isPresent();
        if (!hasUserInfo) {
            throw new AppException(ErrorCode.USER_INFO_NOT_EXISTED);
        }

        // Tạo JWT token cho người dùng
        String token = generateToken(account, false);

        return AuthResponse.builder()
                .token(token)
                .authenticated(true)
                .build();
    }

    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        try {
            var signToken = verifyToken(request.getToken(), true);

            String jit = signToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

            InvalidatedToken invalidatedToken =
                    InvalidatedToken.builder().id(jit).expiryTime(expiryTime).build();

            invalidatedTokenRepository.save(invalidatedToken);
        } catch (AppException exception) {
            log.info("Token already expired");
        }
    }

    public AuthResponse refreshToken(RefreshRequest request)
            throws ParseException, JOSEException {
        var signedJWT = verifyToken(request.getToken(), true);

        var email = signedJWT.getJWTClaimsSet().getSubject();
        var account = accountRepository.findAccountByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        // Kiểm tra trạng thái tài khoản
        if (account.getStatus() == AccountStatus.BANNED) {
            if (account.getBanEndDate() != null && account.getBanEndDate().isBefore(LocalDateTime.now())) {
                account.setStatus(AccountStatus.ACTIVE);
                account.setBanEndDate(null);
                accountRepository.save(account);
            } else if (account.getBanEndDate() != null) {
                throw new AppException(ErrorCode.ACCOUNT_BANNED);
            } else {
                // If banEndDate is null, treat as not banned
                account.setStatus(AccountStatus.ACTIVE);
                accountRepository.save(account);
            }
        }

        var token = generateToken(account, false);

        // Kiểm tra xem UserInfo đã được tạo chưa
        boolean hasUserInfo = userInfoRepository.findByAccountId(account.getId()).isPresent();
        if (!hasUserInfo) {
            throw new AppException(ErrorCode.USER_INFO_NOT_EXISTED);
        }

        return AuthResponse.builder()
                .token(token)
                .refreshToken(request.getToken())
                .authenticated(true)
                .build();
    }

    private String generateToken(Account account, boolean isRefresh) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(account.getEmail())
                .claim("id", account.getId().toString())
                .claim("username", account.getUsername())
                .issuer("letmecook.com")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(isRefresh ? REFRESH_DURATION : VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()
                ))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", isRefresh ? "REFRESH" : buildScope(account))
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new RuntimeException(e);
        }
    }

    private SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);

        if (!(verified && expiryTime.after(new Date())))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        if (invalidatedTokenRepository
                .existsById(signedJWT.getJWTClaimsSet().getJWTID()))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        if (isRefresh) {
            String scope = signedJWT.getJWTClaimsSet().getStringClaim("scope");
            if (!"REFRESH".equals(scope)) {
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }
        }

        return signedJWT;
    }

    private String buildScope(Account account) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        if (account.getRoles() != null && !account.getRoles().isEmpty()) {
            account.getRoles().forEach(role -> {
                String roleName = role.getName().startsWith("ROLE_")
                        ? role.getName()
                        : "ROLE_" + role.getName();
                stringJoiner.add(roleName);

                if (role.getPermissions() != null && !role.getPermissions().isEmpty()) {
                    role.getPermissions().forEach(permission ->
                            stringJoiner.add(permission.getName()));
                }
            });
        }
        return stringJoiner.toString();
    }


}