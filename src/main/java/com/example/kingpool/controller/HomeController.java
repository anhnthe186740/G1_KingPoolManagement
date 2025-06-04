package com.example.kingpool.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String getRoot() {
        return "homepage/index";
    }

    @GetMapping("/homepage")
    public String getHomePage() {
        return "homepage/index";
    }

    @GetMapping("/user-homepage")
    public String getUserHomePage() {
        return "homepage/user-homepage";
    }

    @GetMapping("/dashboard")
    public String getDashboard() {
        return "dashboard/dashboard";
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
}
