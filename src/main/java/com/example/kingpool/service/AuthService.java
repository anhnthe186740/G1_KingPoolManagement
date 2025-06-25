package com.example.kingpool.service;

import com.example.kingpool.entity.Role;
import com.example.kingpool.entity.User;
import com.example.kingpool.repository.RoleRepository;
import com.example.kingpool.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    public User register(User request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already in use");
        }

        User user = new User();
        user.setName(request.getName());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setGender(request.getGender());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setHireDate(LocalDate.now());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        Role role = roleRepository.findByRoleName("USER")
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setRoleName("USER");
                    return roleRepository.save(newRole);
                });

        user.setRole(role);
        user.setCreatedAt(LocalDateTime.now());
        user.setStatus("ACTIVE");

        return userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User login(User request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        Optional<User> user = userRepository.findByUsername(request.getUsername());
        if (user.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        request.setEmail(user.get().getEmail());
        return request;
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User getUserFromAuthentication(Authentication authentication) {
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public void updateUserProfile(User currentUser, User updatedUser, MultipartFile imageFile) throws IOException {
        logger.info("Updating profile for user: {}", currentUser.getUsername());
        logger.info("Image file received: name={}, size={}", 
                imageFile.getOriginalFilename(), imageFile.getSize());

        currentUser.setName(updatedUser.getName());
        currentUser.setEmail(updatedUser.getEmail());
        currentUser.setPhoneNumber(updatedUser.getPhoneNumber());
        currentUser.setGender(updatedUser.getGender());
        currentUser.setDateOfBirth(updatedUser.getDateOfBirth());
        currentUser.setAddress(updatedUser.getAddress());

        if (!imageFile.isEmpty()) {
            String uploadDir = System.getProperty("user.dir") + "/uploads/";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.write(filePath, imageFile.getBytes());
            currentUser.setImage(fileName);
            logger.info("Image saved: {}", fileName);
        } else {
            logger.info("No image file provided, keeping existing: {}", currentUser.getImage());
        }

        userRepository.save(currentUser);
        logger.info("User profile updated successfully for user: {}", currentUser.getUsername());
    }

    public void changePassword(User currentUser, String currentPassword, String newPassword, String confirmNewPassword) {
        User dbUser = userRepository.findById(currentUser.getUserId())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        String storedPassword = dbUser.getPassword();
        logger.info("⛳ Nhập: {}", currentPassword);
        logger.info("🔐 DB: {}", storedPassword);

        if (storedPassword == null || !storedPassword.startsWith("$2a$")) {
            logger.error("Stored password is not encoded for user: {}", currentUser.getUsername());
            throw new RuntimeException("Mật khẩu của tài khoản chưa được mã hóa.");
        }

        boolean matches = passwordEncoder.matches(currentPassword, storedPassword);
        logger.info("🎯 matches: {}", matches);

        if (!matches) {
            logger.warn("Current password does not match for user: {}", currentUser.getUsername());
            throw new RuntimeException("Mật khẩu hiện tại không đúng");
        }

        if (!newPassword.equals(confirmNewPassword)) {
            logger.warn("New password and confirmation do not match for user: {}", currentUser.getUsername());
            throw new RuntimeException("Mật khẩu mới và xác nhận không khớp");
        }

        if (newPassword.length() < 6) {
            logger.warn("New password is too short for user: {}", currentUser.getUsername());
            throw new RuntimeException("Mật khẩu mới phải có ít nhất 6 ký tự");
        }

        dbUser.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(dbUser);
        logger.info("Password changed successfully for user: {}", currentUser.getUsername());
    }
}