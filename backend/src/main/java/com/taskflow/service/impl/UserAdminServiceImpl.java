package com.taskflow.service.impl;

import com.taskflow.dto.UserResponse;
import com.taskflow.enums.Role;
import com.taskflow.repository.UserRepository;
import com.taskflow.service.UserAdminService;
import com.taskflow.util.MaskingUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserAdminServiceImpl implements UserAdminService {

    private final UserRepository userRepository;
    private final MaskingUtil maskingUtil;

    @Override
    public List<UserResponse> listAllUsers() {
        return userRepository.findAll().stream()
                .map(u -> UserResponse.from(u, maskingUtil))
                .collect(Collectors.toList());
    }

    @Override
    public List<UserResponse> listTeamMembers() {
        return userRepository.findByRole(Role.TEAM_MEMBER).stream()
                .map(u -> UserResponse.from(u, maskingUtil))
                .collect(Collectors.toList());
    }
}
