package com.gymflow.pro.service;

import com.gymflow.pro.entity.AuditLog;
import com.gymflow.pro.entity.User;
import com.gymflow.pro.repository.AuditLogRepository;
import com.gymflow.pro.service.impl.AuditServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditServiceImplTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private HttpServletRequest request;

    private AuditServiceImpl auditService;

    @BeforeEach
    void setUp() {
        auditService = new AuditServiceImpl(auditLogRepository);
    }

    @Test
    void log_withUser_shouldSaveAuditLogWithUserNameAndIp() {
        User user = User.builder().name("Alice").build();
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("10.0.0.1");

        auditService.log(user, "CREATE", "Student", "123", "created student", request);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        AuditLog saved = captor.getValue();
        assertThat(saved.getUserName()).isEqualTo("Alice");
        assertThat(saved.getAction()).isEqualTo("CREATE");
        assertThat(saved.getEntityName()).isEqualTo("Student");
        assertThat(saved.getEntityId()).isEqualTo("123");
        assertThat(saved.getIpAddress()).isEqualTo("10.0.0.1");
    }

    @Test
    void log_withNullUser_shouldUseSystemAsUserName() {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("10.0.0.2");

        auditService.log((User) null, "DELETE", "Product", "456", "deleted product", request);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        assertThat(captor.getValue().getUserName()).isEqualTo("SYSTEM");
    }

    @Test
    void log_shouldExtractFirstIp_whenForwardedHeaderPresent() {
        when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.5, 10.0.0.3");

        auditService.log("Bob", "UPDATE", "Plan", "789", "updated plan", request);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        assertThat(captor.getValue().getIpAddress()).isEqualTo("203.0.113.5");
        assertThat(captor.getValue().getUserName()).isEqualTo("Bob");
    }

    @Test
    void log_shouldHandleNullRequest_withoutThrowing() {
        auditService.log("Carol", "READ", "Report", null, "viewed report", null);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        assertThat(captor.getValue().getIpAddress()).isNull();
    }
}
