package com.example.kingpool.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.kingpool.entity.User;
import java.util.List;


public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    List<User> findByRoleRoleName(String roleName);

}
// This interface extends JpaRepository to provide CRUD operations for User entities.