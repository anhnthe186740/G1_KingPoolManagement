package com.example.kingpool.controller;

import com.example.kingpool.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String getRoot(Model model) {
        return getHomePage(model); // Gọi chung về /homepage
    }

    @GetMapping("/homepage")
    public String getHomePage(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
if (auth != null) {
        Object principal = auth.getPrincipal();

        // Nếu login bằng form thường
        if (principal instanceof CustomUserDetails userDetails) {
            model.addAttribute("name", userDetails.getName());
        }

        // Nếu login bằng Google OAuth2
        else if (principal instanceof OAuth2User oauthUser) {
            String name = oauthUser.getAttribute("name");
            model.addAttribute("name", name);
        }
    }
        return "homepage/homepage";
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
