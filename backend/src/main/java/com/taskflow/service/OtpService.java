package com.taskflow.service;

import com.taskflow.enums.OtpType;

public interface OtpService {

    /** Generates, hashes and stores a new OTP for the user, and sends it via the given channel. */
    String generateAndSendEmailOtp(Long userId, String email, OtpType type);

    String generateAndSendPhoneOtp(Long userId, String phoneNumber, OtpType type);

    /** Verifies the OTP; throws ApiException on invalid/expired/exhausted attempts. */
    void verify(Long userId, OtpType type, String rawOtp);

    /** Enforces resend cooldown; throws ApiException if called too soon. */
    void assertCanResend(Long userId, OtpType type);
}
