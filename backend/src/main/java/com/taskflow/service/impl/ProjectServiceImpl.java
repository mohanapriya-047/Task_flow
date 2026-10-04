package com.taskflow.service.impl;

import com.taskflow.dto.ProjectRequest;
import com.taskflow.dto.ProjectResponse;
import com.taskflow.entity.Project;
import com.taskflow.entity.User;
import com.taskflow.enums.ProjectStatus;
import com.taskflow.enums.Role;
import com.taskflow.exception.ApiException;
import com.taskflow.repository.ProjectRepository;
import com.taskflow.repository.UserRepository;
import com.taskflow.service.AuditService;
import com.taskflow.service.ManagerVerificationService;
import com.taskflow.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ManagerVerificationService managerVerificationService;
    private final AuditService auditService;

    @Override
    public List<ProjectResponse> listForUser(User currentUser) {
        List<Project> projects = switch (currentUser.getRole()) {
            case ADMIN -> projectRepository.findAll();
            case MANAGER -> projectRepository.findByManagerId(currentUser.getId());
            case TEAM_MEMBER -> projectRepository.findProjectsAssignedToUser(currentUser.getId());
        };
        return projects.stream().map(ProjectResponse::from).collect(Collectors.toList());
    }

    @Override
    public ProjectResponse getById(Long id, User currentUser) {
        Project project = findAccessible(id, currentUser);
        return ProjectResponse.from(project);
    }

    @Override
    @Transactional
    public ProjectResponse create(ProjectRequest request, User currentUser) {
        // Team Members can never reach here (endpoint is @PreAuthorize'd), but keep
        // a defensive check in the service layer too - never rely on frontend-only checks.
        if (currentUser.getRole() == Role.TEAM_MEMBER) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You do not have permission to perform this action.");
        }

        User manager;
        if (currentUser.getRole() == Role.ADMIN) {
            if (request.getManagerId() == null) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "managerId is required when Admin creates a project.");
            }
            manager = userRepository.findById(request.getManagerId())
                    .filter(u -> u.getRole() == Role.MANAGER)
                    .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Manager not found."));
        } else {
            manager = currentUser; // MANAGER creates a project owned by themselves
        }

        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .manager(manager)
                .status(request.getStatus() != null ? parseStatus(request.getStatus()) : ProjectStatus.PLANNING)
                .progress(request.getProgress() != null ? clampProgress(request.getProgress()) : 0)
                .build();
        project = projectRepository.save(project);

        auditService.log(currentUser.getId(), "PROJECT_CREATED", "PROJECT", project.getId().toString(),
                "Project '" + project.getName() + "' created.");

        return ProjectResponse.from(project);
    }

    @Override
    @Transactional
    public ProjectResponse update(Long id, ProjectRequest request, User currentUser) {
        Project project = findAccessible(id, currentUser);

        if (currentUser.getRole() == Role.TEAM_MEMBER) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You do not have permission to perform this action.");
        }
        if (currentUser.getRole() == Role.MANAGER) {
            requireOwnedByManager(project, currentUser);
            // Editing name/description/status is a "sensitive" change -> phone OTP grant required.
            managerVerificationService.assertValidActionToken(currentUser.getId(), request.getManagerActionToken());
        }

        if (request.getName() != null) project.setName(request.getName());
        if (request.getDescription() != null) project.setDescription(request.getDescription());
        if (request.getStatus() != null) project.setStatus(parseStatus(request.getStatus()));
        if (request.getProgress() != null) project.setProgress(clampProgress(request.getProgress()));

        project = projectRepository.save(project);

        auditService.log(currentUser.getId(), "PROJECT_UPDATED", "PROJECT", project.getId().toString(),
                "Project '" + project.getName() + "' updated.");

        return ProjectResponse.from(project);
    }

    @Override
    @Transactional
    public void delete(Long id, ProjectRequest request, User currentUser) {
        Project project = findAccessible(id, currentUser);

        if (currentUser.getRole() == Role.TEAM_MEMBER) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You do not have permission to perform this action.");
        }
        if (currentUser.getRole() == Role.MANAGER) {
            requireOwnedByManager(project, currentUser);
            managerVerificationService.assertValidActionToken(currentUser.getId(),
                    request != null ? request.getManagerActionToken() : null);
        }

        projectRepository.delete(project);
        auditService.log(currentUser.getId(), "PROJECT_DELETED", "PROJECT", id.toString(),
                "Project '" + project.getName() + "' deleted.");
    }

    // ----------------------------------------------------------------- helpers

    private Project findAccessible(Long id, User currentUser) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Project not found."));

        switch (currentUser.getRole()) {
            case ADMIN -> { /* full access */ }
            case MANAGER -> requireOwnedByManager(project, currentUser);
            case TEAM_MEMBER -> {
                boolean assigned = projectRepository.findProjectsAssignedToUser(currentUser.getId()).stream()
                        .anyMatch(p -> p.getId().equals(project.getId()));
                if (!assigned) {
                    throw new ApiException(HttpStatus.FORBIDDEN, "You do not have permission to perform this action.");
                }
            }
        }
        return project;
    }

    private void requireOwnedByManager(Project project, User manager) {
        if (!project.getManager().getId().equals(manager.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You do not have permission to perform this action.");
        }
    }

    private ProjectStatus parseStatus(String status) {
        try {
            return ProjectStatus.valueOf(status);
        } catch (Exception e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid project status.");
        }
    }

    private int clampProgress(int value) {
        return Math.max(0, Math.min(100, value));
    }
}
