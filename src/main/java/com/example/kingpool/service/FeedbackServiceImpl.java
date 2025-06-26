package com.example.kingpool.service;

import com.example.kingpool.entity.Feedback;
import com.example.kingpool.repository.FeedbackRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FeedbackServiceImpl implements FeedbackService {

    private static final Logger logger = LoggerFactory.getLogger(FeedbackServiceImpl.class);

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Override
    public Page<Feedback> findAll(Pageable pageable) {
        logger.debug("Fetching feedbacks with pagination");
        try {
            Page<Feedback> feedbacks = feedbackRepository.findAll(pageable);
            logger.info("Found {} feedbacks on page", feedbacks.getNumberOfElements());
            return feedbacks;
        } catch (Exception e) {
            logger.error("Error fetching feedbacks: {}", e.getMessage());
            throw new RuntimeException("Không thể lấy danh sách phản hồi: " + e.getMessage());
        }
    }

    @Override
    public List<Feedback> getAllFeedbacks() {
        logger.debug("Fetching all feedbacks");
        try {
            List<Feedback> feedbacks = feedbackRepository.findAll();
            logger.info("Found {} feedbacks", feedbacks.size());
            return feedbacks;
        } catch (Exception e) {
            logger.error("Error fetching all feedbacks: {}", e.getMessage());
            throw new RuntimeException("Không thể lấy danh sách tất cả phản hồi: " + e.getMessage());
        }
    }

    @Override
    public List<Feedback> getFeedbacksByDate(LocalDate date) {
        logger.debug("Fetching feedbacks by date: {}", date);
        try {
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(23, 59, 59);
            List<Feedback> feedbacks = feedbackRepository.findAll().stream()
                    .filter(f -> f.getSubmittedAt() != null &&
                            !f.getSubmittedAt().isBefore(startOfDay) &&
                            !f.getSubmittedAt().isAfter(endOfDay))
                    .toList();
            logger.info("Found {} feedbacks for date {}", feedbacks.size(), date);
            return feedbacks;
        } catch (Exception e) {
            logger.error("Error fetching feedbacks by date: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public void respondToFeedback(Long feedbackId, String response) {
        logger.debug("Responding to feedback with ID: {}", feedbackId);
        try {
            feedbackRepository.updateFeedbackResponse(feedbackId, response, LocalDateTime.now(), "Completed");
            logger.info("Successfully responded to feedback ID: {}", feedbackId);
        } catch (Exception e) {
            logger.error("Error responding to feedback ID: {} - {}", feedbackId, e.getMessage());
            throw new RuntimeException("Không thể phản hồi feedback: " + e.getMessage());
        }
    }

    @Override
    public Feedback getFeedbackById(Long feedbackId) {
        logger.debug("Fetching feedback by ID: {}", feedbackId);
        try {
            Feedback feedback = feedbackRepository.findByFeedbackId(feedbackId);
            if (feedback == null) {
                logger.warn("Feedback not found for ID: {}", feedbackId);
                throw new RuntimeException("Không tìm thấy phản hồi với ID: " + feedbackId);
            }
            return feedback;
        } catch (Exception e) {
            logger.error("Error fetching feedback by ID: {} - {}", feedbackId, e.getMessage());
            throw new RuntimeException("Không thể lấy phản hồi: " + e.getMessage());
        }
    }
}