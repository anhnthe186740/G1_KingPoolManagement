package com.example.kingpool.controller;

import com.example.kingpool.entity.User;
import com.example.kingpool.service.AuthService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;

@Controller
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("user") User user,
            BindingResult result, @RequestParam("g-recaptcha-response") String recaptchaToken,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng kiểm tra lại thông tin.");
            return "redirect:/register";
        }
        if (recaptchaToken == null || recaptchaToken.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng xác minh reCAPTCHA.");
            return "redirect:/register";
        }
        try {
            authService.register(user);
            redirectAttributes.addFlashAttribute("success", "Đăng ký thành công, vui lòng đăng nhập!");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    // adada
}