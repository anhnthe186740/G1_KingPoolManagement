package com.example.kingpool.repository;

import com.example.kingpool.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {
    List<Schedule> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);
    Optional<Schedule> findByStartTime(LocalDateTime startTime);
}
// This interface extends JpaRepository to provide CRUD operations for Schedule entities.