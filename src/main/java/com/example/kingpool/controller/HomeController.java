package com.example.kingpool.controller;

import com.example.kingpool.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    private FeedbackService feedbackService;

    @GetMapping("/")
    public String getRoot(Model model) {
        model.addAttribute("visibleFeedbacks", feedbackService.getVisibleFeedbacks());
        return "homepage/index";
    }

    @GetMapping("/homepage")
    public String getHomePage(Model model) {
        model.addAttribute("visibleFeedbacks", feedbackService.getVisibleFeedbacks());
        return "homepage/index";
    }

    @GetMapping("/user-homepage")
    public String getUserHomePage() {
        return "homepage/user-homepage";
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

    @GetMapping("/about")
    public String getAbout() {
        return "homepage/about";
    }
}