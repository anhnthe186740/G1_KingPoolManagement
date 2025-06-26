package com.example.kingpool.repository;

import com.example.kingpool.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    @Modifying
    @Transactional
    @Query("UPDATE Feedback f SET f.response = :response, f.respondedAt = :respondedAt, f.status = :status WHERE f.feedbackId = :feedbackId")
    void updateFeedbackResponse(Long feedbackId, String response, LocalDateTime respondedAt, String status);

    @Query("SELECT f FROM Feedback f JOIN FETCH f.user u WHERE f.feedbackId = :feedbackId")
    Feedback findByFeedbackId(Long feedbackId);
}