package com.example.kingpool.repository;

import com.example.kingpool.entity.PackageSubscriptions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PackageSubscriptionRepository extends JpaRepository<PackageSubscriptions, Long> {
    List<PackageSubscriptions> findByUserUserId(Long userId);
}