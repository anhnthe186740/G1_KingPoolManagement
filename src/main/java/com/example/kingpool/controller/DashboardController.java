package com.example.kingpool.controller;

import com.example.kingpool.entity.User;
import com.example.kingpool.service.AuthService;
import com.example.kingpool.service.BookingService;
import com.example.kingpool.service.FeedbackService;
import com.example.kingpool.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private AuthService authService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication, HttpSession session) {
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("Unauthenticated access to dashboard, redirecting to login.");
            return "redirect:/login";
        }

        User user = authService.getUserFromAuthentication(authentication);
        logger.info("User accessed dashboard: {}", user.getUsername());

        session.setAttribute("user", user);

        long totalUsers = userService.getAllUsers().size();
        long totalCoaches = userService.getAllCoaches().size();
        long totalBookings = bookingService.getAllBookings().size();
        long totalFeedbacks = feedbackService.getAllFeedbacks().size();

        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalCoaches", totalCoaches);
        model.addAttribute("totalBookings", totalBookings);
        model.addAttribute("totalFeedbacks", totalFeedbacks);

        return "dashboard/dashboard";
    }
}