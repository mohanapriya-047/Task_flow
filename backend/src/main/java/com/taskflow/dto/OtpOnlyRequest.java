package com.taskflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/** Used for endpoints that just need a 6-digit OTP from an already-authenticated user. */
@Data
public class OtpOnlyRequest {
    @NotBlank
    @Pattern(regexp = "^[0-9]{6}$", message = "OTP must be 6 digits")
    private String otp;
}
