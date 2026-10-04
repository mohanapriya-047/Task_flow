package com.taskflow.controller;

import com.taskflow.dto.AdminAuthorizationRequest;
import com.taskflow.dto.AuthStageResponse;
import com.taskflow.dto.VerifyAdminStepRequest;
import com.taskflow.entity.User;
import com.taskflow.service.AdminAuthorizationService;
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

/**
 * Manager -> Admin dual-OTP authorization flow for database-related /
 * sensitive administrative operations (spec section 11). This grants a
 * temporary authorization token; it does NOT execute the operation itself
 * or expose any database credentials - see section 11/12 of the spec.
 */
@RestController
@RequestMapping("/api/admin/authorization")
@RequiredArgsConstructor
public class AdminAuthorizationController {

    private final AdminAuthorizationService adminAuthorizationService;
    private final SecurityUtil securityUtil;

    @PostMapping("/request")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<AuthStageResponse> request(@Valid @RequestBody AdminAuthorizationRequest request) {
        User manager = securityUtil.currentUser();
        return ResponseEntity.ok(
                adminAuthorizationService.requestAuthorization(manager.getId(), request.getOperationDescription()));
    }

    @PostMapping("/verify-email")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<AuthStageResponse> verifyEmail(@Valid @RequestBody VerifyAdminStepRequest request) {
        return ResponseEntity.ok(
                adminAuthorizationService.verifyAdminEmailOtp(request.getAuthorizationToken(), request.getOtp()));
    }

    @PostMapping("/verify-phone")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Map<String, String>> verifyPhone(@Valid @RequestBody VerifyAdminStepRequest request) {
        String grantToken = adminAuthorizationService.verifyAdminPhoneOtp(
                request.getAuthorizationToken(), request.getOtp());
        return ResponseEntity.ok(Map.of(
                "authorizationGrantToken", grantToken,
                "message", "Admin authorization approved. This grant is temporary and single-purpose."));
    }
}
