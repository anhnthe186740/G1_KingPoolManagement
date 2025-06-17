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

    @Column(name = "user_id")
    private Integer userId; // Thêm userId để ánh xạ với cột user_id trong database

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "address")
    private String address;

    @Column(name = "content")
    private String content;

    @Column(name = "rating")
    private Integer rating;

    @Column(name = "is_visible")
    private Boolean isVisible;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "response") // Thêm thuộc tính response
    private String response;

    @Column(name = "responded_at") // Thêm thuộc tính respondedAt
    private LocalDateTime respondedAt;

    @Column(name = "status") // Thêm thuộc tính status
    private String status;
}