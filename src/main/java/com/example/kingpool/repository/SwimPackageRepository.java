package com.example.kingpool.repository;

import com.example.kingpool.entity.SwimPackages;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SwimPackageRepository extends JpaRepository<SwimPackages, Long> {
}