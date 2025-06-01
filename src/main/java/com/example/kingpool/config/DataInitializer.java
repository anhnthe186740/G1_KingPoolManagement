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
        System.out.println("Initializing data...");

        // Tạo role Admin nếu chưa có
        Role adminRole = roleRepository.findByRoleName("Admin")
                .orElseGet(() -> {
                    System.out.println("Creating role 'Admin'");
                    Role newRole = new Role();
                    newRole.setRoleName("Admin");
                    return roleRepository.save(newRole);
                });

        // Tạo role Customer nếu chưa có
        if (!roleRepository.existsByRoleName("Customer")) {
            System.out.println("Creating role 'Customer'");
            Role customerRole = new Role();
            customerRole.setRoleName("Customer");
            roleRepository.save(customerRole);
            System.out.println("Role 'Customer' created");
        }

        // Tạo tài khoản admin mặc định nếu chưa tồn tại
        if (!userRepository.existsByUsername("admin1")) {
            // Kiểm tra email admin@gmail.com
            if (userRepository.existsByEmail("admin@gmail.com")) {
                System.out.println("Email 'admin@gmail.com' already exists, updating admin user...");
                User existingUser = userRepository.findByEmail("admin@gmail.com").get();
                existingUser.setUsername("admin1");
                existingUser.setPassword(passwordEncoder.encode("1"));
                existingUser.setName("Admin1");
                existingUser.setRole(adminRole);
                existingUser.setStatus(User.Status.Active);
                userRepository.save(existingUser);
                System.out.println("Updated admin user with username: admin1");
            } else {
                System.out.println("Creating default admin user: admin1 with password: 1");
                User admin = new User();
                admin.setUsername("admin1");
                admin.setPassword(passwordEncoder.encode("1"));
                admin.setName("Admin1");
                admin.setEmail("admin@gmail.com");
                admin.setPhoneNumber("0123456789");
                admin.setGender(User.Gender.M);
                admin.setDateOfBirth(LocalDate.of(2000, 1, 1));
                admin.setHireDate(LocalDate.now());
                admin.setCreatedAt(LocalDateTime.now());
                admin.setStatus(User.Status.Active);
                admin.setRole(adminRole);
                userRepository.save(admin);
                System.out.println("Default admin user created: admin1");
                System.out.println("Admin password (encoded): " + admin.getPassword());
            }
        } else {
            System.out.println("Admin user 'admin1' already exists");
            userRepository.findByUsername("admin1").ifPresent(user -> 
                System.out.println("Admin user details - Username: " + user.getUsername() + 
                                   ", Password: " + user.getPassword() + 
                                   ", Role: " + (user.getRole() != null ? user.getRole().getRoleName() : "null"))
            );
        }
    }
}