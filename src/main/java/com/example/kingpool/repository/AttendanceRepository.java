package com.example.kingpool.repository;

import com.example.kingpool.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Integer> {

    @Query("SELECT a FROM Attendance a WHERE a.user.userId = :userId AND a.classSchedule.classScheduleId = :scheduleId")
    Attendance findByUserIdAndScheduleId(@Param("userId") Integer userId, @Param("scheduleId") Integer scheduleId);

    @Query("SELECT a FROM Attendance a WHERE a.user.userId = :userId AND a.classSchedule.startTime BETWEEN :start AND :end")
    List<Attendance> findByUserIdAndDateRange(@Param("userId") Integer userId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT a FROM Attendance a WHERE a.classSchedule.classScheduleId IN :scheduleIds AND a.user.userId = :userId")
    List<Attendance> findByClassScheduleIdsAndUserId(@Param("scheduleIds") List<Integer> scheduleIds, @Param("userId") Integer userId);

    @Query("SELECT a FROM Attendance a WHERE a.classSchedule.classScheduleId = :scheduleId AND a.user.userId = :userId")
    Optional<Attendance> findByClassScheduleIdAndUserId(@Param("scheduleId") Integer scheduleId, @Param("userId") Integer userId);
}