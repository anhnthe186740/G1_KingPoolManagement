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
    

    // @PostMapping("/login")
    // public String login(@ModelAttribute("user") User user, Model model) {
    // User loggedInUser = authService.login(user);
    // if (loggedInUser == null) {
    // model.addAttribute("error", "Invalid username or password");
    // return "auth/login";
    // }
    // Claims claim = jwtUtil.getClaimsFromToken(loggedInUser.getToken());
    // User newUser = new User();
    // newUser.setUsername(claim.getSubject());
    // newUser.setEmail(claim.get("email", String.class));
    // newUser.setRole(claim.get("role", String.class));
    // System.out.println(claim.get("role", String.class));
    // System.out.println(claim.get("email", String.class));
    // System.out.println(claim.getSubject());
    // model.addAttribute("user", newUser);
    // return "auth/profile";
    // }

    @GetMapping("/logout")
    public String getMethodName2() {
        return "/";
    }

}
