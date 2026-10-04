package com.taskflow.service;

import com.taskflow.dto.TaskRequest;
import com.taskflow.dto.TaskResponse;
import com.taskflow.entity.User;

import java.util.List;

public interface TaskService {
    List<TaskResponse> listForProject(Long projectId, User currentUser);
    List<TaskResponse> listMine(User currentUser);
    TaskResponse create(Long projectId, TaskRequest request, User currentUser);
    TaskResponse update(Long taskId, TaskRequest request, User currentUser);
    void delete(Long taskId, User currentUser);
}
