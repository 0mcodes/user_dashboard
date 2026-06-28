package com.dashboard.userdashboard.controller;

import com.dashboard.userdashboard.dto.request.UpdateRoleRequest;
import com.dashboard.userdashboard.model.User;
import com.dashboard.userdashboard.service.AuditService;
import com.dashboard.userdashboard.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/dashboard/admin")
@RequiredArgsConstructor
public class AdminWebController {

    private final UserService userService;
    private final AuditService auditService;

    @GetMapping("/home")
    public String home(Model model) {
        model.addAttribute("stats",
                userService.getDashboardStats());
        return "dashboard/admin/home";
    }

    @GetMapping("/users")
    public String users(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false)    String search,
            Model model) {

        model.addAttribute("users",
                userService.getAllUsers(page, size, "createdAt", search));
        model.addAttribute("search", search);
        model.addAttribute("currentPage", page);
        return "dashboard/admin/users";
    }

    @PostMapping("/users/{userId}/role")
    public String updateRole(
            @PathVariable Long userId,
            @RequestParam String role,
            @AuthenticationPrincipal User admin,
            RedirectAttributes redirectAttributes) {

        try {
            UpdateRoleRequest req = new UpdateRoleRequest();
            req.setRole(
                    com.dashboard.userdashboard.model.Role.valueOf(role));
            userService.updateUserRole(
                    userId, req, admin.getId(), admin.getEmail());
            redirectAttributes.addFlashAttribute("message",
                    "Role updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    e.getMessage());
        }
        return "redirect:/dashboard/admin/users";
    }

    @PostMapping("/users/{userId}/toggle")
    public String toggleStatus(
            @PathVariable Long userId,
            @RequestParam boolean enabled,
            @AuthenticationPrincipal User admin,
            RedirectAttributes redirectAttributes) {

        userService.toggleUserStatus(
                userId, enabled, admin.getId(), admin.getEmail());
        redirectAttributes.addFlashAttribute("message",
                "User " + (enabled ? "enabled" : "disabled") + ".");
        return "redirect:/dashboard/admin/users";
    }

    @PostMapping("/users/{userId}/delete")
    public String deleteUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal User admin,
            RedirectAttributes redirectAttributes) {

        userService.deleteUser(
                userId, admin.getId(), admin.getEmail());
        redirectAttributes.addFlashAttribute("message",
                "User deleted.");
        return "redirect:/dashboard/admin/users";
    }

    @GetMapping("/audit-logs")
    public String auditLogs(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {

        model.addAttribute("logs", auditService.getAllLogs(
                PageRequest.of(page, size,
                        Sort.by(Sort.Direction.DESC, "createdAt"))));
        return "dashboard/admin/audit-logs";
    }
}