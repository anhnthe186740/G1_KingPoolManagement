package com.example.kingpool.service;

import com.example.kingpool.entity.Feedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface FeedbackService {
    Page<Feedback> findAll(Pageable pageable);
        List<Feedback> getAllFeedbacks(); // Thêm phương thức này
}