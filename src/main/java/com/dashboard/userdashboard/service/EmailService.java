package com.dashboard.userdashboard.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${spring.mail.username:noreply@dashboard.com}")
    private String fromEmail;

    @Async
    public void sendPasswordResetEmail(String toEmail,
                                       String firstName,
                                       String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Password Reset Request — User Dashboard");
            message.setText(
                    "Hi " + firstName + ",\n\n" +
                            "You requested a password reset.\n\n" +
                            "Click the link below to reset your password " +
                            "(valid for 15 minutes):\n\n" +
                            frontendUrl + "/reset-password?token=" + token + "\n\n" +
                            "If you did not request this, ignore this email.\n" +
                            "Your password will remain unchanged.\n\n" +
                            "— The Dashboard Team"
            );
            mailSender.send(message);
            log.info("Password reset email sent to {}", toEmail);

        } catch (Exception e) {
            // Email failure must NOT fail the main request
            // The user already got a 200 response — log and move on
            log.error("Failed to send reset email to {}: {}",
                    toEmail, e.getMessage());
        }
    }

    @Async
    public void sendWelcomeEmail(String toEmail, String firstName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Welcome to User Dashboard!");
            message.setText(
                    "Hi " + firstName + ",\n\n" +
                            "Welcome! Your account has been created successfully.\n\n" +
                            "Log in at: " + frontendUrl + "/login\n\n" +
                            "— The Dashboard Team"
            );
            mailSender.send(message);

        } catch (Exception e) {
            log.error("Failed to send welcome email to {}: {}",
                    toEmail, e.getMessage());
        }
    }
}