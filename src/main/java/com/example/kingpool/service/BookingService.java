package com.example.kingpool.service;

import com.example.kingpool.entity.Booking;
import com.example.kingpool.entity.Schedule;
import com.example.kingpool.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    public List<Booking> findBookingsByUserId(Long userId) {
        System.out.println("Finding bookings for userId: " + userId);
        List<Booking> bookings = bookingRepository.findByUserUserId(userId);
        System.out.println("Found bookings for userId " + userId + ": " + bookings);
        return bookings;
    }

    public void deleteBookingsByUserId(Long userId) {
        System.out.println("Deleting bookings for userId: " + userId);
        bookingRepository.deleteByUserUserId(userId);
        System.out.println("Deleted bookings for userId: " + userId);
    }

    public void saveBooking(Booking booking, Schedule schedule) {
        if (schedule == null) {
            throw new IllegalArgumentException("Schedule không được để trống.");
        }
        double total = booking.getQuantityAdult() * schedule.getAdultPrice() +
                       booking.getQuantityChild() * schedule.getChildPrice();
        booking.setTotalPrice(BigDecimal.valueOf(total));
        bookingRepository.save(booking);
    }
}