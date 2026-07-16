package com.dashboard.userdashboard.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_mast", indexes = {
        @Index(name = "idx_audit_user_id", columnList = "userId"),
        @Index(name = "idx_audit_action",  columnList = "action"),
        @Index(name = "idx_audit_created", columnList = "createdAt")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String userEmail;

    @Column(nullable = false)
    private String action;

    private String entityType;
    private Long entityId;

    @Column(length = 1000)
    private String description;

    private String ipAddress;
    private String requestPath;

    @Column(nullable = false)
    @Builder.Default
    private boolean success = true;

    private String errorMessage;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public static final class Actions {
        public static final String USER_REGISTERED     = "USER_REGISTERED";
        public static final String USER_LOGIN          = "USER_LOGIN";
        public static final String USER_LOGIN_FAILED   = "USER_LOGIN_FAILED";
        public static final String PASSWORD_CHANGED    = "PASSWORD_CHANGED";
        public static final String PASSWORD_RESET_REQ  = "PASSWORD_RESET_REQUESTED";
        public static final String PASSWORD_RESET_DONE = "PASSWORD_RESET_COMPLETED";
        public static final String PROFILE_UPDATED     = "PROFILE_UPDATED";
        public static final String ROLE_UPDATED        = "ROLE_UPDATED";
        public static final String USER_ENABLED        = "USER_ENABLED";
        public static final String USER_DISABLED       = "USER_DISABLED";
        public static final String USER_DELETED        = "USER_DELETED";

        private Actions() {}
    }
}