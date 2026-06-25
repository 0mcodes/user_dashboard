package com.dashboard.userdashboard.controller;

import com.dashboard.userdashboard.dto.request.UpdateRoleRequest;
import com.dashboard.userdashboard.model.Role;
import com.dashboard.userdashboard.model.User;
import com.dashboard.userdashboard.service.AuditService;
import com.dashboard.userdashboard.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/dashboard/manager")
@PreAuthorize("hasAnyAuthority('ROLE_MANAGER','ROLE_ADMIN')")
@RequiredArgsConstructor
public class ManagerWebController {

    private final UserService userService;
    private final AuditService auditService;

    @GetMapping("/home")
    public String home(Model model) {
        model.addAttribute("stats",
                userService.getDashboardStats());
        return "dashboard/manager/home";
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
        return "dashboard/manager/users";
    }

    @PostMapping("/users/{userId}/promote")
    public String promote(
            @PathVariable Long userId,
            @AuthenticationPrincipal User manager,
            RedirectAttributes redirectAttributes) {

        UpdateRoleRequest req = new UpdateRoleRequest();
        req.setRole(Role.ROLE_MANAGER);
        userService.updateUserRole(
                userId, req, manager.getId(), manager.getEmail());
        redirectAttributes.addFlashAttribute("message",
                "User promoted to Manager.");
        return "redirect:/dashboard/manager/users";
    }

    @GetMapping("/audit-logs/{userId}")
    public String userLogs(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        model.addAttribute("logs", auditService.getLogsByUser(
                userId, PageRequest.of(page, 20,
                        Sort.by(Sort.Direction.DESC, "createdAt"))));
        model.addAttribute("userId", userId);
        return "dashboard/manager/audit-logs";
    }
}