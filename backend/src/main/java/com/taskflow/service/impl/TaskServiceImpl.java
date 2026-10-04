package com.taskflow.service.impl;

import com.taskflow.dto.TaskRequest;
import com.taskflow.dto.TaskResponse;
import com.taskflow.entity.Project;
import com.taskflow.entity.Task;
import com.taskflow.entity.User;
import com.taskflow.enums.Role;
import com.taskflow.enums.TaskPriority;
import com.taskflow.enums.TaskStatus;
import com.taskflow.exception.ApiException;
import com.taskflow.repository.ProjectRepository;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.UserRepository;
import com.taskflow.service.AuditService;
import com.taskflow.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Override
    public List<TaskResponse> listForProject(Long projectId, User currentUser) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Project not found."));

        if (currentUser.getRole() == Role.MANAGER && !project.getManager().getId().equals(currentUser.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You do not have permission to perform this action.");
        }
        List<Task> tasks = taskRepository.findByProjectId(projectId);
        if (currentUser.getRole() == Role.TEAM_MEMBER) {
            tasks = tasks.stream()
                    .filter(t -> t.getAssignedUser() != null && t.getAssignedUser().getId().equals(currentUser.getId()))
                    .collect(Collectors.toList());
        }
        return tasks.stream().map(TaskResponse::from).collect(Collectors.toList());
    }

    @Override
    public List<TaskResponse> listMine(User currentUser) {
        return taskRepository.findByAssignedUserId(currentUser.getId()).stream()
                .map(TaskResponse::from).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TaskResponse create(Long projectId, TaskRequest request, User currentUser) {
        if (currentUser.getRole() == Role.TEAM_MEMBER) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You do not have permission to perform this action.");
        }
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Project not found."));
        if (currentUser.getRole() == Role.MANAGER && !project.getManager().getId().equals(currentUser.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You do not have permission to perform this action.");
        }

        User assignee = null;
        if (request.getAssignedUserId() != null) {
            assignee = userRepository.findById(request.getAssignedUserId())
                    .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Assigned user not found."));
        }

        Task task = Task.builder()
                .project(project)
                .name(request.getName())
                .description(request.getDescription())
                .assignedUser(assignee)
                .status(request.getStatus() != null ? parseStatus(request.getStatus()) : TaskStatus.TODO)
                .priority(request.getPriority() != null ? parsePriority(request.getPriority()) : TaskPriority.MEDIUM)
                .dueDate(request.getDueDate())
                .build();
        task = taskRepository.save(task);

        auditService.log(currentUser.getId(), "TASK_CREATED", "TASK", task.getId().toString(),
                "Task '" + task.getName() + "' created in project " + project.getId());

        return TaskResponse.from(task);
    }

    @Override
    @Transactional
    public TaskResponse update(Long taskId, TaskRequest request, User currentUser) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Task not found."));
        Project project = task.getProject();

        boolean isOwnerManager = currentUser.getRole() == Role.MANAGER
                && project.getManager().getId().equals(currentUser.getId());
        boolean isAssignedMember = currentUser.getRole() == Role.TEAM_MEMBER
                && task.getAssignedUser() != null
                && task.getAssignedUser().getId().equals(currentUser.getId());

        if (currentUser.getRole() == Role.ADMIN || isOwnerManager) {
            // Admin/Manager can edit full task details.
            if (request.getName() != null) task.setName(request.getName());
            if (request.getDescription() != null) task.setDescription(request.getDescription());
            if (request.getAssignedUserId() != null) {
                User assignee = userRepository.findById(request.getAssignedUserId())
                        .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Assigned user not found."));
                task.setAssignedUser(assignee);
            }
            if (request.getPriority() != null) task.setPriority(parsePriority(request.getPriority()));
            if (request.getDueDate() != null) task.setDueDate(request.getDueDate());
            if (request.getStatus() != null) task.setStatus(parseStatus(request.getStatus()));
        } else if (isAssignedMember) {
            // Team Members may ONLY change status, and only on their own task
            // (TODO -> IN_PROGRESS -> COMPLETED). They cannot touch anything else.
            if (request.getStatus() == null) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Only task status can be updated.");
            }
            task.setStatus(parseStatus(request.getStatus()));
        } else {
            throw new ApiException(HttpStatus.FORBIDDEN, "You do not have permission to perform this action.");
        }

        task = taskRepository.save(task);
        String action = (isAssignedMember && task.getStatus() == TaskStatus.COMPLETED)
                ? "TASK_COMPLETED" : "TASK_UPDATED";
        auditService.log(currentUser.getId(), action, "TASK", task.getId().toString(),
                "Task '" + task.getName() + "' updated.");

        return TaskResponse.from(task);
    }

    @Override
    @Transactional
    public void delete(Long taskId, User currentUser) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Task not found."));
        Project project = task.getProject();

        boolean isOwnerManager = currentUser.getRole() == Role.MANAGER
                && project.getManager().getId().equals(currentUser.getId());

        // Team Members can never delete tasks, per spec.
        if (currentUser.getRole() != Role.ADMIN && !isOwnerManager) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You do not have permission to perform this action.");
        }

        taskRepository.delete(task);
        auditService.log(currentUser.getId(), "TASK_DELETED", "TASK", taskId.toString(),
                "Task '" + task.getName() + "' deleted.");
    }

    private TaskStatus parseStatus(String s) {
        try {
            return TaskStatus.valueOf(s);
        } catch (Exception e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid task status.");
        }
    }

    private TaskPriority parsePriority(String s) {
        try {
            return TaskPriority.valueOf(s);
        } catch (Exception e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid task priority.");
        }
    }
}
