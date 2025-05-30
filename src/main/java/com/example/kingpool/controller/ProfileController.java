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
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        // Tự kiểm tra validation cho name và email
        if (updatedUser.getName() == null || updatedUser.getName().trim().isEmpty()) {
            result.rejectValue("name", "error.name", "Tên không được để trống");
        }
        if (updatedUser.getEmail() == null || updatedUser.getEmail().trim().isEmpty()) {
            result.rejectValue("email", "error.email", "Email không được để trống");
        } else if (!updatedUser.getEmail().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            result.rejectValue("email", "error.email", "Email không hợp lệ");
        }

        // Kiểm tra file ảnh
        if (imageFile != null && !imageFile.isEmpty()) {
            String contentType = imageFile.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                result.rejectValue("image", "error.image", "Vui lòng chọn một file ảnh (jpg, png, v.v.)");
            }
        }

        if (result.hasErrors()) {
            logger.warn("Validation errors: {}", result.getAllErrors());
            model.addAttribute("user", updatedUser);
            return "profile/edit";
        }

        try {
            logger.info("Received updated user data: name={}, email={}, phoneNumber={}, gender={}, dateOfBirth={}, address={}",
                    updatedUser.getName(), updatedUser.getEmail(), updatedUser.getPhoneNumber(),
                    updatedUser.getGender(), updatedUser.getDateOfBirth(), updatedUser.getAddress());
            if (imageFile != null && !imageFile.isEmpty()) {
                logger.info("Received image file: {}", imageFile.getOriginalFilename());
            }

            User currentUser = authService.getUserFromAuthentication(authentication);
            authService.updateUserProfile(currentUser, updatedUser, imageFile);
            redirectAttributes.addFlashAttribute("success", "Cập nhật hồ sơ thành công!");
            logger.info("Profile updated successfully for user: {}", currentUser.getUsername());
            return "redirect:/profile"; // Chuyển hướng về /profile để hiển thị thông báo
        } catch (Exception e) {
            logger.error("Error updating profile: {}", e.getMessage());
            model.addAttribute("user", updatedUser);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/profile/edit";
        }
    }
}