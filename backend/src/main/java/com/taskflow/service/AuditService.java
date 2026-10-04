package com.taskflow.service;

public interface AuditService {
    void log(Long userId, String action, String resourceType, String resourceId, String description);
}
