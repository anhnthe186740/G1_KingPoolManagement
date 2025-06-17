package com.example.kingpool.controller;

import com.example.kingpool.entity.User;
import com.example.kingpool.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/users")
    @PreAuthorize("hasRole('Admin')")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/user-list";
    }

    @GetMapping("/users/add")
    @PreAuthorize("hasRole('Admin')")
    public String showAddUserForm(Model model) {
        model.addAttribute("user", new User());
        return "admin/user-form";
    }

    @PostMapping("/users/add")
    @PreAuthorize("hasRole('Admin')")
    public String addUser(@Valid @ModelAttribute("user") User user, 
                          BindingResult result, 
                          @RequestParam("roleName") String roleName,
                          RedirectAttributes redirectAttributes, 
                          Model model) {
        if (result.hasErrors()) {
            return "admin/user-form";
        }
        try {
            userService.addUser(user, roleName);
            redirectAttributes.addFlashAttribute("success", "Thêm người dùng thành công!");
            return "redirect:/admin/users";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "admin/user-form";
        }
    }

    @GetMapping("/users/edit/{id}")
    @PreAuthorize("hasRole('Admin')")
    public String showEditUserForm(@PathVariable("id") Integer id, Model model) {
        User user = userService.getAllUsers().stream()
                .filter(u -> u.getUserId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("user", user);
        return "admin/user-form";
    }

    @PostMapping("/users/edit/{id}")
    @PreAuthorize("hasRole('Admin')")
    public String editUser(@PathVariable("id") Integer id, 
                           @Valid @ModelAttribute("user") User user, 
                           BindingResult result, 
                           @RequestParam("roleName") String roleName,
                           RedirectAttributes redirectAttributes, 
                           Model model) {
        if (result.hasErrors()) {
            return "admin/user-form";
        }
        try {
            userService.updateUser(id, user, roleName);
            redirectAttributes.addFlashAttribute("success", "Cập nhật người dùng thành công!");
            return "redirect:/admin/users";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "admin/user-form";
        }
    }

    @PostMapping("/users/delete/{id}")
    @PreAuthorize("hasRole('Admin')")
    public String deleteUser(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("success", "Xóa người dùng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/change-role/{id}")
    @PreAuthorize("hasRole('Admin')")
    public String changeUserRole(@PathVariable("id") Integer id, 
                                 @RequestParam("roleName") String roleName, 
                                 RedirectAttributes redirectAttributes) {
        try {
            userService.changeUserRole(id, roleName);
            redirectAttributes.addFlashAttribute("success", "Đổi vai trò thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/users/view/{id}")
    @PreAuthorize("hasRole('Admin')")
    public String viewUserDetails(@PathVariable("id") Integer id, Model model) {
        User user = userService.getAllUsers().stream()
                .filter(u -> u.getUserId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("user", user);
        return "admin/user-details";
    }
}