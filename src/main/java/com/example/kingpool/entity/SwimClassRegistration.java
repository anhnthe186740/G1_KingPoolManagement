package com.example.kingpool.entity;

import jakarta.persistence.*;
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

    @ManyToOne
    @JoinColumn(name = "class_id", nullable = false)
    private SwimClass swimClass;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "registration_date")
    private LocalDateTime registrationDate = LocalDateTime.now();

    @Column(name = "status", length = 20)
    private String status;
}