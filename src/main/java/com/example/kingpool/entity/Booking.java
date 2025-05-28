package com.example.kingpool.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "booking")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Integer bookingId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @NotNull
    @Column(name = "booking_type", nullable = false, length = 20)
    private String bookingType;

    @PositiveOrZero
    @Column(name = "quantity_adult")
    private Integer quantityAdult;

    @PositiveOrZero
    @Column(name = "quantity_child")
    private Integer quantityChild;

    @Positive
    @Column(name = "total_price")
    private Double totalPrice;

    @Column(name = "booking_date")
    private LocalDateTime bookingDate = LocalDateTime.now();

    @Column(name = "status", length = 20)
    private String status;
}