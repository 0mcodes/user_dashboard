package com.dashboard.userdashboard.controller;

import com.dashboard.userdashboard.dto.request.ForgotPasswordRequest;
import com.dashboard.userdashboard.dto.request.RegisterRequest;
import com.dashboard.userdashboard.dto.request.ResetPasswordRequest;
import com.dashboard.userdashboard.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthWebController {

    private final AuthService authService;

    // ── LOGIN ─────────────────────────────────────────────────────
    @GetMapping("/login")
    public String loginPage(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String logout,
            Model model) {

        if (error != null) {
            model.addAttribute("error",
                    "Invalid email or password. Please try again.");
        }
        if (logout != null) {
            model.addAttribute("message",
                    "You have been logged out successfully.");
        }
        return "auth/login";
    }

    // ── REGISTER ──────────────────────────────────────────────────
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("registerRequest")
            RegisterRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            authService.register(request);
            redirectAttributes.addFlashAttribute("message",
                    "Registration successful! Please log in.");
            return "redirect:/login";

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
    }

    // ── FORGOT PASSWORD ───────────────────────────────────────────
    @GetMapping("/forgot-password")
    public String forgotPasswordPage(Model model) {
        model.addAttribute("forgotRequest",
                new ForgotPasswordRequest());
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(
            @Valid @ModelAttribute("forgotRequest")
            ForgotPasswordRequest request,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            return "auth/forgot-password";
        }

        authService.forgotPassword(request);
        model.addAttribute("message",
                "If this email is registered, a reset link has been sent.");
        return "auth/forgot-password";
    }

    // ── RESET PASSWORD ────────────────────────────────────────────
    @GetMapping("/reset-password")
    public String resetPasswordPage(
            @RequestParam String token, Model model) {
        model.addAttribute("token", token);
        model.addAttribute("resetRequest", new ResetPasswordRequest());
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(
            @Valid @ModelAttribute("resetRequest")
            ResetPasswordRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("token", request.getToken());
            return "auth/reset-password";
        }

        try {
            authService.resetPassword(request);
            redirectAttributes.addFlashAttribute("message",
                    "Password reset successfully. Please log in.");
            return "redirect:/login";

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("token", request.getToken());
            return "auth/reset-password";
        }
    }
}