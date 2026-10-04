package com.taskflow.service;

import com.taskflow.dto.*;

public interface AuthService {
    AuthStageResponse register(RegisterRequest request);
    AuthStageResponse verifyRegistrationOtp(VerifyOtpRequest request);
    AuthStageResponse resendOtp(ResendOtpRequest request, String otpTypeHint);

    AuthStageResponse login(LoginRequest request);
    AuthSuccessResponse verifyLoginOtp(VerifyOtpRequest request, String ipAddress);
}
