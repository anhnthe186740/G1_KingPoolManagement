package com.example.kingpool.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "support_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SupportRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Integer requestId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Column(name = "support_type", nullable = false, length = 20)
    private String supportType;

    @NotNull
    @Column(name = "description", nullable = false)
    private String description;

    @Size(max = 255)
    @Column(name = "image1_path", length = 255)
    private String image1Path;

    @Size(max = 255)
    @Column(name = "image2_path", length = 255)
    private String image2Path;

    @Column(name = "priority", length = 20)
    private String priority;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt = LocalDateTime.now();

    @Column(name = "response")
    private String response;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @Column(name = "status", length = 20)
    private String status;
}