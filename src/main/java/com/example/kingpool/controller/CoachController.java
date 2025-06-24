package com.example.kingpool.controller;

import com.example.kingpool.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class CoachController {
    private final UserService userService;

    @GetMapping("/coaches")
    @PreAuthorize("hasRole('ADMIN')")
    public String listCoaches(Model model) {
        model.addAttribute("coaches", userService.getAllCoaches());
        return "admin/coach-list";
    }
}