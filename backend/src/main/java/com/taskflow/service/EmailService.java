package com.taskflow.service;

public interface EmailService {
    void sendOtp(String email, String otp, String purpose);
}
