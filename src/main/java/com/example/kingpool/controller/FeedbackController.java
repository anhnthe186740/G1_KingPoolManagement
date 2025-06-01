package com.example.kingpool.controller;

import com.example.kingpool.entity.Feedback;
import com.example.kingpool.entity.User;
import com.example.kingpool.service.FeedbackService;
import com.example.kingpool.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Optional;

@Controller
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private UserService userService;

    @GetMapping("/feedback")
    public String showFeedbackForm(Model model) {
        model.addAttribute("feedback", new Feedback());
        return "user/feedback";
    }

    @PostMapping("/feedback/submit")
    public String submitFeedback(
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("address") String address,
            @RequestParam("message") String message,
            @RequestParam("content") String content,
            @RequestParam("rating") Integer rating,
            RedirectAttributes redirectAttributes) {
        try {
            // Lấy thông tin người dùng hiện tại (nếu đã đăng nhập)
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = null;
            if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
                String username = authentication.getName();
                Optional<User> userOptional = userService.getUserByUsername(username);
                user = userOptional.orElse(null);
            }

            // Tạo feedback mới
            Feedback feedback = new Feedback();
            feedback.setName(name);
            feedback.setEmail(email);
            feedback.setPhoneNumber(phoneNumber);
            feedback.setAddress(address);
            feedback.setMessage(message);
            feedback.setContent(content);
            feedback.setRating(rating);
            feedback.setVisible(false);
            feedback.setSubmittedAt(LocalDateTime.now());
            feedback.setUser(user);

            // Lưu feedback
            feedbackService.saveFeedback(feedback);

            // Thêm thông báo thành công
            redirectAttributes.addFlashAttribute("successMessage", "Cảm ơn bạn đã gửi phản hồi, bấm vào Đồng ý để quay lại trang chủ chính");
            return "redirect:/user-homepage";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi gửi phản hồi: " + e.getMessage());
            return "redirect:/feedback";
        }
    }
}