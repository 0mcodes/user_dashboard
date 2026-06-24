package com.dashboard.userdashboard.dto.request;

import com.dashboard.userdashboard.model.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateRoleRequest {

    @NotNull(message = "Role is required")
    private Role role;
}