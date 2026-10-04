package com.taskflow.service.impl;

import com.taskflow.dto.*;
import com.taskflow.entity.User;
import com.taskflow.enums.OtpType;
import com.taskflow.enums.Role;
import com.taskflow.exception.ApiException;
import com.taskflow.repository.UserRepository;
import com.taskflow.security.JwtUtil;
import com.taskflow.service.AuditService;
import com.taskflow.service.AuthService;
import com.taskflow.service.OtpService;
import com.taskflow.util.HashUtil;
import com.taskflow.util.MaskingUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final JwtUtil jwtUtil;
    private final MaskingUtil maskingUtil;
    private final HashUtil hashUtil;
    private final AuditService auditService;
    private final com.taskflow.service.SmsService smsService;

    private static final DateTimeFormatter SMS_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", Locale.ENGLISH);

    // ---------------------------------------------------------------- register

    @Override
    @Transactional
    public AuthStageResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ApiException(HttpStatus.CONFLICT, "Username is already taken.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException(HttpStatus.CONFLICT, "Email is already registered.");
        }
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new ApiException(HttpStatus.CONFLICT, "Phone number is already registered.");
        }

        // SECURITY: role is never taken from client input. Public self-registration
        // always creates a TEAM_MEMBER. Manager accounts must be created/approved by
        // an Admin through a separate, authenticated admin-only endpoint; Admin
        // accounts are only created via the DataSeeder bootstrap process.
        User user = User.builder()
                .name(request.getName())
                .username(request.getUsername())
                .email(request.getEmail())
                .emailHash(hashUtil.sha256(request.getEmail()))
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .role(Role.TEAM_MEMBER)
                .enabled(false)
                .emailVerified(false)
                .build();
        user = userRepository.save(user);

        otpService.generateAndSendEmailOtp(user.getId(), user.getEmail(), OtpType.REGISTRATION_EMAIL);
        auditService.log(user.getId(), "REGISTRATION_STARTED", "USER", user.getId().toString(),
                "New registration pending email OTP verification.");

        String tempToken = jwtUtil.generatePreAuthToken(user);
        return new AuthStageResponse("EMAIL_OTP_REQUIRED", tempToken,
                maskingUtil.maskEmail(user.getEmail()),
                "Enter the OTP sent to your email.");
    }

    @Override
    @Transactional
    public AuthStageResponse verifyRegistrationOtp(VerifyOtpRequest request) {
        User user = requirePreAuthUser(request.getTempToken());

        otpService.verify(user.getId(), OtpType.REGISTRATION_EMAIL, request.getOtp());

        user.setEmailVerified(true);
        user.setEnabled(true);
        userRepository.save(user);

        auditService.log(user.getId(), "EMAIL_OTP_VERIFIED", "USER", user.getId().toString(),
                "Registration email OTP verified; account activated.");

        return new AuthStageResponse("REGISTRATION_COMPLETE", null, null,
                "Account activated. You can now log in.");
    }

    // ------------------------------------------------------------------ login

    @Override
    @Transactional
    public AuthStageResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> unauthorized());

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            auditService.log(user.getId(), "LOGIN_FAILED", "USER", user.getId().toString(),
                    "Incorrect password.");
            throw unauthorized();
        }
        if (!user.isEnabled()) {
            throw new ApiException(HttpStatus.FORBIDDEN,
                    "Account is not active. Please complete email verification first.");
        }

        otpService.generateAndSendEmailOtp(user.getId(), user.getEmail(), OtpType.LOGIN_EMAIL);
        auditService.log(user.getId(), "LOGIN_PASSWORD_OK", "USER", user.getId().toString(),
                "Password verified; login email OTP sent.");

        String tempToken = jwtUtil.generatePreAuthToken(user);
        return new AuthStageResponse("EMAIL_OTP_REQUIRED", tempToken,
                maskingUtil.maskEmail(user.getEmail()),
                "OTP sent to " + maskingUtil.maskEmail(user.getEmail()));
    }

    @Override
    @Transactional
    public AuthSuccessResponse verifyLoginOtp(VerifyOtpRequest request, String ipAddress) {
        User user = requirePreAuthUser(request.getTempToken());

        otpService.verify(user.getId(), OtpType.LOGIN_EMAIL, request.getOtp());

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        auditService.log(user.getId(), "LOGIN_SUCCESS", "USER", user.getId().toString(),
                "Login completed from IP " + ipAddress);

        // Fire-and-forget style notification; failures here should not block login.
        try {
            String message = "TaskFlow Login Alert: Your TaskFlow account was successfully logged in at "
                    + LocalDateTime.now().format(SMS_TIME_FORMAT) + ".";
            smsService.sendSms(user.getPhoneNumber(), message);
        } catch (Exception ignored) {
            // SMS delivery failures must never block a successful login.
        }

        String accessToken = jwtUtil.generateAccessToken(user);
        return AuthSuccessResponse.builder()
                .accessToken(accessToken)
                .user(UserResponse.from(user, maskingUtil))
                .build();
    }

    // ----------------------------------------------------------------- resend

    @Override
    @Transactional
    public AuthStageResponse resendOtp(ResendOtpRequest request, String otpTypeHint) {
        User user = requirePreAuthUser(request.getTempToken());
        OtpType type = OtpType.valueOf(otpTypeHint);

        otpService.assertCanResend(user.getId(), type);
        otpService.generateAndSendEmailOtp(user.getId(), user.getEmail(), type);

        String newTempToken = jwtUtil.generatePreAuthToken(user);
        return new AuthStageResponse("EMAIL_OTP_REQUIRED", newTempToken,
                maskingUtil.maskEmail(user.getEmail()), "A new OTP has been sent to your email.");
    }

    // ----------------------------------------------------------------- helpers

    /** Parses a PRE_AUTH token and loads the corresponding user, or rejects the request. */
    private User requirePreAuthUser(String tempToken) {
        Claims claims;
        try {
            claims = jwtUtil.parseClaims(tempToken);
        } catch (JwtException | IllegalArgumentException e) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid or expired session. Please start again.");
        }
        if (!"PRE_AUTH".equals(claims.get("purpose", String.class))) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid or expired session. Please start again.");
        }
        Long userId = claims.get("uid", Number.class).longValue();
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid or expired session."));
    }

    private ApiException unauthorized() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "Invalid username or password.");
    }
}
