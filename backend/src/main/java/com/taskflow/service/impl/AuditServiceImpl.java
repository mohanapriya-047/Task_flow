package com.taskflow.service.impl;

import com.taskflow.entity.AuditLog;
import com.taskflow.repository.AuditLogRepository;
import com.taskflow.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    @Override
    public void log(Long userId, String action, String resourceType, String resourceId, String description) {
        AuditLog entry = AuditLog.builder()
                .userId(userId)
                .action(action)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .description(description)
                .build();
        auditLogRepository.save(entry);
    }
}
