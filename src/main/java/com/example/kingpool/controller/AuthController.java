package com.example.kingpool.controller;

import com.example.kingpool.entity.User;
import com.example.kingpool.service.AuthService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @Autowired
    private AuthenticationManager authenticationManager;

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

    @PostMapping("/check-current-password")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> checkCurrentPassword(
            @RequestParam("currentPassword") String currentPassword,
            Authentication authentication) {
        Map<String, Boolean> response = new HashMap<>();
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                response.put("valid", false);
                return ResponseEntity.ok(response);
            }

            String username = authentication.getName();
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, currentPassword)
            );
            response.put("valid", true);
        } catch (AuthenticationException e) {
            response.put("valid", false);
        }
        return ResponseEntity.ok(response);
    }
}