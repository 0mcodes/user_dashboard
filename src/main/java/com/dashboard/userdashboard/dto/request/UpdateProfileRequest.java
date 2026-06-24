package com.dashboard.userdashboard.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be 2-50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be 2-50 characters")
    private String lastName;

    @Pattern(
            regexp = "^[+]?[0-9]{10,15}$",
            message = "Phone number must be 10-15 digits"
    )
    private String phoneNumber;

    @Size(max = 500, message = "Bio cannot exceed 500 characters")
    private String bio;

    private String profilePictureUrl;
}