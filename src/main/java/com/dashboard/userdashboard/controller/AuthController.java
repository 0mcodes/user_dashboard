package com.dashboard.userdashboard.controller;

import com.dashboard.userdashboard.dto.request.ForgotPasswordRequest;
import com.dashboard.userdashboard.dto.request.LoginRequest;
import com.dashboard.userdashboard.dto.request.RegisterRequest;
import com.dashboard.userdashboard.dto.request.ResetPasswordRequest;
import com.dashboard.userdashboard.dto.response.ApiResponse;
import com.dashboard.userdashboard.dto.response.AuthResponse;
import com.dashboard.userdashboard.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication",
        description = "Register, login, forgot password, reset password — all public")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user account",
            description = "Creates a new account with ROLE_USER. "
                    + "Returns a JWT token so the user is "
                    + "logged in immediately after registration.")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        AuthResponse response = authService.register(request);

        // 201 Created — a new resource was created
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Registration successful", response));
    }

    @PostMapping("/login")
    @Operation(summary = "Login and receive a JWT token",
            description = "Validates credentials and returns a signed "
                    + "JWT. Include this token in the Authorization "
                    + "header for all subsequent requests.")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse response = authService.login(request);

        return ResponseEntity.ok(
                ApiResponse.success("Login successful", response));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request a password reset email",
            description = "Sends a reset link to the email if it exists. "
                    + "Always returns success to prevent user enumeration.")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        authService.forgotPassword(request);

        // Always the same message — never reveal if email exists or not
        return ResponseEntity.ok(ApiResponse.success(
                "If this email is registered, a reset link has been sent."));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password using the emailed token",
            description = "Validates the token from the email link "
                    + "and applies the new password.")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        authService.resetPassword(request);

        return ResponseEntity.ok(ApiResponse.success(
                "Password reset successfully. You can now log in."));
    }
}