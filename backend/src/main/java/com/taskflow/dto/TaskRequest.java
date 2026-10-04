package com.taskflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TaskRequest {
    @NotBlank
    @Size(max = 150)
    private String name;

    @Size(max = 2000)
    private String description;

    private Long assignedUserId;
    private String status;   // TODO | IN_PROGRESS | COMPLETED
    private String priority; // LOW | MEDIUM | HIGH | URGENT
    private LocalDate dueDate;
}
