package com.dashboard.userdashboard.service;

import com.dashboard.userdashboard.model.AuditLog;
import com.dashboard.userdashboard.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    // ── Log a SUCCESSFUL action ───────────────────────────────────
    @Async
    @Transactional
    public void log(Long userId,
                    String userEmail,
                    String action,
                    String entityType,
                    Long entityId,
                    String description) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .userId(userId)
                    .userEmail(userEmail)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .description(description)
                    .ipAddress(getClientIpAddress())
                    .requestPath(getCurrentRequestPath())
                    .success(true)
                    .build();

            auditLogRepository.save(auditLog);

        } catch (Exception e) {
            // Audit logging must NEVER crash the caller
            log.error("Failed to save audit log for action {}: {}",
                    action, e.getMessage());
        }
    }

    // ── Log a FAILED action ───────────────────────────────────────
    @Async
    @Transactional
    public void logFailure(Long userId,
                           String userEmail,
                           String action,
                           String description,
                           String errorMessage) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .userId(userId)
                    .userEmail(userEmail)
                    .action(action)
                    .description(description)
                    .ipAddress(getClientIpAddress())
                    .requestPath(getCurrentRequestPath())
                    .success(false)
                    .errorMessage(errorMessage)
                    .build();

            auditLogRepository.save(auditLog);

        } catch (Exception e) {
            log.error("Failed to save failure audit log: {}",
                    e.getMessage());
        }
    }

    // ── Query methods (called by Admin/Manager controllers) ────────
    @Transactional(readOnly = true)
    public Page<AuditLog> getAllLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> getLogsByUser(Long userId, Pageable pageable) {
        return auditLogRepository.findByUserId(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> getLogsByAction(String action, Pageable pageable) {
        return auditLogRepository.findByAction(action, pageable);
    }

    // ── Private helpers ────────────────────────────────────────────

    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes)
                            RequestContextHolder.getRequestAttributes();

            if (attrs == null) return "unknown";

            HttpServletRequest request = attrs.getRequest();

            // X-Forwarded-For is set by reverse proxies (Nginx, AWS ALB)
            // It contains the REAL client IP — getRemoteAddr() would
            // return the proxy's IP, not the user's
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                // Can be comma-separated list — first is the original client
                return xForwardedFor.split(",")[0].trim();
            }

            return request.getRemoteAddr();

        } catch (Exception e) {
            return "unknown";
        }
    }

    private String getCurrentRequestPath() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes)
                            RequestContextHolder.getRequestAttributes();

            if (attrs == null) return "unknown";

            HttpServletRequest req = attrs.getRequest();
            return req.getMethod() + " " + req.getRequestURI();

        } catch (Exception e) {
            return "unknown";
        }
    }
}