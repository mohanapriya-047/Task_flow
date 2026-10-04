package com.taskflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProjectRequest {
    @NotBlank
    @Size(max = 150)
    private String name;

    @Size(max = 2000)
    private String description;

    private String status; // PLANNING | IN_PROGRESS | COMPLETED | ON_HOLD
    private Integer progress;

    // Required only when Admin creates a project on behalf of a manager.
    private Long managerId;

    // Required on PUT/DELETE when the caller is a MANAGER: the short-lived
    // grant token returned by POST /api/auth/verify-manager-phone-otp,
    // proving the manager just completed phone OTP verification for this
    // sensitive action. Admins never need to supply this.
    private String managerActionToken;
}
