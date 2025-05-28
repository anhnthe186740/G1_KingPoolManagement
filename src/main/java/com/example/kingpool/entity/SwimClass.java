package com.example.kingpool.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "swim_classes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SwimClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "class_id")
    private Integer classId;

    @ManyToOne
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @ManyToOne
    @JoinColumn(name = "coach_id", nullable = false)
    private User coach;

    @NotNull
    @Positive
    @Column(name = "max_students", nullable = false)
    private Integer maxStudents;

    @Column(name = "description")
    private String description;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}