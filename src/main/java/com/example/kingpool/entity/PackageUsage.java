package com.example.kingpool.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "package_usages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PackageUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usage_id")
    private Integer usageId;

    @ManyToOne
    @JoinColumn(name = "subscription_id", nullable = false)
    private PackageSubscription subscription;

    @ManyToOne
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @Column(name = "usage_date")
    private LocalDateTime usageDate = LocalDateTime.now();

    @Column(name = "note")
    private String note;
}