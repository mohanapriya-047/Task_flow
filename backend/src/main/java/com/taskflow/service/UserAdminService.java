package com.taskflow.service;

import com.taskflow.dto.UserResponse;
import com.taskflow.entity.User;

import java.util.List;

public interface UserAdminService {
    List<UserResponse> listAllUsers();
    List<UserResponse> listTeamMembers();
}
