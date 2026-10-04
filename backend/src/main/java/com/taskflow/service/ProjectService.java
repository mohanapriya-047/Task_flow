package com.taskflow.service;

import com.taskflow.dto.ProjectRequest;
import com.taskflow.dto.ProjectResponse;
import com.taskflow.entity.User;

import java.util.List;

public interface ProjectService {
    List<ProjectResponse> listForUser(User currentUser);
    ProjectResponse getById(Long id, User currentUser);
    ProjectResponse create(ProjectRequest request, User currentUser);
    ProjectResponse update(Long id, ProjectRequest request, User currentUser);
    void delete(Long id, ProjectRequest request, User currentUser);
}
