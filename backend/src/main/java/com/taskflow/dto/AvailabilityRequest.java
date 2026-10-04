package com.taskflow.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AvailabilityRequest {
    @NotBlank
    private String status; // AVAILABLE | BUSY | NOT_AVAILABLE
}
