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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
    public String dashboard(Model model, Authentication authentication, HttpSession session,
                            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("Unauthenticated access to dashboard, redirecting to login.");
            return "redirect:/login";
        }

        User user = authService.getUserFromAuthentication(authentication);
        logger.info("User accessed dashboard: {}", user.getUsername());

        session.setAttribute("user", user);

        LocalDate selectedDate = date != null ? date : LocalDate.now();

        long totalUsers = userService.getAllUsers().size();
        long totalCoaches = userService.getAllCoaches().size();
        long totalBookings = bookingService.getAllBookings().size();
        long totalFeedbacks = feedbackService.getAllFeedbacks().size();

        Map<String, Long> dailyStats = new HashMap<>();
        try {
            dailyStats.put("usersToday", userService.getUsersByDate(selectedDate) != null ? (long) userService.getUsersByDate(selectedDate).size() : 0L);
            dailyStats.put("bookingsToday", bookingService.getBookingsByDate(selectedDate) != null ? (long) bookingService.getBookingsByDate(selectedDate).size() : 0L);
            dailyStats.put("coachesToday", userService.getCoachesByDate(selectedDate) != null ? (long) userService.getCoachesByDate(selectedDate).size() : 0L);
            dailyStats.put("feedbacksToday", feedbackService.getFeedbacksByDate(selectedDate) != null ? (long) feedbackService.getFeedbacksByDate(selectedDate).size() : 0L);
        } catch (Exception e) {
            logger.error("Error fetching daily statistics: {}", e.getMessage(), e);
            dailyStats.put("usersToday", 0L);
            dailyStats.put("bookingsToday", 0L);
            dailyStats.put("coachesToday", 0L);
            dailyStats.put("feedbacksToday", 0L);
        }

        Map<String, Double> revenueStats = new HashMap<>();
        revenueStats.put("dailyRevenue", bookingService.getRevenueByDate(selectedDate));
        revenueStats.put("monthlyRevenue", bookingService.getRevenueByMonth(selectedDate.getMonthValue(), selectedDate.getYear()));
        revenueStats.put("quarterlyRevenue", bookingService.getRevenueByQuarter((selectedDate.getMonthValue() - 1) / 3 + 1, selectedDate.getYear()));
        revenueStats.put("yearlyRevenue", bookingService.getRevenueByYear(selectedDate.getYear()));

        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalBookings", totalBookings);
        model.addAttribute("totalCoaches", totalCoaches);
        model.addAttribute("totalFeedbacks", totalFeedbacks);
        model.addAttribute("dailyStats", dailyStats);
        model.addAttribute("revenueStats", revenueStats);
        model.addAttribute("selectedDate", selectedDate);

        return "dashboard/dashboard";
    }
}