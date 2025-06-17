package com.example.kingpool.service;

import com.example.kingpool.entity.Feedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.List;

public interface FeedbackService {
    Page<Feedback> findAll(Pageable pageable);
    List<Feedback> getAllFeedbacks();
    List<Feedback> getFeedbacksByDate(LocalDate date);
}