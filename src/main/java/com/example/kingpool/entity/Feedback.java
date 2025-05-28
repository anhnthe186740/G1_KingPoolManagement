package com.example.kingpool.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "feedback")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Integer feedbackId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Column(name = "content", nullable = false)
    private String content;

    @Min(1)
    @Max(5)
    @Column(name = "rating")
    private Integer rating;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt = LocalDateTime.now();

    @Column(name = "response")
    private String response;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @Column(name = "status", length = 20)
    private String status;
}