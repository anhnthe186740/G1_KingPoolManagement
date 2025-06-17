package com.example.kingpool.service;

import com.example.kingpool.entity.Role;
import com.example.kingpool.entity.User;
import com.example.kingpool.repository.RoleRepository;
import com.example.kingpool.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public List<User> getAllUsers() {
        try {
            return userRepository.findAll();
        } catch (Exception e) {
            logger.error("Error fetching all users: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<User> getAllCoaches() {
        try {
            return userRepository.findAll().stream()
                    .filter(user -> "Coach".equals(user.getRole().getRoleName()))
                    .toList();
        } catch (Exception e) {
            logger.error("Error fetching all coaches: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<User> getUsersByDate(LocalDate date) {
        try {
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(23, 59, 59);
            return userRepository.findAll().stream()
                    .filter(user -> user.getCreatedAt() != null &&
                            !user.getCreatedAt().isBefore(startOfDay) &&
                            !user.getCreatedAt().isAfter(endOfDay))
                    .toList();
        } catch (Exception e) {
            logger.error("Error fetching users by date {}: {}", date, e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<User> getCoachesByDate(LocalDate date) {
        try {
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(23, 59, 59);
            return userRepository.findAll().stream()
                    .filter(user -> "Coach".equals(user.getRole().getRoleName()) &&
                            user.getCreatedAt() != null &&
                            !user.getCreatedAt().isBefore(startOfDay) &&
                            !user.getCreatedAt().isAfter(endOfDay))
                    .toList();
        } catch (Exception e) {
            logger.error("Error fetching coaches by date {}: {}", date, e.getMessage());
            return Collections.emptyList();
        }
    }

    public User addUser(User user, String roleName) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(role);
        user.setCreatedAt(LocalDateTime.now());
        user.setStatus("ACTIVE");
        return userRepository.save(user);
    }

    public User updateUser(Integer userId, User updatedUser, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!user.getUsername().equals(updatedUser.getUsername()) && 
            userRepository.existsByUsername(updatedUser.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (!user.getEmail().equals(updatedUser.getEmail()) && 
            userRepository.existsByEmail(updatedUser.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        user.setName(updatedUser.getName());
        user.setUsername(updatedUser.getUsername());
        user.setEmail(updatedUser.getEmail());
        user.setPhoneNumber(updatedUser.getPhoneNumber());
        user.setGender(updatedUser.getGender());
        user.setDateOfBirth(updatedUser.getDateOfBirth());
        user.setAddress(updatedUser.getAddress());
        user.setRole(role);
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }
        return userRepository.save(user);
    }

    public void deleteUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.delete(user);
    }

    public User changeUserRole(Integer userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        user.setRole(role);
        return userRepository.save(user);
    }
}
