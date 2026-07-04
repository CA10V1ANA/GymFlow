package com.gymflow.pro.service;

import com.gymflow.pro.entity.User;
import jakarta.servlet.http.HttpServletRequest;

public interface AuditService {

    void log(User user, String action, String entityName, String entityId, String details, HttpServletRequest request);

    void log(String userName, String action, String entityName, String entityId, String details, HttpServletRequest request);
}
