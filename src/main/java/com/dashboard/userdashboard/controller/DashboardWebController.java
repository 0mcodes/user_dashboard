package com.dashboard.userdashboard.controller;

import com.dashboard.userdashboard.model.Role;
import com.dashboard.userdashboard.model.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dashboard")
public class DashboardWebController {

    // After login, redirect to the right dashboard based on role
    @GetMapping("/redirect")
    public String redirect(@AuthenticationPrincipal User user) {
        if (user.getRole() == Role.ROLE_ADMIN) {
            return "redirect:/dashboard/admin/home";
        } else if (user.getRole() == Role.ROLE_MANAGER) {
            return "redirect:/dashboard/manager/home";
        } else {
            return "redirect:/dashboard/user/home";
        }
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "dashboard/access-denied";
    }
}