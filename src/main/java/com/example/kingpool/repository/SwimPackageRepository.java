package com.example.kingpool.repository;

import com.example.kingpool.entity.SwimPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SwimPackageRepository extends JpaRepository<SwimPackage, Integer> {
    Optional<SwimPackage> findByPackageName(String packageName);
}
// This interface extends JpaRepository to provide CRUD operations for SwimPackage entities.