package com.taskflow.service.impl;

import com.taskflow.enums.OtpType;
import com.taskflow.exception.ApiException;
import com.taskflow.security.JwtUtil;
import com.taskflow.service.ManagerVerificationService;
import com.taskflow.service.OtpService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ManagerVerificationServiceImpl implements ManagerVerificationService {

    private final OtpService otpService;
    private final JwtUtil jwtUtil;

    private static final long ACTION_TOKEN_EXPIRY_MS = 3 * 60 * 1000; // 3 minutes

    @Override
    public void sendPhoneOtp(Long managerUserId, String phoneNumber) {
        otpService.generateAndSendPhoneOtp(managerUserId, phoneNumber, OtpType.MANAGER_PHONE);
    }

    @Override
    public String verifyPhoneOtpAndIssueActionToken(Long managerUserId, String otp) {
        otpService.verify(managerUserId, OtpType.MANAGER_PHONE, otp);
        return jwtUtil.generateGenericToken(
                Map.of("managerId", managerUserId),
                "MANAGER_ACTION_GRANT",
                ACTION_TOKEN_EXPIRY_MS);
    }

    @Override
    public void assertValidActionToken(Long managerUserId, String actionToken) {
        if (actionToken == null || actionToken.isBlank()) {
            throw new ApiException(HttpStatus.FORBIDDEN,
                    "Phone OTP verification is required before this action.");
        }
        Claims claims;
        try {
            claims = jwtUtil.parseClaims(actionToken);
        } catch (JwtException | IllegalArgumentException e) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Phone verification expired. Please verify again.");
        }
        if (!"MANAGER_ACTION_GRANT".equals(claims.get("purpose", String.class))) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Phone verification expired. Please verify again.");
        }
        Long tokenManagerId = claims.get("managerId", Number.class).longValue();
        if (!tokenManagerId.equals(managerUserId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Phone verification expired. Please verify again.");
        }
    }
}
