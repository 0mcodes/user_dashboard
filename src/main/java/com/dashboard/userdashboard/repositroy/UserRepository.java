package com.dashboard.userdashboard.repository;

import com.dashboard.userdashboard.model.Role;
import com.dashboard.userdashboard.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Spring reads "findByEmail" and generates:
    // SELECT * FROM users WHERE email = ?
    Optional<User> findByEmail(String email);

    // Spring reads "existsByEmail" and generates:
    // SELECT COUNT(*) > 0 FROM users WHERE email = ?
    boolean existsByEmail(String email);

    // Spring reads "findByRole" and generates:
    // SELECT * FROM users WHERE role = ? with pagination
    Page<User> findByRole(Role role, Pageable pageable);

    // Custom JPQL — too complex for derived query methods
    // Searches across 3 columns with OR and case-insensitive matching
    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.lastName)  LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.email)     LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<User> searchUsers(@Param("search") String search, Pageable pageable);

    // Statistics — these return a single number
    long countByRole(Role role);
    long countByEnabledTrue();
    long countByEnabledFalse();
    long countByAccountNonLockedFalse();
    long countByCreatedAtAfter(LocalDateTime after);

    // UPDATE queries need @Modifying
    // Without it Spring treats them as SELECT and crashes
    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :loginTime WHERE u.id = :userId")
    void updateLastLoginAt(@Param("userId") Long userId,
                           @Param("loginTime") LocalDateTime loginTime);

    @Modifying
    @Query("UPDATE User u SET u.enabled = :enabled WHERE u.id = :userId")
    void updateEnabledStatus(@Param("userId") Long userId,
                             @Param("enabled") boolean enabled);
}