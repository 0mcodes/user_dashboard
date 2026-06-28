package com.dashboard.userdashboard.controller;

import com.dashboard.userdashboard.dto.request.ChangePasswordRequest;
import com.dashboard.userdashboard.dto.request.UpdateProfileRequest;
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
    public String home(@AuthenticationPrincipal User user,
                       Model model) {
        model.addAttribute("profile",
                userService.getProfile(user.getId()));
        return "dashboard/user/home";
    }

    @GetMapping("/profile")
    public String profilePage(
            @AuthenticationPrincipal User user, Model model) {
        model.addAttribute("profile",
                userService.getProfile(user.getId()));
        model.addAttribute("updateRequest",
                new UpdateProfileRequest());
        model.addAttribute("passwordRequest",
                new ChangePasswordRequest());
        return "dashboard/user/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(
            @AuthenticationPrincipal User user,
            @Valid @ModelAttribute("updateRequest")
            UpdateProfileRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("profile",
                    userService.getProfile(user.getId()));
            return "dashboard/user/profile";
        }

        userService.updateProfile(user.getId(), request);
        redirectAttributes.addFlashAttribute("message",
                "Profile updated successfully.");
        return "redirect:/dashboard/user/profile";
    }

    @PostMapping("/password")
    public String changePassword(
            @AuthenticationPrincipal User user,
            @Valid @ModelAttribute("passwordRequest")
            ChangePasswordRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("profile",
                    userService.getProfile(user.getId()));
            return "dashboard/user/profile";
        }

        try {
            userService.changePassword(user.getId(), request);
            redirectAttributes.addFlashAttribute("message",
                    "Password changed successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    e.getMessage());
        }
        return "redirect:/dashboard/user/profile";
    }


    private void populateProfileModel(User user, Model model) {
        var profile = userService.getProfile(user.getId());

        // Pre-fill the update form with current profile values
        // so the user sees their existing data, not blank fields
        UpdateProfileRequest updateRequest = new UpdateProfileRequest();
        updateRequest.setFirstName(profile.getFirstName());
        updateRequest.setLastName(profile.getLastName());
        updateRequest.setPhoneNumber(profile.getPhoneNumber());
        updateRequest.setBio(profile.getBio());
        updateRequest.setProfilePictureUrl(profile.getProfilePictureUrl());

        model.addAttribute("profile", profile);
        model.addAttribute("updateRequest", updateRequest);
        model.addAttribute("passwordRequest", new ChangePasswordRequest());
    }
}