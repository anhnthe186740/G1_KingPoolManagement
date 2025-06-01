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
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        System.out.println("Loading OAuth2 user from provider: " + userRequest.getClientRegistration().getRegistrationId());
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2User oauthUser = super.loadUser(userRequest);
        Map<String, Object> attributes = oauthUser.getAttributes();

        String email = (String) attributes.get("email");
        if (email == null) {
            System.out.println("Email not provided by OAuth2 provider!");
            throw new OAuth2AuthenticationException("Email không được cung cấp bởi OAuth2 provider!");
        }

        String name = attributes.get("name") != null ? (String) attributes.get("name") : "Unknown";
        String username = email;

        if ("github".equals(registrationId)) {
            username = (String) attributes.get("login");
            if (name.equals("Unknown")) {
                name = username;
            }
        }

        String finalUsername = username.length() > 45 ? username.substring(0, 45) : username;
        int counter = 1;
        while (userRepository.existsByUsername(finalUsername)) {
            String suffix = String.valueOf(counter);
            finalUsername = username.length() > (45 - suffix.length()) 
                    ? username.substring(0, 45 - suffix.length()) + suffix 
                    : username + suffix;
            counter++;
        }

        System.out.println("Looking for role 'Customer'");
        Role role = roleRepository.findByRoleName("Customer")
                .orElseThrow(() -> {
                    System.out.println("Role 'Customer' not found in database!");
                    return new RuntimeException("Role 'Customer' not found in database!");
                });

        User user;
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isEmpty()) {
            user = new User();
            user.setEmail(email);
            user.setUsername(finalUsername);
            user.setName(name);
            user.setPassword(null);
            user.setPhoneNumber(null);
            user.setGender(null);
            user.setDateOfBirth(null);
            user.setHireDate(null);
            user.setCreatedAt(LocalDateTime.now());
            user.setStatus(User.Status.Active);
            user.setRole(role);

            System.out.println("Saving new OAuth2 user: " + email);
            userRepository.save(user);
            System.out.println("OAuth2 user created: " + email);
        } else {
            user = existingUser.get();
            if (user.getStatus() != User.Status.Active) {
                throw new OAuth2AuthenticationException("Tài khoản không hoạt động.");
            }
            user.setName(name);
            user.setRole(role);
            System.out.println("Updating existing OAuth2 user: " + email);
            userRepository.save(user);
            System.out.println("OAuth2 user updated: " + email);
        }

        return new DefaultOAuth2User(
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.getRoleName())),
                oauthUser.getAttributes(),
                registrationId.equals("github") ? "login" : "email"
        );
    }
}