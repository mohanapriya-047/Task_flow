package com.taskflow.controller;

import com.taskflow.dto.TaskRequest;
import com.taskflow.dto.TaskResponse;
import com.taskflow.service.TaskService;
import com.taskflow.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final SecurityUtil securityUtil;

    @GetMapping("/api/projects/{projectId}/tasks")
    public ResponseEntity<List<TaskResponse>> listForProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(taskService.listForProject(projectId, securityUtil.currentUser()));
    }

    @GetMapping("/api/tasks/mine")
    public ResponseEntity<List<TaskResponse>> listMine() {
        return ResponseEntity.ok(taskService.listMine(securityUtil.currentUser()));
    }

    @PostMapping("/api/projects/{projectId}/tasks")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<TaskResponse> create(@PathVariable Long projectId, @Valid @RequestBody TaskRequest request) {
        return ResponseEntity.ok(taskService.create(projectId, request, securityUtil.currentUser()));
    }

    @PutMapping("/api/tasks/{taskId}")
    // Reachable by all roles - service layer enforces that Team Members may
    // only change status on their OWN assigned task; everything else is 403.
    public ResponseEntity<TaskResponse> update(@PathVariable Long taskId, @Valid @RequestBody TaskRequest request) {
        return ResponseEntity.ok(taskService.update(taskId, request, securityUtil.currentUser()));
    }

    @DeleteMapping("/api/tasks/{taskId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable Long taskId) {
        taskService.delete(taskId, securityUtil.currentUser());
        return ResponseEntity.noContent().build();
    }
}
