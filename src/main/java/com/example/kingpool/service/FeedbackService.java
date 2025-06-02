package com.example.kingpool.service;

import com.example.kingpool.entity.Feedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FeedbackService {
    Page<Feedback> findAll(Pageable pageable);
}