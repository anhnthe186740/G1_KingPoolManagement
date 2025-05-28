package com.example.kingpool.repository;

import com.example.kingpool.entity.PackageUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PackageUsageRepository extends JpaRepository<PackageUsage, Integer> {
    List<PackageUsage> findBySubscriptionId(Integer subscriptionId);
}
// This interface extends JpaRepository to provide CRUD operations for PackageUsage entities.