package com.example.kingpool.service;

import com.example.kingpool.entity.Role;
import com.example.kingpool.entity.User;
import com.example.kingpool.repository.RoleRepository;
import com.example.kingpool.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest)
            throws OAuth2AuthenticationException {

        String registrationId = userRequest.getClientRegistration().getRegistrationId(); // "google", "github"
        OAuth2User oauthUser = super.loadUser(userRequest);
        Map<String, Object> attributes = oauthUser.getAttributes();

        System.out.println("OAuth2 attributes: " + attributes); // Debug

        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String username = email;

        if ("github".equals(registrationId)) {
            username = (String) attributes.get("login");
            if (name == null)
                name = username;

            // GitHub thường không trả email nếu user không public email
            if (email == null || email.isEmpty()) {
                email = username + "@github.com"; // Gán email tạm để xử lý
            }
        }

        if (email == null || email.isEmpty()) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider. Please ensure your Google/GitHub account has email access granted.");
        }

        // Lấy hoặc tạo role USER
        Role role = roleRepository.findByRoleName("USER")
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setRoleName("USER");
                    return roleRepository.save(newRole);
                });

        // Tạo hoặc cập nhật user
        User user;
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isEmpty()) {
            user = new User();
            user.setEmail(email);
            user.setUsername(username);
            user.setName(name != null ? name : "No Name");
            user.setPassword("pass_oauth"); // OAuth2 không có password
            user.setPhoneNumber(null);
            user.setGender(null);
            user.setDateOfBirth(null);
            user.setHireDate(LocalDate.now());
            user.setCreatedAt(LocalDateTime.now());
            user.setStatus("ACTIVE");
            user.setRole(role);

            userRepository.save(user);
        } else {
            user = existingUser.get();
            user.setName(name);
            user.setRole(role); // đảm bảo user có role
            userRepository.save(user);
        }

        return new DefaultOAuth2User(
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.getRoleName())),
                oauthUser.getAttributes(),
                registrationId.equals("github") ? "login" : "email"
        );
    }
}
