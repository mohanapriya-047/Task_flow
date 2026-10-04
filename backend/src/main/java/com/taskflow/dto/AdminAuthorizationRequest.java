package com.taskflow.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** Manager requests Admin authorization for a sensitive/DB-adjacent operation. */
@Data
public class AdminAuthorizationRequest {
    @NotBlank
    private String operationDescription;
}
