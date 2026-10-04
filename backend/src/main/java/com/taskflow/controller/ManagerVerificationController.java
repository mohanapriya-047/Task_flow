package com.taskflow.controller;

import com.taskflow.dto.ActionTokenResponse;
import com.taskflow.dto.OtpOnlyRequest;
import com.taskflow.entity.User;
import com.taskflow.service.ManagerVerificationService;
import com.taskflow.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class ManagerVerificationController {

    private final ManagerVerificationService managerVerificationService;
    private final SecurityUtil securityUtil;

    @PostMapping("/manager-phone-otp")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Map<String, String>> sendPhoneOtp() {
        User manager = securityUtil.currentUser();
        managerVerificationService.sendPhoneOtp(manager.getId(), manager.getPhoneNumber());
        return ResponseEntity.ok(Map.of("message", "Phone OTP sent."));
    }

    @PostMapping("/verify-manager-phone-otp")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ActionTokenResponse> verifyPhoneOtp(@Valid @RequestBody OtpOnlyRequest request) {
        User manager = securityUtil.currentUser();
        String actionToken = managerVerificationService.verifyPhoneOtpAndIssueActionToken(
                manager.getId(), request.getOtp());
        return ResponseEntity.ok(new ActionTokenResponse(actionToken,
                "Phone verified. You may now complete the sensitive action."));
    }
}
