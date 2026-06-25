package com.dashboard.userdashboard.controller;

import com.dashboard.userdashboard.dto.request.UpdateRoleRequest;
import com.dashboard.userdashboard.dto.response.ApiResponse;
import com.dashboard.userdashboard.dto.response.DashboardStatsResponse;
import com.dashboard.userdashboard.dto.response.PagedResponse;
import com.dashboard.userdashboard.dto.response.UserResponse;
import com.dashboard.userdashboard.model.AuditLog;
import com.dashboard.userdashboard.model.Role;
import com.dashboard.userdashboard.model.User;
import com.dashboard.userdashboard.service.AuditService;
import com.dashboard.userdashboard.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/manager")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_MANAGER','ROLE_ADMIN')")
@Tag(name = "Manager",
        description = "Team management — ROLE_MANAGER and ROLE_ADMIN")
@SecurityRequirement(name = "bearerAuth")
public class ManagerController {

    private final UserService userService;


    @GetMapping("/users")
    @Operation(summary = "View all users — read only",
            description = "Managers can view but cannot edit users "
                    + "directly. Supports search and pagination.")
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> viewUsers(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {

        return ResponseEntity.ok(ApiResponse.success(
                "Users retrieved",
                userService.getAllUsers(page, size, "createdAt", search)));
    }

    @PatchMapping("/users/{userId}/promote")
    @Operation(summary = "Promote a USER to MANAGER",
            description = "Managers can elevate regular users to "
                    + "manager level. Cannot assign ROLE_ADMIN — "
                    + "only admins can do that.")
    public ResponseEntity<ApiResponse<UserResponse>> promoteToManager(
            @PathVariable Long userId,
            @AuthenticationPrincipal User manager) {

        // Managers can ONLY promote to ROLE_MANAGER
        // They cannot promote to ROLE_ADMIN — that's enforced here
        // in the controller, not left to the caller to decide
        UpdateRoleRequest request = new UpdateRoleRequest();
        request.setRole(Role.ROLE_MANAGER);

        UserResponse updated = userService.updateUserRole(
                userId, request, manager.getId(), manager.getEmail());

        return ResponseEntity.ok(ApiResponse.success(
                "User promoted to Manager", updated));
    }

    @GetMapping("/stats")
    @Operation(summary = "View dashboard statistics",
            description = "Same stats as Admin. Managers use this "
                    + "for team overview.")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getStats() {

        return ResponseEntity.ok(ApiResponse.success(
                "Statistics retrieved",
                userService.getDashboardStats()));
    }

}