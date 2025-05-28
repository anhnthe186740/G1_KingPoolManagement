package com.example.kingpool.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "swim_packages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SwimPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "package_id")
    private Integer packageId;

    @NotNull
    @Size(max = 100)
    @Column(name = "package_name", nullable = false, length = 100)
    private String packageName;

    @Column(name = "description")
    private String description;

    @Positive
    @Column(name = "total_sessions")
    private Integer totalSessions;

    @Positive
    @Column(name = "duration_days")
    private Integer durationDays;

    @NotNull
    @Positive
    @Column(name = "price", nullable = false)
    private Double price;

    @ManyToOne
    @JoinColumn(name = "coach_id")
    private User coach;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}