package com.example.kingpool.service;

import com.example.kingpool.entity.User;
import com.example.kingpool.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("Loading user details for username: " + username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    System.out.println("User not found in database with username: " + username);
                    return new UsernameNotFoundException("User not found with username: " + username);
                });
        if (user.getRole() == null || user.getRole().getRoleName() == null) {
            System.out.println("User role is null for username: " + username);
            throw new UsernameNotFoundException("User role not found for username: " + username);
        }
        String roleName = user.getRole().getRoleName();
        System.out.println("User found: " + username + ", Password: " + user.getPassword() + ", Role: " + roleName);
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + roleName)));
    }
}