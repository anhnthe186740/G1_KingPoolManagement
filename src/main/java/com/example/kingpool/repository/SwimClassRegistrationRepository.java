package com.example.kingpool.repository;

import com.example.kingpool.entity.SwimClassRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SwimClassRegistrationRepository extends JpaRepository<SwimClassRegistration, Integer> {

    List<SwimClassRegistration> findBySwimClassClassId(Integer classId);

    boolean existsBySwimClass_ClassIdAndUser_UserId(Integer classId, Integer userId);

    List<SwimClassRegistration> findByUserUserId(Integer userId);

    List<SwimClassRegistration> findBySwimClass_ClassIdAndStatus(Integer classId, String status);

    void deleteBySwimClassClassId(Integer classId);
}