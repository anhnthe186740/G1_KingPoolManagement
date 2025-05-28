package com.example.kingpool.repository;

import com.example.kingpool.entity.SwimClassRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SwimClassRegistrationRepository extends JpaRepository<SwimClassRegistration, Integer> {
    List<SwimClassRegistration> findByUserId(Integer userId);
    List<SwimClassRegistration> findBySwimClassId(Integer classId);
}
// This interface extends JpaRepository to provide CRUD operations for SwimClassRegistration entities.