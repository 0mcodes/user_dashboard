package com.dashboard.userdashboard.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title       = "User Dashboard API",
                version     = "1.0",
                description = "Industry-ready user management system with JWT "
                        + "authentication, role-based access control, "
                        + "audit logging, and dashboard statistics.",
                contact     = @Contact(
                        name  = "Dashboard Team",
                        email = "admin@dashboard.com"
                )
        )
)
@SecurityScheme(
        name        = "bearerAuth",
        type        = SecuritySchemeType.HTTP,
        scheme      = "bearer",
        bearerFormat = "JWT",
        description  = "Paste your JWT token here (without the 'Bearer ' prefix). "
                + "Get a token from POST /api/auth/login first."
)
public class SwaggerConfig {
    // Springdoc reads these annotations and configures Swagger UI automatically
}