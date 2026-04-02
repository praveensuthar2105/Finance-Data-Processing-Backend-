package com.finance.backend.service;

import com.finance.backend.entity.AuditLog;
import com.finance.backend.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @PreAuthorize("hasRole('ADMIN')")
    public Page<AuditLog> getAuditLogs(Long userId, String action, Pageable pageable) {
        if (userId != null && action != null) {
            return auditLogRepository.findByUserIdAndAction(userId, action, pageable);
        } else if (userId != null) {
            return auditLogRepository.findByUserId(userId, pageable);
        } else if (action != null) {
            return auditLogRepository.findByAction(action, pageable);
        }
        return auditLogRepository.findAll(pageable);
    }

    public void createAuditLog(AuditLog auditLog) {
        auditLogRepository.save(auditLog);
    }
}
