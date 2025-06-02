package com.example.kingpool.repository;

import com.example.kingpool.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {
    @Query(value = "SELECT SUM(quantity_adult + quantity_child) FROM Booking WHERE schedule_id = :scheduleId AND status = 'Confirmed'", nativeQuery = true)
    Integer countBookedTickets(Integer scheduleId);
}