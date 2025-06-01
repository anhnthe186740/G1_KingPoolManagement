package com.example.kingpool.controller;

import com.example.kingpool.entity.User;
import com.example.kingpool.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("user") User user,
            BindingResult result, @RequestParam(value = "g-recaptcha-response", required = false) String recaptchaToken,
            RedirectAttributes redirectAttributes) {
        System.out.println("Received registration request: " + user.toString());
        System.out.println("reCAPTCHA token: " + recaptchaToken);

        // Kiểm tra lỗi validation
        if (result.hasErrors()) {
            System.out.println("Validation errors: " + result.getAllErrors());
            redirectAttributes.addFlashAttribute("error", "Vui lòng kiểm tra lại thông tin. Errors: " + result.getAllErrors());
            return "redirect:/register";
        }

        // Tạm thời bỏ kiểm tra reCAPTCHA để test
        /*
        if (recaptchaToken == null || recaptchaToken.isEmpty()) {
            System.out.println("reCAPTCHA token missing");
            redirectAttributes.addFlashAttribute("error", "Vui lòng xác minh reCAPTCHA.");
            return "redirect:/register";
        }
        */

        try {
            authService.register(user);
            redirectAttributes.addFlashAttribute("success", "Đăng ký thành công, vui lòng đăng nhập!");
            return "redirect:/login";
        } catch (Exception e) {
            System.out.println("Registration failed: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }
}