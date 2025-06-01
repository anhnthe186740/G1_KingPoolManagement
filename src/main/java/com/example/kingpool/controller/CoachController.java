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
    @PreAuthorize("hasRole('Admin')")
    public String listCoaches(Model model) {
        // Sử dụng getUsersByRoleId(2) thay vì getAllCoaches()
        model.addAttribute("coaches", userService.getUsersByRoleId(2));
        return "admin/coach-list";
    }
}