package com.taskflow.controller;

import com.taskflow.dto.ProjectRequest;
import com.taskflow.dto.ProjectResponse;
import com.taskflow.entity.User;
import com.taskflow.service.ProjectService;
import com.taskflow.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final SecurityUtil securityUtil;

    @GetMapping
    // All three roles may call this - the service scopes results per role
    // (Admin: all, Manager: own, Team Member: assigned-only).
    public ResponseEntity<List<ProjectResponse>> list() {
        return ResponseEntity.ok(projectService.listForUser(securityUtil.currentUser()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getById(id, securityUtil.currentUser()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ProjectResponse> create(@Valid @RequestBody ProjectRequest request) {
        return ResponseEntity.ok(projectService.create(request, securityUtil.currentUser()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ProjectResponse> update(@PathVariable Long id, @Valid @RequestBody ProjectRequest request) {
        return ResponseEntity.ok(projectService.update(id, request, securityUtil.currentUser()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                        @RequestBody(required = false) ProjectRequest request) {
        projectService.delete(id, request, securityUtil.currentUser());
        return ResponseEntity.noContent().build();
    }
}
