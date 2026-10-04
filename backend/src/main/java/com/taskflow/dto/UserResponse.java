package com.taskflow.dto;

import com.taskflow.entity.User;
import com.taskflow.enums.AvailabilityStatus;
import com.taskflow.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Public-facing user representation. Never includes passwordHash or raw email in list contexts. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String name;
    private String username;
    private String maskedEmail;
    private Role role;
    private AvailabilityStatus availabilityStatus;

    public static UserResponse from(User user, com.taskflow.util.MaskingUtil maskingUtil) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .username(user.getUsername())
                .maskedEmail(maskingUtil.maskEmail(user.getEmail()))
                .role(user.getRole())
                .availabilityStatus(user.getAvailabilityStatus())
                .build();
    }
}
