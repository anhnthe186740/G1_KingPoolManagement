package com.example.kingpool.service;

import com.example.kingpool.entity.User;
import com.example.kingpool.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public List<User> getUsersByRoleId(long roleId) {
        try {
            List<User> users = userRepository.findByRoleId((int) roleId);
            return users != null ? users : Collections.emptyList();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi lấy người dùng theo roleId: " + e.getMessage(), e);
        }
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}