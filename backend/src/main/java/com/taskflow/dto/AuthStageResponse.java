package com.taskflow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic "not fully authenticated yet" response: registration awaiting OTP,
 * login awaiting OTP, manager awaiting phone OTP, etc.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthStageResponse {
    private String stage;          // e.g. "EMAIL_OTP_REQUIRED"
    private String tempToken;      // short-lived pre-auth token for the next step
    private String maskedEmail;    // e.g. "m***@gmail.com" (null if not applicable)
    private String message;
}
