package com.example.kingpool.repository;

import com.example.kingpool.entity.SwimClass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SwimClassRepository extends JpaRepository<SwimClass, Integer> {

    List<SwimClass> findByCoachUserId(Integer coachId);

    List<SwimClass> findByStatus(String status);

    Page<SwimClass> findByStatus(String status, Pageable pageable);

    @Query("SELECT s FROM SwimClass s LEFT JOIN FETCH s.schedules WHERE s.classId = :id")
    Optional<SwimClass> findByIdWithSchedules(@Param("id") Integer id);
    
}
