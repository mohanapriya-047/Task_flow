package com.taskflow.service.impl;

import com.taskflow.entity.OtpVerification;
import com.taskflow.enums.OtpType;
import com.taskflow.exception.ApiException;
import com.taskflow.repository.OtpVerificationRepository;
import com.taskflow.service.EmailService;
import com.taskflow.service.OtpService;
import com.taskflow.service.SmsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final OtpVerificationRepository otpRepository;
    private final PasswordEncoder passwordEncoder; // BCrypt is reused for hashing OTPs too
    private final EmailService emailService;
    private final SmsService smsService;

    private static final SecureRandom RANDOM = new SecureRandom();

    @Value("${taskflow.otp.length}")
    private int otpLength;
    @Value("${taskflow.otp.expiry-minutes}")
    private int expiryMinutes;
    @Value("${taskflow.otp.max-attempts}")
    private int maxAttempts;
    @Value("${taskflow.otp.resend-cooldown-seconds}")
    private int resendCooldownSeconds;

    @Override
    public String generateAndSendEmailOtp(Long userId, String email, OtpType type) {
        String otp = generate();
        store(userId, otp, type);
        emailService.sendOtp(email, otp, type.name());
        return otp; // returned only for internal use (e.g. tests) - callers must never expose this to the client
    }

    @Override
    public String generateAndSendPhoneOtp(Long userId, String phoneNumber, OtpType type) {
        String otp = generate();
        store(userId, otp, type);
        String message = switch (type) {
            case MANAGER_PHONE -> "Your TaskFlow Manager verification OTP is " + otp + ". It expires in "
                    + expiryMinutes + " minutes.";
            case ADMIN_PHONE -> "Your TaskFlow Admin authorization OTP is " + otp + ". It expires in "
                    + expiryMinutes + " minutes.";
            default -> "Your TaskFlow verification OTP is " + otp + ".";
        };
        smsService.sendSms(phoneNumber, message);
        return otp;
    }

    @Override
    public void verify(Long userId, OtpType type, String rawOtp) {
        OtpVerification record = otpRepository
                .findFirstByUserIdAndOtpTypeAndVerifiedFalseOrderByCreatedAtDesc(userId, type)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Invalid or expired OTP."));

        if (record.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid or expired OTP.");
        }
        if (record.getAttemptCount() >= maxAttempts) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS,
                    "Maximum OTP attempts exceeded. Please request a new OTP.");
        }

        record.setAttemptCount(record.getAttemptCount() + 1);

        if (!passwordEncoder.matches(rawOtp, record.getOtpHash())) {
            otpRepository.save(record);
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid or expired OTP.");
        }

        record.setVerified(true); // one-time use: cannot be replayed
        otpRepository.save(record);
    }

    @Override
    public void assertCanResend(Long userId, OtpType type) {
        long recentCount = otpRepository.countByUserIdAndOtpTypeAndCreatedAtAfter(
                userId, type, LocalDateTime.now().minusSeconds(resendCooldownSeconds));
        if (recentCount > 0) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS,
                    "Please wait before requesting another OTP.");
        }
    }

    private void store(Long userId, String rawOtp, OtpType type) {
        OtpVerification record = OtpVerification.builder()
                .userId(userId)
                .otpHash(passwordEncoder.encode(rawOtp)) // never store the raw OTP
                .otpType(type)
                .expiresAt(LocalDateTime.now().plusMinutes(expiryMinutes))
                .attemptCount(0)
                .verified(false)
                .build();
        otpRepository.save(record);
    }

    private String generate() {
        int bound = (int) Math.pow(10, otpLength);
        int value = RANDOM.nextInt(bound);
        return String.format("%0" + otpLength + "d", value);
    }
}
