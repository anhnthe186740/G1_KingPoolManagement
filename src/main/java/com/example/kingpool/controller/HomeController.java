package com.example.kingpool.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String getRoot() {
        return "homepage/index"; // Hiển thị homepage cho người dùng chưa đăng nhập
    }

    @GetMapping("/homepage")
    public String getHomePage() {
        return "homepage/index"; // Homepage sau khi user đăng nhập
    }

    @GetMapping("/dashboard")
    public String getDashboard() {
        return "dashboard/dashboard"; // Dashboard cho admin
    }

    @GetMapping("/login")
    public String getLogin(Model model) {
        model.addAttribute("user", new com.example.kingpool.entity.User());
        return "auth/login";
    }

    @GetMapping("/register")
    public String getRegister(Model model) {
        model.addAttribute("user", new com.example.kingpool.entity.User());
        return "auth/register";
    }

    @GetMapping("/logout")
    public String getLogout() {
        return "redirect:/";
    }
}