package com.taskflow.service.impl;

import com.taskflow.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/** Real email sender. Activate with taskflow.email.provider=smtp and MAIL_* env vars. */
@Service
@ConditionalOnProperty(name = "taskflow.email.provider", havingValue = "smtp")
@RequiredArgsConstructor
public class SmtpEmailService implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendOtp(String email, String otp, String purpose) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Your TaskFlow verification code");
        message.setText("Your TaskFlow verification OTP is " + otp
                + "\n\nThis code expires in a few minutes. If you did not request this, ignore this email.");
        mailSender.send(message);
    }
}
