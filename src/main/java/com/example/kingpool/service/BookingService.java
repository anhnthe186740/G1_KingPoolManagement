package com.example.kingpool.service;

import com.example.kingpool.entity.Booking;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface BookingService {
    List<Map<String, Object>> getAvailableSchedules();
    Booking bookTicket(Authentication authentication, Integer scheduleId, Integer quantityAdult, Integer quantityChild);
    List<Map<String, Object>> getBookingHistory(Authentication authentication);
    void cancelBooking(Authentication authentication, Integer bookingId);
    List<Booking> getAllBookings();
    List<Booking> getBookingsByDate(LocalDate date);
    double getRevenueByDate(LocalDate date);
    double getRevenueByMonth(int month, int year);
    double getRevenueByQuarter(int quarter, int year);
    double getRevenueByYear(int year);
}