package com.example.kingpool.repository;

import com.example.kingpool.entity.SwimClass;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SwimClassRepository extends JpaRepository<SwimClass, Integer> {
    List<SwimClass> findByCoachId(Integer coachId);
    List<SwimClass> findByScheduleId(Integer scheduleId);
}
// This interface extends JpaRepository to provide CRUD operations for SwimClass entities.