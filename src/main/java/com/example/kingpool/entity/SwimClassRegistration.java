package com.example.kingpool.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "swim_class_registrations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SwimClassRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "registration_id")
    private Integer registrationId;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "class_id", nullable = false)
    private SwimClass swimClass;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "registration_date")
    private LocalDateTime registrationDate = LocalDateTime.now();

    @Column(name = "cancel_reason", columnDefinition = "TEXT")
    private String cancelReason;

    @NotNull
    @Size(max = 20)
    @Column(name = "status", nullable = false, length = 20)
    private String status = "Pending";
}