package com.gymflow.pro.service.impl;

import com.gymflow.pro.entity.AuditLog;
import com.gymflow.pro.entity.User;
import com.gymflow.pro.repository.AuditLogRepository;
import com.gymflow.pro.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(User user, String action, String entityName, String entityId, String details, HttpServletRequest request) {
        AuditLog auditLog = AuditLog.builder()
                .user(user)
                .userName(user != null ? user.getName() : "SYSTEM")
                .action(action)
                .entityName(entityName)
                .entityId(entityId)
                .details(details)
                .ipAddress(extractIp(request))
                .build();
        auditLogRepository.save(auditLog);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String userName, String action, String entityName, String entityId, String details, HttpServletRequest request) {
        AuditLog auditLog = AuditLog.builder()
                .userName(userName)
                .action(action)
                .entityName(entityName)
                .entityId(entityId)
                .details(details)
                .ipAddress(extractIp(request))
                .build();
        auditLogRepository.save(auditLog);
    }

    private String extractIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
