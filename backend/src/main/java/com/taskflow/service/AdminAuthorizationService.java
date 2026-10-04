package com.taskflow.service;

import com.taskflow.dto.AuthStageResponse;

/**
 * Handles the two-factor (email + phone) Admin authorization flow a Manager
 * must go through for database-related / sensitive administrative operations.
 */
public interface AdminAuthorizationService {

    /** Manager requests authorization; an Admin's email+phone OTP flow begins. Returns a stage token. */
    AuthStageResponse requestAuthorization(Long managerId, String operationDescription);

    /** First factor: admin email OTP. Returns a stage token pointing to the phone step. */
    AuthStageResponse verifyAdminEmailOtp(String authorizationToken, String otp);

    /** Second factor: admin phone OTP. Returns a short-lived authorization grant token. */
    String verifyAdminPhoneOtp(String authorizationToken, String otp);
}
