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

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

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

        // Dữ liệu cơ bản
        long totalUsers = userService.getAllUsers().size();
        long totalCoaches = userService.getAllCoaches().size();
        long totalBookings = bookingService.getAllBookings().size();
        long totalFeedbacks = feedbackService.getAllFeedbacks().size();

        // Dữ liệu thống kê theo ngày
        LocalDate today = LocalDate.now();
        Map<String, Long> dailyStats = new HashMap<>();
        try {
            dailyStats.put("usersToday", userService.getUsersByDate(today) != null ? (long) userService.getUsersByDate(today).size() : 0L);
            dailyStats.put("bookingsToday", bookingService.getBookingsByDate(today) != null ? (long) bookingService.getBookingsByDate(today).size() : 0L);
            dailyStats.put("coachesToday", userService.getCoachesByDate(today) != null ? (long) userService.getCoachesByDate(today).size() : 0L);
            dailyStats.put("feedbacksToday", feedbackService.getFeedbacksByDate(today) != null ? (long) feedbackService.getFeedbacksByDate(today).size() : 0L);
        } catch (Exception e) {
            logger.error("Error fetching daily statistics: {}", e.getMessage(), e);
            dailyStats.put("usersToday", 0L);
            dailyStats.put("bookingsToday", 0L);
            dailyStats.put("coachesToday", 0L);
            dailyStats.put("feedbacksToday", 0L);
        }

        // Dữ liệu doanh thu
        Map<String, Double> revenueStats = new HashMap<>();
        revenueStats.put("dailyRevenue", bookingService.getRevenueByDate(today));
        revenueStats.put("monthlyRevenue", bookingService.getRevenueByMonth(today.getMonthValue(), today.getYear()));
        revenueStats.put("quarterlyRevenue", bookingService.getRevenueByQuarter((today.getMonthValue() - 1) / 3 + 1, today.getYear()));
        revenueStats.put("yearlyRevenue", bookingService.getRevenueByYear(today.getYear()));

        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalBookings", totalBookings);
        model.addAttribute("totalCoaches", totalCoaches);
        model.addAttribute("totalFeedbacks", totalFeedbacks);
        model.addAttribute("dailyStats", dailyStats);
        model.addAttribute("revenueStats", revenueStats);

        return "dashboard/dashboard";
    }
}