package com.taskflow.service;

/**
 * Handles the "Manager phone OTP" step required before sensitive project
 * mutations (edit name/description/status, delete project).
 */
public interface ManagerVerificationService {

    /** Sends a phone OTP to the currently authenticated manager. */
    void sendPhoneOtp(Long managerUserId, String phoneNumber);

    /**
     * Verifies the OTP the manager just entered and, on success, issues a
     * short-lived action grant token proving this manager just completed
     * phone verification for a sensitive action.
     */
    String verifyPhoneOtpAndIssueActionToken(Long managerUserId, String otp);

    /** Validates a manager action grant token belongs to this manager; throws ApiException otherwise. */
    void assertValidActionToken(Long managerUserId, String actionToken);
}
