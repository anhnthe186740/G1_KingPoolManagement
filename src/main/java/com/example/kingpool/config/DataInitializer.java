package com.example.kingpool.config;

import com.example.kingpool.entity.Role;
import com.example.kingpool.entity.User;
import com.example.kingpool.repository.RoleRepository;
import com.example.kingpool.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void initData() {
        // Tạo role ADMIN nếu chưa có
        Role adminRole = roleRepository.findByRoleName("ADMIN")
                .orElseGet(() -> roleRepository.save(new Role(null, "ADMIN")));

        // Tạo tài khoản admin mặc định nếu chưa tồn tại
        if (!userRepository.existsByUsername("admin1")) {
            User admin = new User();
            admin.setUsername("admin1"); 
            admin.setPassword(passwordEncoder.encode("1")); 
            admin.setName("Admin1");
            admin.setEmail("admin@gmail.com");
            admin.setPhoneNumber("0123456789");
            admin.setGender("MALE");
            admin.setDateOfBirth(LocalDate.of(2000, 1, 1));
            admin.setHireDate(LocalDate.now());
            admin.setCreatedAt(LocalDateTime.now());
            admin.setStatus("ACTIVE");
            admin.setRole(adminRole);
            userRepository.save(admin);
        }
    }
}
