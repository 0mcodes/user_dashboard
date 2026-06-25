package com.dashboard.userdashboard.repository;

import com.dashboard.userdashboard.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // All logs for a specific user — Manager and Admin use this
    Page<AuditLog> findByUserId(Long userId, Pageable pageable);

    // All logs of a specific action type — e.g., all USER_LOGIN events
    Page<AuditLog> findByAction(String action, Pageable pageable);

    // Count failed actions since a given time
    // Used for: "How many failed logins happened today?"
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE " +
            "a.action = :action AND a.success = false AND a.createdAt >= :since")
    long countFailedActionsSince(@Param("action") String action,
                                 @Param("since") LocalDateTime since);

    // Total log count for the stats card
    @Query("SELECT COUNT(a) FROM AuditLog a")
    long countAllLogs();
}