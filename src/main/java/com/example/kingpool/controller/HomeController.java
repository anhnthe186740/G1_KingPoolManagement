package com.example.kingpool.controller;

import com.example.kingpool.entity.User;
import com.example.kingpool.repository.UserRepository;
import com.example.kingpool.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final AuthService authService;
    private final UserRepository userRepository; // Thêm dependency mới

    @GetMapping("/login")
    public String getMethodName(Model model) {
        model.addAttribute("user", new User());
        return "auth/login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    @GetMapping("/")
    public String getMethodName() {
        return "redirect:/homepage";
    }

    @GetMapping("/homepage")
    public String getHomePage() {
        return "homepage/index";
    }

    @GetMapping("/profile")
    public String viewProfile(Model model, Principal principal) {
        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
        model.addAttribute("user", user);
        return "auth/profile";
    }

    @GetMapping("/logout")
    public String getMethodName2() {
        return "/";
    }

}
