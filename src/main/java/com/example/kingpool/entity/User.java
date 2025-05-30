package com.example.kingpool.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @NotBlank(message = "Tên không được để trống")
    @Size(min = 1, max = 100, message = "Tên phải từ 1 đến 100 ký tự")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(max = 50, message = "Tên đăng nhập tối đa 50 ký tự")
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Size(max = 255)
    @Column(name = "password", length = 255)
    private String password;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Size(max = 100, message = "Email tối đa 100 ký tự")
    @Column(name = "email", unique = true, length = 100)
    private String email;

    @Size(max = 15, message = "Số điện thoại tối đa 15 ký tự")
    @Column(name = "phone_number", length = 15)
    private String phoneNumber;

    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Size(max = 200, message = "Địa chỉ tối đa 200 ký tự")
    @Column(name = "address", length = 200)
    private String address;

    @Column(name = "image", length = 255)
    private String image;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "status", length = 20)
    private String status;
}