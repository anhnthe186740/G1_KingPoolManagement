package com.example.kingpool.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Booking")
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

    @Column(name = "schedule_id", nullable = false)
    private Integer scheduleId;

    @Column(name = "booking_type", nullable = false)
    private String bookingType;

    @Column(name = "quantity_adult")
    private Integer quantityAdult;

    @Column(name = "quantity_child")
    private Integer quantityChild;

    @Column(name = "total_price", nullable = false)
    private Double totalPrice;

    @Column(name = "booking_date")
    private LocalDateTime bookingDate = LocalDateTime.now();

    @Column(name = "status")
    private String status;
}