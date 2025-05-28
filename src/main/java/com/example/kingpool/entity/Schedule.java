package com.example.kingpool.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "schedules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Integer scheduleId;

    @NotNull
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @NotNull
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @NotNull
    @Positive
    @Column(name = "max_tickets", nullable = false)
    private Integer maxTickets;

    @NotNull
    @Positive
    @Column(name = "adult_price", nullable = false)
    private Double adultPrice;

    @NotNull
    @Positive
    @Column(name = "child_price", nullable = false)
    private Double childPrice;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}