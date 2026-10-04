package com.taskflow.controller;

import com.taskflow.dto.*;
import com.taskflow.enums.OtpType;
import com.taskflow.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthStageResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/verify-registration-otp")
    public ResponseEntity<AuthStageResponse> verifyRegistrationOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return ResponseEntity.ok(authService.verifyRegistrationOtp(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthStageResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/verify-login-otp")
    public ResponseEntity<AuthSuccessResponse> verifyLoginOtp(@Valid @RequestBody VerifyOtpRequest request,
                                                                HttpServletRequest httpRequest) {
        return ResponseEntity.ok(authService.verifyLoginOtp(request, clientIp(httpRequest)));
    }

    /** type=REGISTRATION_EMAIL or LOGIN_EMAIL, matching which stage the tempToken was issued for. */
    @PostMapping("/resend-otp")
    public ResponseEntity<AuthStageResponse> resendOtp(@Valid @RequestBody ResendOtpRequest request,
                                                          @RequestParam("type") String type) {
        // Only email OTP types are valid here - resend for phone flows has its own endpoints.
        OtpType.valueOf(type); // validates the enum value up front (throws 400 via handler otherwise)
        return ResponseEntity.ok(authService.resendOtp(request, type));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        // Stateless JWT: logout is a client-side token discard. Nothing to invalidate server-side
        // in this scaffold; a production system might maintain a short-lived token blocklist.
        return ResponseEntity.noContent().build();
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
