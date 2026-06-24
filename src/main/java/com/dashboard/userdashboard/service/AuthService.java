package com.dashboard.userdashboard.service;

import com.dashboard.userdashboard.dto.request.*;
import com.dashboard.userdashboard.dto.response.AuthResponse;
import com.dashboard.userdashboard.exception.AppException;
import com.dashboard.userdashboard.model.AuditLog;
import com.dashboard.userdashboard.model.PasswordResetToken;
import com.dashboard.userdashboard.model.User;
import com.dashboard.userdashboard.repository.PasswordResetTokenRepository;
import com.dashboard.userdashboard.repository.UserRepository;
import com.dashboard.userdashboard.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final AuditService auditService;
    private final EmailService emailService;

    @Value("${app.password-reset.expiry-minutes:15}")
    private int passwordResetExpiryMinutes;

    // ── REGISTER ──────────────────────────────────────────────────
    @Transactional
    public AuthResponse register(RegisterRequest request) {

        // Step 1 — Is this email already taken?
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(
                    "An account with this email already exists",
                    HttpStatus.CONFLICT
            );
        }

        // Step 2 — Do the two passwords match?
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new AppException(
                    "Passwords do not match",
                    HttpStatus.BAD_REQUEST
            );
        }

        // Step 3 — Build the User entity
        // Notice: role is NOT taken from the request — we set it ourselves
        User user = User.builder()
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .email(request.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .build();
        // role defaults to ROLE_USER from @Builder.Default on the entity

        // Step 4 — Save to database
        User savedUser = userRepository.save(user);
        log.info("New user registered: {}", savedUser.getEmail());

        // Step 5 — Generate JWT so user is logged in immediately
        String token = jwtUtil.generateToken(savedUser);

        // Step 6 — Audit log (async — does not delay the response)
        auditService.log(
                savedUser.getId(),
                savedUser.getEmail(),
                AuditLog.Actions.USER_REGISTERED,
                "User",
                savedUser.getId(),
                "New user registered: " + savedUser.getFullName()
        );

        // Step 7 — Welcome email (async)
        emailService.sendWelcomeEmail(
                savedUser.getEmail(),
                savedUser.getFirstName()
        );

        return AuthResponse.builder()
                .token(token)
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .role(savedUser.getRole())
                .message("Registration successful! Welcome, "
                        + savedUser.getFirstName() + "!")
                .build();
    }

    // ── LOGIN ─────────────────────────────────────────────────────
    @Transactional
    public AuthResponse login(LoginRequest request) {

        try {
            // This ONE call does everything:
            // 1. Loads user by email via CustomUserDetailsService
            // 2. BCrypt-compares the submitted password with the stored hash
            // 3. Throws BadCredentialsException if either step fails
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail().toLowerCase().trim(),
                            request.getPassword()
                    )
            );

        } catch (BadCredentialsException e) {
            // Log failed attempt — repeated failures = possible attack
            auditService.logFailure(
                    null,
                    request.getEmail(),
                    AuditLog.Actions.USER_LOGIN_FAILED,
                    "Failed login attempt for: " + request.getEmail(),
                    "Invalid credentials"
            );
            throw e; // GlobalExceptionHandler returns 401
        }

        // Authentication passed — load the full user object
        User user = userRepository
                .findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() ->
                        new AppException("User not found", HttpStatus.NOT_FOUND));

        // Update last login timestamp
        userRepository.updateLastLoginAt(user.getId(), LocalDateTime.now());

        // Generate JWT token
        String token = jwtUtil.generateToken(user);

        // Audit log — success
        auditService.log(
                user.getId(),
                user.getEmail(),
                AuditLog.Actions.USER_LOGIN,
                "User",
                user.getId(),
                "User logged in successfully"
        );

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .message("Welcome back, " + user.getFirstName() + "!")
                .build();
    }

    // ── FORGOT PASSWORD ───────────────────────────────────────────
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail().toLowerCase().trim();

        // SECURITY: We always return the same response
        // whether the email exists or not.
        // This prevents user enumeration attacks.
        userRepository.findByEmail(email).ifPresent(user -> {

            // Invalidate any existing active tokens for this email
            // One active reset link at a time per user
            List<PasswordResetToken> activeTokens =
                    passwordResetTokenRepository.findActiveTokensByEmail(email);

            activeTokens.forEach(t -> {
                t.setUsed(true);
                passwordResetTokenRepository.save(t);
            });

            // Create new token — UUID generated automatically in entity
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .email(email)
                    .expiresAt(LocalDateTime.now()
                            .plusMinutes(passwordResetExpiryMinutes))
                    .build();

            passwordResetTokenRepository.save(resetToken);

            // Send the reset email (async)
            emailService.sendPasswordResetEmail(
                    email,
                    user.getFirstName(),
                    resetToken.getToken()
            );

            auditService.log(
                    user.getId(), email,
                    AuditLog.Actions.PASSWORD_RESET_REQ,
                    "User", user.getId(),
                    "Password reset requested"
            );
        });

        // We fall through here for both found and not-found cases
        // Same code path = identical response time = no timing attack
    }

    // ── RESET PASSWORD ────────────────────────────────────────────
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {

        // Check passwords match first — quick fail
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new AppException(
                    "Passwords do not match",
                    HttpStatus.BAD_REQUEST
            );
        }

        // Find the token
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByToken(request.getToken())
                .orElseThrow(() ->
                        new AppException(
                                "Invalid or expired reset link. Please request a new one.",
                                HttpStatus.BAD_REQUEST
                        ));

        // Check it hasn't expired
        if (resetToken.isExpired()) {
            throw new AppException(
                    "This reset link has expired. Please request a new one.",
                    HttpStatus.BAD_REQUEST
            );
        }

        // Check it hasn't been used already
        if (resetToken.isUsed()) {
            throw new AppException(
                    "This reset link has already been used.",
                    HttpStatus.BAD_REQUEST
            );
        }

        // Find the user and update their password
        User user = userRepository
                .findByEmail(resetToken.getEmail())
                .orElseThrow(() ->
                        new AppException("User not found", HttpStatus.NOT_FOUND));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Mark token as used — prevents replay attacks
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        auditService.log(
                user.getId(), user.getEmail(),
                AuditLog.Actions.PASSWORD_RESET_DONE,
                "User", user.getId(),
                "Password reset completed"
        );
    }
}