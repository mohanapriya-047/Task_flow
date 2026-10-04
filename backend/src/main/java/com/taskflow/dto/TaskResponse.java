package com.taskflow.dto;

import com.taskflow.entity.Task;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskResponse {
    private Long id;
    private Long projectId;
    private String name;
    private String description;
    private Long assignedUserId;
    private String assignedUserName;
    private String status;
    private String priority;
    private LocalDate dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TaskResponse from(Task t) {
        return TaskResponse.builder()
                .id(t.getId())
                .projectId(t.getProject().getId())
                .name(t.getName())
                .description(t.getDescription())
                .assignedUserId(t.getAssignedUser() != null ? t.getAssignedUser().getId() : null)
                .assignedUserName(t.getAssignedUser() != null ? t.getAssignedUser().getName() : null)
                .status(t.getStatus().name())
                .priority(t.getPriority().name())
                .dueDate(t.getDueDate())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}
