package com.example.kingpool.controller;

import com.example.kingpool.entity.User;
import com.example.kingpool.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);

    @Autowired
    private AuthService authService;

    @GetMapping
    public String viewProfile(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        User user = authService.getUserFromAuthentication(authentication);
        logger.info("Viewing profile for user: {}", user.getUsername());
        model.addAttribute("user", user);
        return "profile/view";
    }

    @GetMapping("/edit")
    public String editProfile(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        User user = authService.getUserFromAuthentication(authentication);
        logger.info("Editing profile for user: {}", user.getUsername());
        model.addAttribute("user", user);
        return "profile/edit";
    }

    @PostMapping("/edit")
    public String updateProfile(
            @ModelAttribute("user") User updatedUser,
            BindingResult result,
            @RequestParam("imageFile") MultipartFile imageFile,
            @RequestParam(value = "dateOfBirth", required = false) String dateOfBirthStr,
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        // Validate name and email
        if (updatedUser.getName() == null || updatedUser.getName().trim().isEmpty()) {
            result.rejectValue("name", "error.name", "Tên không được để trống");
        }
        if (updatedUser.getEmail() == null || updatedUser.getEmail().trim().isEmpty()) {
            result.rejectValue("email", "error.email", "Email không được để trống");
        } else if (!updatedUser.getEmail().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            result.rejectValue("email", "error.email", "Email không hợp lệ");
        }

        // Validate image file
        if (!imageFile.isEmpty()) {
            String contentType = imageFile.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                result.rejectValue("image", "error.image", "Vui lòng chọn một file ảnh (jpg, png, v.v.)");
            } else if (imageFile.getSize() > 5 * 1024 * 1024) { // Giới hạn 5MB
                result.rejectValue("image", "error.image", "Kích thước ảnh không được vượt quá 5MB");
            }
        }

        // Validate and parse dateOfBirth
        if (dateOfBirthStr != null && !dateOfBirthStr.isEmpty()) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate dateOfBirth = LocalDate.parse(dateOfBirthStr, formatter);
                if (dateOfBirth.isAfter(LocalDate.now())) {
                    result.rejectValue("dateOfBirth", "error.dateOfBirth", "Ngày sinh không được ở tương lai");
                }
                updatedUser.setDateOfBirth(dateOfBirth);
            } catch (DateTimeParseException e) {
                result.rejectValue("dateOfBirth", "error.dateOfBirth", "Ngày sinh không hợp lệ");
            }
        } else {
            updatedUser.setDateOfBirth(null);
        }

        if (result.hasErrors()) {
            logger.warn("Validation errors: {}", result.getAllErrors());
            model.addAttribute("user", updatedUser);
            return "profile/edit";
        }

        try {
            User currentUser = authService.getUserFromAuthentication(authentication);
            authService.updateUserProfile(currentUser, updatedUser, imageFile);
            redirectAttributes.addFlashAttribute("success", "Cập nhật hồ sơ thành công!");
            logger.info("Profile updated successfully for user: {}", currentUser.getUsername());
            return "redirect:/profile";
        } catch (Exception e) {
            logger.error("Error updating profile: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/profile/edit";
        }
    }

    @PostMapping("/change-password")
    public String changePassword(
            @RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmNewPassword") String confirmNewPassword,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        if (newPassword == null || newPassword.isEmpty() || confirmNewPassword == null || !newPassword.equals(confirmNewPassword)) {
            redirectAttributes.addFlashAttribute("passwordError", "Mật khẩu mới không hợp lệ hoặc không khớp");
            return "redirect:/profile/edit";
        }

        try {
            User currentUser = authService.getUserFromAuthentication(authentication);
            authService.changePassword(currentUser, currentPassword, newPassword, confirmNewPassword);
            redirectAttributes.addFlashAttribute("success", "Thay đổi mật khẩu thành công!");
            logger.info("Password changed successfully for user: {}", currentUser.getUsername());
            return "redirect:/profile/edit";
        } catch (Exception e) {
            logger.error("Error changing password: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("passwordError", e.getMessage());
            return "redirect:/profile/edit";
        }
    }
}