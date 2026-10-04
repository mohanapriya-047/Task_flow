package com.taskflow.controller;

import com.taskflow.dto.UserResponse;
import com.taskflow.service.UserAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Team member directory - visible to Admin and Manager (e.g. for assigning tasks / viewing availability). */
@RestController
@RequestMapping("/api/team")
@RequiredArgsConstructor
public class TeamController {

    private final UserAdminService userAdminService;

    @GetMapping("/members")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<UserResponse>> listMembers() {
        return ResponseEntity.ok(userAdminService.listTeamMembers());
    }
}
