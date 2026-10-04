package com.taskflow.service.impl;

import com.taskflow.entity.User;
import com.taskflow.enums.AvailabilityStatus;
import com.taskflow.exception.ApiException;
import com.taskflow.repository.UserRepository;
import com.taskflow.service.AuditService;
import com.taskflow.service.AvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AvailabilityServiceImpl implements AvailabilityService {

    private final UserRepository userRepository;
    private final AuditService auditService;

    @Override
    public AvailabilityStatus getAvailability(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found."));
        return user.getAvailabilityStatus();
    }

    @Override
    @Transactional
    public void updateOwnAvailability(User currentUser, AvailabilityStatus newStatus) {
        // A user may only ever change their OWN availability - there is no
        // endpoint/method that takes an arbitrary target userId for writes.
        currentUser.setAvailabilityStatus(newStatus);
        userRepository.save(currentUser);
        auditService.log(currentUser.getId(), "AVAILABILITY_CHANGED", "USER", currentUser.getId().toString(),
                "Availability changed to " + newStatus);
    }
}
