package com.dashboard.userdashboard.controller.web;

import com.dashboard.userdashboard.dto.request.ChangePasswordRequest;
import com.dashboard.userdashboard.dto.request.UpdateProfileRequest;
import com.dashboard.userdashboard.dto.response.UserResponse;
import com.dashboard.userdashboard.exception.AppException;
import com.dashboard.userdashboard.model.User;
import com.dashboard.userdashboard.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/dashboard/user")
@RequiredArgsConstructor
public class UserWebController {

    private final UserService userService;

    @GetMapping("/home")
    public String home(@AuthenticationPrincipal User user, Model model) {
        UserResponse profile = userService.getProfile(user.getId());
        model.addAttribute("profile", profile);
        return "dashboard/user/home";
    }

    @GetMapping("/profile")
    public String profilePage(@AuthenticationPrincipal User user, Model model) {
        populateProfileModel(user, model);
        return "dashboard/user/profile";
    }

    @GetMapping("/users")
    public String viewUsers(
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int size,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String search,
            Model model) {
        model.addAttribute("users",
                userService.getAllUsers(page, size, "createdAt", search));
        model.addAttribute("search", search);
        return "dashboard/user/users";
    }

    @PostMapping("/profile")
    public String updateProfile(
            @AuthenticationPrincipal User user,
            @Valid @ModelAttribute("updateRequest") UpdateProfileRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            UserResponse profile = userService.getProfile(user.getId());
            model.addAttribute("profile", profile);
            model.addAttribute("passwordRequest", new ChangePasswordRequest());
            return "dashboard/user/profile";
        }

        try {
            userService.updateProfile(user.getId(), request);
            redirectAttributes.addFlashAttribute("message", "Profile updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dashboard/user/profile";
    }

    @PostMapping("/password")
    public String changePassword(
            @AuthenticationPrincipal User user,
            @Valid @ModelAttribute("passwordRequest") ChangePasswordRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            populateProfileModel(user, model);
            return "dashboard/user/profile";
        }

        try {
            userService.changePassword(user.getId(), request);
            redirectAttributes.addFlashAttribute("message", "Password changed successfully.");
        } catch (AppException e) {
            redirectAttributes.addFlashAttribute("passwordError", e.getMessage());
        }
        return "redirect:/dashboard/user/profile";
    }

    private void populateProfileModel(User user, Model model) {
        // Always load fresh from database — never use session user directly
        // The session user was loaded at login and may be stale
        UserResponse profile = userService.getProfile(user.getId());

        // Pre-fill the form with current database values
        UpdateProfileRequest updateRequest = new UpdateProfileRequest();
        updateRequest.setFirstName(profile.getFirstName());
        updateRequest.setLastName(profile.getLastName());
        updateRequest.setPhoneNumber(profile.getPhoneNumber());
        updateRequest.setBio(profile.getBio());
        updateRequest.setDateOfBirth(profile.getDateOfBirth());
        updateRequest.setLocation(profile.getLocation());

        model.addAttribute("profile", profile);
        model.addAttribute("updateRequest", updateRequest);
        model.addAttribute("passwordRequest", new ChangePasswordRequest());
    }
}
