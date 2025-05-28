package com.example.kingpool.repository;

import com.example.kingpool.entity.PackageSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PackageSubscriptionRepository extends JpaRepository<PackageSubscription, Integer> {
    List<PackageSubscription> findByUserId(Integer userId);
    List<PackageSubscription> findBySwimPackageId(Integer packageId);
}
// This interface extends JpaRepository to provide CRUD operations for PackageSubscription entities.