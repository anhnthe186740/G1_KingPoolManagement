package com.example.kingpool.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_id")
    private Integer ticketId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @NotNull
    @Column(name = "ticket_type", nullable = false, length = 20)
    private String ticketType;

    @NotNull
    @Positive
    @Column(name = "price", nullable = false)
    private Double price;

    @Size(max = 50)
    @Column(name = "ticket_code", unique = true, length = 50)
    private String ticketCode;

    @Column(name = "purchase_date")
    private LocalDateTime purchaseDate = LocalDateTime.now();

    @Column(name = "status", length = 20)
    private String status;
}