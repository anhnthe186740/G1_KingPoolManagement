package com.example.kingpool.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "feedback")
@Getter
@Setter
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Long feedbackId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "content")
    private String content;

    @Column(name = "rating")
    private Integer rating;

    @Column(name = "is_visible")
    private Boolean isVisible;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "response")
    private String response;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @Column(name = "status")
    private String status;

    @Column(name = "is_read")
    private Boolean isRead;
}