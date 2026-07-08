package com.dashboard.userdashboard.service;

import com.dashboard.userdashboard.dto.request.ChangePasswordRequest;
import com.dashboard.userdashboard.dto.request.UpdateProfileRequest;
import com.dashboard.userdashboard.dto.request.UpdateRoleRequest;
import com.dashboard.userdashboard.dto.response.DashboardStatsResponse;
import com.dashboard.userdashboard.dto.response.PagedResponse;
import com.dashboard.userdashboard.dto.response.UserResponse;
import com.dashboard.userdashboard.exception.AppException;
import com.dashboard.userdashboard.model.AuditLog;
import com.dashboard.userdashboard.model.Role;
import com.dashboard.userdashboard.model.User;
import com.dashboard.userdashboard.repository.AuditLogRepository;
import com.dashboard.userdashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    // ── GET PROFILE ───────────────────────────────────────────────
    @Transactional(readOnly = true)
    public UserResponse getProfile(Long userId) {
        /*
         * findById returns Optional<User>.
         * orElseThrow unwraps it or throws AppException if not found.
         * UserResponse.fromUser() converts the entity to a safe DTO
         * (excludes the password hash).
         *
         * Every call to this method hits the database for fresh data.
         * No caching — profile data must always be current.
         */
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new AppException("User not found", HttpStatus.NOT_FOUND));
        return UserResponse.fromUser(user);
    }

    // ── UPDATE PROFILE ────────────────────────────────────────────
    @Transactional
    public UserResponse updateProfile(Long userId,
                                      UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new AppException("User not found", HttpStatus.NOT_FOUND));

        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setBio(request.getBio());
        user.setProfilePictureUrl(request.getProfilePictureUrl());
        user.setDateOfBirth(request.getDateOfBirth());    // ADD
        user.setLocation(request.getLocation());

        User saved = userRepository.save(user);

        auditService.log(
                userId, user.getEmail(),
                AuditLog.Actions.PROFILE_UPDATED,
                "User", userId,
                "Profile updated for " + user.getFullName()
        );

        return UserResponse.fromUser(saved);
    }

    // ── CHANGE PASSWORD ───────────────────────────────────────────
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {

        // Check the two new passwords match first
        if (!request.getNewPassword()
                .equals(request.getConfirmNewPassword())) {
            throw new AppException(
                    "New passwords do not match",
                    HttpStatus.BAD_REQUEST
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new AppException("User not found", HttpStatus.NOT_FOUND));

        // Verify current password — user must prove they know it
        if (!passwordEncoder.matches(
                request.getCurrentPassword(), user.getPassword())) {
            throw new AppException(
                    "Current password is incorrect",
                    HttpStatus.UNAUTHORIZED
            );
        }

        // New password cannot be the same as the current one
        if (passwordEncoder.matches(
                request.getNewPassword(), user.getPassword())) {
            throw new AppException(
                    "New password must be different from current password",
                    HttpStatus.BAD_REQUEST
            );
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        auditService.log(
                userId, user.getEmail(),
                AuditLog.Actions.PASSWORD_CHANGED,
                "User", userId,
                "Password changed for " + user.getEmail()
        );
    }

    // ── GET ALL USERS (with search and pagination) ─────────────────
    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> getAllUsers(int page,
                                                   int size,
                                                   String sortBy,
                                                   String search) {
        // Build the Pageable object — page number, size, sort direction
        Pageable pageable = PageRequest.of(
                page, size,
                Sort.by(Sort.Direction.DESC, sortBy)
        );

        Page<User> users;

        if (search != null && !search.trim().isEmpty()) {
            // Use the custom search query across firstName, lastName, email
            users = userRepository.searchUsers(search.trim(), pageable);
        } else {
            // No search — return all users paginated
            users = userRepository.findAll(pageable);
        }

        return buildPagedResponse(users);
    }

    // ── UPDATE ROLE (Admin only) ──────────────────────────────────
    @Transactional
    public UserResponse updateUserRole(Long targetUserId,
                                       UpdateRoleRequest request,
                                       Long adminId,
                                       String adminEmail) {

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() ->
                        new AppException("User not found", HttpStatus.NOT_FOUND));

        Role oldRole = target.getRole();
        target.setRole(request.getRole());
        User saved = userRepository.save(target);

        auditService.log(
                adminId, adminEmail,
                AuditLog.Actions.ROLE_UPDATED,
                "User", targetUserId,
                "Role changed from " + oldRole
                        + " to " + request.getRole()
                        + " for " + target.getEmail()
        );

        return UserResponse.fromUser(saved);
    }

    // ── TOGGLE ENABLE/DISABLE (Admin only) ────────────────────────
    @Transactional
    public void toggleUserStatus(Long targetUserId,
                                 boolean enable,
                                 Long adminId,
                                 String adminEmail) {

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() ->
                        new AppException("User not found", HttpStatus.NOT_FOUND));

        target.setEnabled(enable);
        userRepository.save(target);

        String action = enable
                ? AuditLog.Actions.USER_ENABLED
                : AuditLog.Actions.USER_DISABLED;

        auditService.log(
                adminId, adminEmail, action,
                "User", targetUserId,
                (enable ? "Enabled" : "Disabled")
                        + " user: " + target.getEmail()
        );
    }

    // ── DELETE USER (Admin only) ──────────────────────────────────
    @Transactional
    public void deleteUser(Long targetUserId,
                           Long adminId,
                           String adminEmail) {

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() ->
                        new AppException("User not found", HttpStatus.NOT_FOUND));

        // Audit log BEFORE delete — after delete, the user ID is gone
        auditService.log(
                adminId, adminEmail,
                AuditLog.Actions.USER_DELETED,
                "User", targetUserId,
                "Deleted user: " + target.getEmail()
                        + " (" + target.getFullName() + ")"
        );

        userRepository.delete(target);
    }

    // ── DASHBOARD STATISTICS ──────────────────────────────────────
    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats() {

        // Calculate time boundaries for "today", "this week", "this month"
        LocalDateTime startOfToday =
                LocalDate.now().atStartOfDay();

        LocalDateTime startOfWeek =
                LocalDate.now()
                        .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                        .atStartOfDay();

        LocalDateTime startOfMonth =
                LocalDate.now()
                        .withDayOfMonth(1)
                        .atStartOfDay();

        return DashboardStatsResponse.builder()
                .totalUsers(userRepository.count())
                .totalAdmins(userRepository.countByRole(Role.ROLE_ADMIN))
                .totalManagers(userRepository.countByRole(Role.ROLE_MANAGER))
                .totalRegularUsers(userRepository.countByRole(Role.ROLE_USER))
                .activeUsers(userRepository.countByEnabledTrue())
                .disabledUsers(userRepository.countByEnabledFalse())
                .lockedUsers(userRepository.countByAccountNonLockedFalse())
                .newUsersToday(
                        userRepository.countByCreatedAtAfter(startOfToday))
                .newUsersThisWeek(
                        userRepository.countByCreatedAtAfter(startOfWeek))
                .newUsersThisMonth(
                        userRepository.countByCreatedAtAfter(startOfMonth))
                .totalAuditLogs(auditLogRepository.countAllLogs())
                .failedLoginsToday(auditLogRepository.countFailedActionsSince(
                        AuditLog.Actions.USER_LOGIN_FAILED, startOfToday))
                .build();
    }

    // ── PRIVATE HELPER ────────────────────────────────────────────
    private PagedResponse<UserResponse> buildPagedResponse(Page<User> page) {
        return PagedResponse.<UserResponse>builder()
                .content(page.getContent()
                        .stream()
                        .map(UserResponse::fromUser)
                        .collect(java.util.stream.Collectors.toList()))
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .pageSize(page.getSize())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .isFirst(page.isFirst())
                .isLast(page.isLast())
                .build();
    }
}