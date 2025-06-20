package com.example.kingpool.service;

import com.example.kingpool.entity.Feedback;
import com.example.kingpool.repository.FeedbackRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class FeedbackServiceImpl implements FeedbackService {

    private static final Logger logger = LoggerFactory.getLogger(FeedbackServiceImpl.class);

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public Page<Feedback> findAll(Pageable pageable) {
        String sql = "SELECT f.feedback_id, f.user_id, f.content, f.rating, f.submitted_at, f.response, f.responded_at, f.status, f.is_visible, " +
                     "u.name, u.email, u.phone_number, u.address " +
                     "FROM Feedback f " +
                     "JOIN Users u ON f.user_id = u.user_id " +
                     "ORDER BY f.submitted_at DESC LIMIT ? OFFSET ?";

        int pageSize = pageable.getPageSize();
        int offset = (int) pageable.getOffset();

        List<Map<String, Object>> feedbackData = jdbcTemplate.queryForList(sql, pageSize, offset);

        List<Feedback> feedbacks = feedbackData.stream().map(data -> {
            Feedback feedback = new Feedback();
            feedback.setFeedbackId(((Number) data.get("feedback_id")).longValue());
            feedback.setUserId(((Number) data.get("user_id")).intValue());
            feedback.setContent(data.get("content") != null ? String.valueOf(data.get("content")) : null);
            feedback.setRating(((Number) data.get("rating")).intValue());

            Object submittedAtObj = data.get("submitted_at");
            if (submittedAtObj instanceof Timestamp) {
                feedback.setSubmittedAt(((Timestamp) submittedAtObj).toLocalDateTime());
            } else if (submittedAtObj != null) {
                logger.warn("Unexpected type for submitted_at: {}", submittedAtObj.getClass().getName());
                feedback.setSubmittedAt(null);
            }

            Object responseObj = data.get("response");
            if (responseObj != null) {
                feedback.setResponse(String.valueOf(responseObj));
            } else {
                feedback.setResponse(null);
                logger.debug("response is null for feedback_id: {}", data.get("feedback_id"));
            }

            Object respondedAtObj = data.get("responded_at");
            if (respondedAtObj instanceof Timestamp) {
                feedback.setRespondedAt(((Timestamp) respondedAtObj).toLocalDateTime());
            } else if (respondedAtObj != null) {
                logger.warn("Unexpected type for responded_at: {}", respondedAtObj.getClass().getName());
                feedback.setRespondedAt(null);
            } else {
                feedback.setRespondedAt(null);
            }

            Object statusObj = data.get("status");
            if (statusObj != null) {
                feedback.setStatus(String.valueOf(statusObj));
            } else {
                feedback.setStatus(null);
                logger.debug("status is null for feedback_id: {}", data.get("feedback_id"));
            }

            feedback.setIsVisible(data.get("is_visible") != null ? (Boolean) data.get("is_visible") : false);
            feedback.setName(data.get("name") != null ? String.valueOf(data.get("name")) : null);
            feedback.setEmail(data.get("email") != null ? String.valueOf(data.get("email")) : null);
            feedback.setPhoneNumber(data.get("phone_number") != null ? String.valueOf(data.get("phone_number")) : null);
            feedback.setAddress(data.get("address") != null ? String.valueOf(data.get("address")) : null);

            return feedback;
        }).toList();

        String countSql = "SELECT COUNT(*) FROM Feedback";
        long total = jdbcTemplate.queryForObject(countSql, Long.class);

        return new PageImpl<>(feedbacks, pageable, total);
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
        String sql = "SELECT * FROM Feedback WHERE DATE(submitted_at) = ?";
        try {
            List<Feedback> feedbacks = jdbcTemplate.query(sql, new Object[]{date}, (rs, rowNum) -> {
                Feedback feedback = new Feedback();
                feedback.setFeedbackId(rs.getLong("feedback_id"));
                feedback.setUserId(rs.getInt("user_id"));
                feedback.setContent(rs.getString("content"));
                feedback.setRating(rs.getInt("rating"));
                feedback.setSubmittedAt(rs.getTimestamp("submitted_at") != null ? 
                    rs.getTimestamp("submitted_at").toLocalDateTime() : null);
                feedback.setResponse(rs.getString("response"));
                feedback.setRespondedAt(rs.getTimestamp("responded_at") != null ? 
                    rs.getTimestamp("responded_at").toLocalDateTime() : null);
                feedback.setStatus(rs.getString("status"));
                feedback.setIsVisible(rs.getBoolean("is_visible"));
                return feedback;
            });
            return feedbacks;
        } catch (Exception e) {
            logger.error("Error fetching feedbacks by date: {}", e.getMessage());
            return List.of();
        }
    }
}