package com.taskflow.controller;

import com.taskflow.dto.UserResponse;
import com.taskflow.entity.AuditLog;
import com.taskflow.repository.AuditLogRepository;
import com.taskflow.service.UserAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')") // class-level: every endpoint here is Admin-only
@RequiredArgsConstructor
public class AdminController {

    private final UserAdminService userAdminService;
    private final AuditLogRepository auditLogRepository;

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> listUsers() {
        return ResponseEntity.ok(userAdminService.listAllUsers());
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLog>> auditLogs(@RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(
                auditLogRepository.findAllByOrderByTimestampDesc(PageRequest.of(page, size, Sort.unsorted()))
                        .getContent());
    }
}
