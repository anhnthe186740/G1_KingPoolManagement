package com.example.kingpool.service;

import com.example.kingpool.entity.Role;
import com.example.kingpool.entity.User;
import com.example.kingpool.repository.RoleRepository;
import com.example.kingpool.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    // Inject upload directory from application.properties
    @Value("${file.upload-dir}")
    private String UPLOAD_DIR;

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

        userRepository.save(user);

        return user;
    }

    public User findByUsername(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            return user.get();
        } else {
            throw new RuntimeException("User not found");
        }
    }

    public User login(User request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        Optional<User> user = userRepository.findByUsername(request.getUsername());
        if (user.isEmpty()) {
            throw new RuntimeException("User not found");
        } else {
            request.setEmail(user.get().getEmail());
        }

        String username = authentication.getName();
        String role = authentication.getAuthorities().iterator().next().getAuthority().substring(5); // Bỏ "ROLE_"

        return request;
    }

    public User findByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            return user.get();
        } else {
            throw new RuntimeException("User not found");
        }
    }

    public User getUserFromAuthentication(Authentication authentication) {
        String username = authentication.getName();
        return findByUsername(username);
    }

    public User updateUserProfile(User currentUser, User updatedUser, MultipartFile imageFile) throws IOException {
        logger.info("Updating profile for user: {}", currentUser.getUsername());

        // Xử lý upload ảnh
        if (imageFile != null && !imageFile.isEmpty()) {
            // Tạo thư mục uploads nếu chưa tồn tại
            Path uploadPath = Paths.get(UPLOAD_DIR).toAbsolutePath().normalize();
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Tạo tên file duy nhất bằng UUID
            String originalFilename = imageFile.getOriginalFilename();
            String fileExtension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String newFilename = UUID.randomUUID().toString() + fileExtension;

            // Lưu file vào thư mục uploads
            Path filePath = uploadPath.resolve(newFilename);
            Files.copy(imageFile.getInputStream(), filePath);

            // Xóa ảnh cũ nếu có
            if (currentUser.getImage() != null && !currentUser.getImage().isEmpty()) {
                Path oldFilePath = uploadPath.resolve(currentUser.getImage());
                Files.deleteIfExists(oldFilePath);
            }

            // Cập nhật tên file vào trường image
            currentUser.setImage(newFilename);
            logger.info("Updated image for user: {} to {}", currentUser.getUsername(), newFilename);
        }

        // Cập nhật email nếu có thay đổi
        if (updatedUser.getEmail() != null && !updatedUser.getEmail().equals(currentUser.getEmail())) {
            logger.info("Updating email from {} to {}", currentUser.getEmail(), updatedUser.getEmail());
            if (userRepository.existsByEmail(updatedUser.getEmail())) {
                throw new RuntimeException("Email đã được sử dụng");
            }
            currentUser.setEmail(updatedUser.getEmail());
        }

        // Cập nhật các trường khác, cho phép giá trị rỗng
        if (updatedUser.getName() != null) {
            logger.info("Updating name from {} to {}", currentUser.getName(), updatedUser.getName());
            currentUser.setName(updatedUser.getName());
        }
        if (updatedUser.getPhoneNumber() != null) {
            logger.info("Updating phoneNumber from {} to {}", currentUser.getPhoneNumber(), updatedUser.getPhoneNumber());
            currentUser.setPhoneNumber(updatedUser.getPhoneNumber());
        } else {
            currentUser.setPhoneNumber("");
        }

        // Cập nhật gender
        if (updatedUser.getGender() != null) {
            logger.info("Updating gender from {} to {}", currentUser.getGender(), updatedUser.getGender());
            currentUser.setGender(updatedUser.getGender());
        } else {
            currentUser.setGender(null);
        }

        // Cập nhật dateOfBirth
        if (updatedUser.getDateOfBirth() != null) {
            logger.info("Updating dateOfBirth from {} to {}", currentUser.getDateOfBirth(), updatedUser.getDateOfBirth());
            currentUser.setDateOfBirth(updatedUser.getDateOfBirth());
        } else {
            currentUser.setDateOfBirth(null);
        }

        // Cập nhật address
        if (updatedUser.getAddress() != null) {
            logger.info("Updating address from {} to {}", currentUser.getAddress(), updatedUser.getAddress());
            currentUser.setAddress(updatedUser.getAddress());
        } else {
            currentUser.setAddress("");
        }

        // Lưu vào cơ sở dữ liệu
        User savedUser = userRepository.save(currentUser);
        logger.info("Profile saved successfully for user: {}", savedUser.getUsername());
        return savedUser;
    }
}