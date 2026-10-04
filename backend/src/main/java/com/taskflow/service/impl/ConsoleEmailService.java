package com.taskflow.service.impl;

import com.taskflow.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Default EmailService: logs the OTP instead of sending real email, so the
 * project runs out-of-the-box without SMTP credentials.
 *
 * Swap in a real implementation (e.g. SmtpEmailService using JavaMailSender)
 * by setting taskflow.email.provider=smtp and providing MAIL_* env vars.
 */
@Service
@ConditionalOnProperty(name = "taskflow.email.provider", havingValue = "console", matchIfMissing = true)
@Slf4j
public class ConsoleEmailService implements EmailService {

    @Override
    public void sendOtp(String email, String otp, String purpose) {
        // Never log OTPs in a real production deployment - this stub exists
        // purely so the app is runnable without configuring SMTP first.
        log.info("[DEV EMAIL] To: {} | Purpose: {} | Your TaskFlow verification OTP is {}", email, purpose, otp);
    }
}
