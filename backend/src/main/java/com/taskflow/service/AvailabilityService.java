package com.taskflow.service;

import com.taskflow.entity.User;
import com.taskflow.enums.AvailabilityStatus;

public interface AvailabilityService {
    AvailabilityStatus getAvailability(Long userId);
    void updateOwnAvailability(User currentUser, AvailabilityStatus newStatus);
}
