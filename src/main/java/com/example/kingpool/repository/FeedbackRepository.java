package com.example.kingpool.repository;

import com.example.kingpool.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Integer> {
    List<Feedback> findByUserId(Integer userId);
    List<Feedback> findByStatus(String status);
}
// This interface extends JpaRepository to provide CRUD operations for Feedback entities.