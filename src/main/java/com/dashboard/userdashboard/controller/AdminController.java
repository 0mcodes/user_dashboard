package com.dashboard.userdashboard.controller;

import com.dashboard.userdashboard.dto.request.UpdateRoleRequest;
import com.dashboard.userdashboard.dto.response.ApiResponse;
import com.dashboard.userdashboard.dto.response.DashboardStatsResponse;
import com.dashboard.userdashboard.dto.response.PagedResponse;
import com.dashboard.userdashboard.dto.response.UserResponse;
import com.dashboard.userdashboard.model.AuditLog;
import com.dashboard.userdashboard.model.User;
import com.dashboard.userdashboard.service.AuditService;
import com.dashboard.userdashboard.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@Tag(name = "Admin",
        description = "Full user management — ROLE_ADMIN only")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final UserService userService;
    private final AuditService auditService;

    @GetMapping("/users")
    @Operation(summary = "List all users",
            description = "Supports search across name and email, "
                    + "pagination, and sorting.")
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false) String search) {

        return ResponseEntity.ok(ApiResponse.success(
                "Users retrieved",
                userService.getAllUsers(page, size, sortBy, search)));
    }

    @PatchMapping("/users/{userId}/role")
    @Operation(summary = "Change a user's role",
            description = "Assigns ROLE_USER, ROLE_MANAGER, or "
                    + "ROLE_ADMIN to the specified user.")
    public ResponseEntity<ApiResponse<UserResponse>> updateRole(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateRoleRequest request,
            @AuthenticationPrincipal User admin) {

        UserResponse updated = userService.updateUserRole(
                userId, request, admin.getId(), admin.getEmail());

        return ResponseEntity.ok(
                ApiResponse.success("Role updated successfully", updated));
    }

    @PatchMapping("/users/{userId}/enable")
    @Operation(summary = "Enable or disable a user account",
            description = "Pass ?enabled=false to disable, "
                    + "?enabled=true to re-enable.")
    public ResponseEntity<ApiResponse<Void>> toggleUserStatus(
            @PathVariable Long userId,
            @RequestParam boolean enabled,
            @AuthenticationPrincipal User admin) {

        userService.toggleUserStatus(
                userId, enabled, admin.getId(), admin.getEmail());

        String message = enabled
                ? "User account enabled"
                : "User account disabled";

        return ResponseEntity.ok(ApiResponse.success(message));
    }

    @DeleteMapping("/users/{userId}")
    @Operation(summary = "Permanently delete a user",
            description = "Deletes the user and all associated data. "
                    + "This action cannot be undone.")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal User admin) {

        userService.deleteUser(userId, admin.getId(), admin.getEmail());

        return ResponseEntity.ok(
                ApiResponse.success("User deleted permanently"));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get dashboard statistics",
            description = "Returns counts of users by role, "
                    + "new signups, failed logins, and audit log totals.")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getStats() {

        return ResponseEntity.ok(ApiResponse.success(
                "Statistics retrieved",
                userService.getDashboardStats()));
    }

    @GetMapping("/audit-logs")
    @Operation(summary = "View all system audit logs",
            description = "Paginated list of all audit events, "
                    + "newest first.")
    public ResponseEntity<ApiResponse<Page<AuditLog>>> getAuditLogs(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<AuditLog> logs = auditService.getAllLogs(
                PageRequest.of(page, size,
                        Sort.by(Sort.Direction.DESC, "createdAt")));

        return ResponseEntity.ok(
                ApiResponse.success("Audit logs retrieved", logs));
    }

    @GetMapping("/audit-logs/user/{userId}")
    @Operation(summary = "View audit logs for a specific user",
            description = "Returns all audit events where this "
                    + "user was the actor or the target.")
    public ResponseEntity<ApiResponse<Page<AuditLog>>> getAuditLogsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<AuditLog> logs = auditService.getLogsByUser(
                userId,
                PageRequest.of(page, size,
                        Sort.by(Sort.Direction.DESC, "createdAt")));

        return ResponseEntity.ok(
                ApiResponse.success("User audit logs retrieved", logs));
    }
}