package com.dashboard.userdashboard.controller;

import com.dashboard.userdashboard.dto.request.ChangePasswordRequest;
import com.dashboard.userdashboard.dto.request.UpdateProfileRequest;
import com.dashboard.userdashboard.dto.response.ApiResponse;
import com.dashboard.userdashboard.dto.response.UserResponse;
import com.dashboard.userdashboard.model.User;
import com.dashboard.userdashboard.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "User Profile",
        description = "Profile and password management for any authenticated user")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    @Operation(summary = "Get own profile",
            description = "Returns the full profile of the currently "
                    + "authenticated user.")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(
            @AuthenticationPrincipal User user) {

        UserResponse response = userService.getProfile(user.getId());

        return ResponseEntity.ok(
                ApiResponse.success("Profile retrieved", response));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update own profile",
            description = "Updates name, phone, bio, and profile picture. "
                    + "Email and password cannot be changed here.")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdateProfileRequest request) {

        UserResponse response =
                userService.updateProfile(user.getId(), request);

        return ResponseEntity.ok(
                ApiResponse.success("Profile updated successfully", response));
    }

    @PutMapping("/password")
    @Operation(summary = "Change own password",
            description = "Requires current password for verification. "
                    + "New password must meet complexity requirements.")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ChangePasswordRequest request) {

        userService.changePassword(user.getId(), request);

        return ResponseEntity.ok(
                ApiResponse.success("Password changed successfully"));
    }
}