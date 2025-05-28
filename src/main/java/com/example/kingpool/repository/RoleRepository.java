package com.example.kingpool.repository;

import com.example.kingpool.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByRoleName(String roleName);
}
// This interface extends JpaRepository to provide CRUD operations for Role entities.