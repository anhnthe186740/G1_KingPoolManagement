package com.example.kingpool.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Booking")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Long bookingId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_type", nullable = false)
    private BookingType bookingType;

    @Column(name = "quantity_adult")
    private Integer quantityAdult = 0;

    @Column(name = "quantity_child")
    private Integer quantityChild = 0;

    @Column(name = "total_price")
    private BigDecimal totalPrice;

    @Column(name = "booking_date")
    private LocalDateTime bookingDate = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private BookingStatus status = BookingStatus.Pending;

    public enum BookingType {
        Ticket, SwimClass
    }

    public enum BookingStatus {
        Pending, Confirmed, Cancelled
    }

    @PrePersist
    @PreUpdate
    private void validateQuantities() {
        if (quantityAdult == null || quantityChild == null) {
            throw new IllegalArgumentException("Số lượng vé không được để trống.");
        }
        if (quantityAdult < 0 || quantityChild < 0) {
            throw new IllegalArgumentException("Số lượng vé không được âm.");
        }
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public BookingType getBookingType() {
        return bookingType;
    }

    public void setBookingType(BookingType bookingType) {
        this.bookingType = bookingType;
    }

    public Integer getQuantityAdult() {
        return quantityAdult;
    }

    public void setQuantityAdult(Integer quantityAdult) {
        this.quantityAdult = quantityAdult;
    }

    public Integer getQuantityChild() {
        return quantityChild;
    }

    public void setQuantityChild(Integer quantityChild) {
        this.quantityChild = quantityChild;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public LocalDateTime getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDateTime bookingDate) {
        this.bookingDate = bookingDate;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }
}