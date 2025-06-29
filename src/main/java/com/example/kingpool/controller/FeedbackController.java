package com.example.kingpool.controller;

import com.example.kingpool.entity.Feedback;
import com.example.kingpool.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin")
public class FeedbackController {
    @Autowired
    private FeedbackService feedbackService;

    @GetMapping("/feedbacks")
    @PreAuthorize("hasRole('ADMIN')")
    public String viewFeedbacks(@RequestParam(defaultValue = "0") int page, Model model) {
        Pageable pageable = PageRequest.of(page, 5);
        Page<Feedback> feedbackPage = feedbackService.findAll(pageable);
        model.addAttribute("feedbacks", feedbackPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", feedbackPage.getTotalPages());
        return "feedback/feedbacks";
    }

    @GetMapping("/feedbacks/respond")
    @PreAuthorize("hasRole('ADMIN')")
    public String showRespondFeedbackForm(@RequestParam("feedbackId") Long feedbackId, Model model) {
        Feedback feedback = feedbackService.getFeedbackById(feedbackId);
        if (feedback == null) {
            return "redirect:/admin/feedbacks";
        }
        model.addAttribute("feedback", feedback);
        return "feedback/respond-feedback";
    }

    @PostMapping("/feedbacks/respond")
    @PreAuthorize("hasRole('ADMIN')")
    public String respondToFeedback(@RequestParam("feedbackId") Long feedbackId, 
                                    @RequestParam("response") String response) {
        feedbackService.respondToFeedback(feedbackId, response);
        return "redirect:/admin/feedbacks";
    }
}