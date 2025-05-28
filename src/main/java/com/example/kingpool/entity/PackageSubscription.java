package com.example.kingpool.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "package_subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PackageSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscription_id")
    private Integer subscriptionId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "package_id", nullable = false)
    private SwimPackage swimPackage;

    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @NotNull
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @PositiveOrZero
    @Column(name = "sessions_used")
    private Integer sessionsUsed;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}