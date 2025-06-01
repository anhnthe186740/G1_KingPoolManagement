package com.example.kingpool.service;

import com.example.kingpool.entity.Role;
import com.example.kingpool.entity.User;
import com.example.kingpool.repository.RoleRepository;
import com.example.kingpool.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    public User register(User request) {
        System.out.println("Starting user registration for: " + request.getUsername());
        if (request.getUsername() == null || request.getUsername().isEmpty()) {
            throw new IllegalArgumentException("Username không được để trống.");
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Mật khẩu không được để trống.");
        }
        if (request.getName() == null || request.getName().isEmpty()) {
            throw new IllegalArgumentException("Tên không được để trống.");
        }
        if (request.getEmail() == null || request.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Email không được để trống.");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            System.out.println("Username already taken: " + request.getUsername());
            throw new RuntimeException("Username đã tồn tại.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            System.out.println("Email already in use: " + request.getEmail());
            throw new RuntimeException("Email đã tồn tại.");
        }

        User user = new User();
        user.setName(request.getName());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setGender(request.getGender());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        System.out.println("Looking for role 'Customer'");
        Role role = roleRepository.findByRoleName("Customer")
                .orElseGet(() -> {
                    System.out.println("Role 'Customer' not found, creating new role...");
                    Role newRole = new Role();
                    newRole.setRoleName("Customer");
                    return roleRepository.save(newRole);
                });

        user.setRole(role);
        user.setCreatedAt(LocalDateTime.now());
        user.setStatus(User.Status.Active);

        System.out.println("Saving user: " + user.getUsername());
        userRepository.save(user);
        System.out.println("User registered successfully: " + user.getUsername());
        return user;
    }

    public User login(User request) {
        System.out.println("Attempting to login user: " + request.getUsername());
        if (request.getUsername() == null || request.getPassword() == null) {
            throw new IllegalArgumentException("Username và mật khẩu không được để trống.");
        }
        try {
            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> {
                        System.out.println("User not found during login: " + request.getUsername());
                        return new RuntimeException("Tài khoản không tồn tại.");
                    });
            System.out.println("Found user: " + user.getUsername() + ", Password in DB: " + user.getPassword() + ", Status: " + user.getStatus());
            
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
            if (!authentication.isAuthenticated()) {
                System.out.println("Authentication failed for user: " + request.getUsername());
                throw new RuntimeException("Mật khẩu không đúng.");
            }
            
            if (user.getStatus() != User.Status.Active) {
                System.out.println("User account is not active: " + request.getUsername());
                throw new RuntimeException("Tài khoản không hoạt động.");
            }
            System.out.println("User logged in successfully: " + user.getUsername());
            return user;
        } catch (Exception e) {
            System.out.println("Login failed for user: " + request.getUsername() + ", Error: " + e.getMessage());
            throw new RuntimeException("Đăng nhập thất bại: " + e.getMessage());
        }
    }

    public User findByUsername(String username) {
        System.out.println("Finding user by username: " + username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    System.out.println("User not found: " + username);
                    return new RuntimeException("User not found");
                });
        System.out.println("Found user: " + username);
        return user;
    }

    public User findByEmail(String email) {
        System.out.println("Finding user by email: " + email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    System.out.println("User not found by email: " + email);
                    return new RuntimeException("User not found");
                });
        System.out.println("Found user by email: " + email);
        return user;
    }
}