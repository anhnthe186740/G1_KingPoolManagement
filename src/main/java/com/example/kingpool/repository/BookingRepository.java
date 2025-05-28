package com.example.kingpool.repository;

import com.example.kingpool.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
    List<Booking> findByUserId(Integer userId);
    Optional<Booking> findByScheduleId(Integer scheduleId);
}
// This interface extends JpaRepository to provide CRUD operations for Booking entities.