package com.taskflow.service.impl;

import com.taskflow.dto.AuthStageResponse;
import com.taskflow.entity.User;
import com.taskflow.enums.OtpType;
import com.taskflow.enums.Role;
import com.taskflow.exception.ApiException;
import com.taskflow.repository.UserRepository;
import com.taskflow.security.JwtUtil;
import com.taskflow.service.AdminAuthorizationService;
import com.taskflow.service.AuditService;
import com.taskflow.service.OtpService;
import com.taskflow.util.MaskingUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Manager -> Admin dual-OTP authorization handshake for database-adjacent /
 * highly sensitive operations (section 11 of the spec).
 *
 * Flow:
 *   1) requestAuthorization: pick an Admin, send them an EMAIL otp, return a
 *      stage-1 token.
 *   2) verifyAdminEmailOtp: consume stage-1 token + email otp, send the same
 *      Admin a PHONE otp, return a stage-2 token.
 *   3) verifyAdminPhoneOtp: consume stage-2 token + phone otp, return a
 *      short-lived "authorization grant" token the Manager's original request
 *      can be retried with.
 *
 * NOTE: for simplicity this scaffold authorizes against the first ADMIN
 * account found. In production you'd likely notify all admins and let any
 * one of them approve, or route to a specific on-call admin.
 */
@Service
@RequiredArgsConstructor
public class AdminAuthorizationServiceImpl implements AdminAuthorizationService {

    private final UserRepository userRepository;
    private final OtpService otpService;
    private final JwtUtil jwtUtil;
    private final MaskingUtil maskingUtil;
    private final AuditService auditService;

    private static final long STAGE_TOKEN_EXPIRY_MS = 5 * 60 * 1000;
    private static final long GRANT_TOKEN_EXPIRY_MS = 2 * 60 * 1000;

    @Override
    public AuthStageResponse requestAuthorization(Long managerId, String operationDescription) {
        User admin = firstAdmin();

        otpService.generateAndSendEmailOtp(admin.getId(), admin.getEmail(), OtpType.ADMIN_EMAIL);
        auditService.log(managerId, "ADMIN_AUTHORIZATION_REQUESTED", "ADMIN_AUTH", admin.getId().toString(),
                "Manager requested admin authorization for: " + operationDescription);

        String token = jwtUtil.generateGenericToken(
                Map.of("managerId", managerId, "adminId", admin.getId(), "op", operationDescription),
                "ADMIN_AUTH_STAGE1", STAGE_TOKEN_EXPIRY_MS);

        return new AuthStageResponse("ADMIN_EMAIL_OTP_REQUIRED", token,
                maskingUtil.maskEmail(admin.getEmail()),
                "Admin Authorization Required. Email OTP sent to admin.");
    }

    @Override
    public AuthStageResponse verifyAdminEmailOtp(String authorizationToken, String otp) {
        Claims claims = parse(authorizationToken, "ADMIN_AUTH_STAGE1");
        Long adminId = claims.get("adminId", Number.class).longValue();
        Long managerId = claims.get("managerId", Number.class).longValue();
        String op = claims.get("op", String.class);

        otpService.verify(adminId, OtpType.ADMIN_EMAIL, otp);

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Admin account not found."));

        otpService.generateAndSendPhoneOtp(admin.getId(), admin.getPhoneNumber(), OtpType.ADMIN_PHONE);
        auditService.log(managerId, "ADMIN_AUTHORIZATION_EMAIL_VERIFIED", "ADMIN_AUTH", adminId.toString(),
                "Admin email OTP verified for: " + op);

        String token = jwtUtil.generateGenericToken(
                Map.of("managerId", managerId, "adminId", adminId, "op", op),
                "ADMIN_AUTH_STAGE2", STAGE_TOKEN_EXPIRY_MS);

        return new AuthStageResponse("ADMIN_PHONE_OTP_REQUIRED", token, null,
                "Admin email verified. Phone OTP sent to admin.");
    }

    @Override
    public String verifyAdminPhoneOtp(String authorizationToken, String otp) {
        Claims claims = parse(authorizationToken, "ADMIN_AUTH_STAGE2");
        Long adminId = claims.get("adminId", Number.class).longValue();
        Long managerId = claims.get("managerId", Number.class).longValue();
        String op = claims.get("op", String.class);

        otpService.verify(adminId, OtpType.ADMIN_PHONE, otp);

        auditService.log(managerId, "ADMIN_AUTHORIZATION_APPROVED", "ADMIN_AUTH", adminId.toString(),
                "Both admin factors verified for: " + op + ". Temporary authorization granted.");

        return jwtUtil.generateGenericToken(
                Map.of("managerId", managerId, "adminId", adminId, "op", op),
                "ADMIN_AUTH_GRANTED", GRANT_TOKEN_EXPIRY_MS);
    }

    private User firstAdmin() {
        List<User> admins = userRepository.findByRole(Role.ADMIN);
        if (admins.isEmpty()) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No Admin account is available to authorize this operation.");
        }
        return admins.get(0);
    }

    private Claims parse(String token, String expectedPurpose) {
        Claims claims;
        try {
            claims = jwtUtil.parseClaims(token);
        } catch (JwtException | IllegalArgumentException e) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid or expired authorization session.");
        }
        if (!expectedPurpose.equals(claims.get("purpose", String.class))) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid or expired authorization session.");
        }
        return claims;
    }
}
