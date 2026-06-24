package com.dashboard.userdashboard.dto.response;

import com.dashboard.userdashboard.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;

    @Builder.Default
    private String tokenType = "Bearer";

    private Long userId;
    private String email;
    private String fullName;
    private Role role;
    private String message;
}