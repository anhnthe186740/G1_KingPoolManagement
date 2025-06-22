package com.example.kingpool.repository;

import com.example.kingpool.entity.ClassSchedule;
import com.example.kingpool.entity.SwimClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public interface ClassScheduleRepository extends JpaRepository<ClassSchedule, Integer> {

    // Tìm lịch theo ngày trong tuần
    List<ClassSchedule> findByDayOfWeek(DayOfWeek dayOfWeek);

    // Tìm lịch theo lớp học cụ thể
    List<ClassSchedule> findBySwimClass(SwimClass swimClass);

    // Tìm tất cả lịch học của một lớp cụ thể theo ID lớp
    @Query("SELECT cs FROM ClassSchedule cs WHERE cs.swimClass.classId = :classId")
    List<ClassSchedule> findByClassId(@Param("classId") Integer classId);

    // Kiểm tra xem một lớp có đang sử dụng một khung giờ cụ thể không (ví dụ tránh trùng giờ)
    @Query("SELECT COUNT(cs) > 0 FROM ClassSchedule cs WHERE cs.dayOfWeek = :day AND cs.startTime = :startTime")
    boolean existsByDayAndStartTime(@Param("day") DayOfWeek dayOfWeek,
                                    @Param("startTime") LocalTime startTime);

     List<ClassSchedule> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);

}
