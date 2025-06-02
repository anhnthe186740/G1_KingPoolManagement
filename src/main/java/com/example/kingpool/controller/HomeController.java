package com.example.kingpool.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.kingpool.entity.User;
import com.example.kingpool.service.AuthService;

import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final AuthService authService;

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
    
    @GetMapping("/logout")
    public String getMethodName2() {
        return "/";
    }

}
