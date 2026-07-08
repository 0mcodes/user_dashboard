package com.dashboard.userdashboard.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class RegisterRequest {

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be 2-50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be 2-50 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password must contain uppercase, lowercase, number, and special character"
    )
    private String password;

    @NotBlank(message = "Please confirm your password")
    private String confirmPassword;

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^[+]?[0-9]{10,15}$",
            message = "Phone number must be 10-15 digits"
    )
    private String phoneNumber;

    @Past(message = "Date of birth must be in the past")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    @Size(max = 100, message = "Location cannot exceed 100 characters")
    private String location;
}