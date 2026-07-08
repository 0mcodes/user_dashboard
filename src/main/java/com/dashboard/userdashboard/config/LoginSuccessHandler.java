package com.dashboard.userdashboard.config;

import com.dashboard.userdashboard.model.User;
import com.dashboard.userdashboard.repository.UserRepository;
import com.dashboard.userdashboard.service.AuditService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final AuditService auditService;

    // Constructor to set the default redirect target
    @jakarta.annotation.PostConstruct
    public void init() {
        setDefaultTargetUrl("/dashboard/redirect");
        setAlwaysUseDefaultTargetUrl(true);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws ServletException, IOException {

        User user = (User) authentication.getPrincipal();

        // Update last login timestamp
        userRepository.updateLastLoginAt(user.getId(), LocalDateTime.now());

        // Log the login event
        String ipAddress = getClientIp(request);
        auditService.log(
                user.getId(),
                user.getEmail(),
                "USER_LOGIN",
                "User",
                user.getId(),
                "User logged in via web dashboard from IP: " + ipAddress
        );

        // Continue with default redirect behaviour (defaultSuccessUrl)
        super.onAuthenticationSuccess(request, response, authentication);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}