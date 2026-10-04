package com.taskflow.controller;

import com.taskflow.dto.AvailabilityRequest;
import com.taskflow.enums.AvailabilityStatus;
import com.taskflow.exception.ApiException;
import com.taskflow.service.AvailabilityService;
import com.taskflow.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class AvailabilityController {

    private final AvailabilityService availabilityService;
    private final SecurityUtil securityUtil;

    @GetMapping("/{id}/availability")
    public ResponseEntity<Map<String, String>> get(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of("status", availabilityService.getAvailability(id).name()));
    }

    // Deliberately NOT /{id}/availability for writes - only "me" can be the
    // target, so there is no id parameter a client could tamper with to
    // change someone else's status.
    @PutMapping("/me/availability")
    public ResponseEntity<Map<String, String>> updateOwn(@Valid @RequestBody AvailabilityRequest request) {
        AvailabilityStatus status;
        try {
            status = AvailabilityStatus.valueOf(request.getStatus());
        } catch (Exception e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid availability status.");
        }
        availabilityService.updateOwnAvailability(securityUtil.currentUser(), status);
        return ResponseEntity.ok(Map.of("status", status.name()));
    }
}
