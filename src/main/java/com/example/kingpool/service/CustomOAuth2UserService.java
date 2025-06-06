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
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        String registrationId = userRequest.getClientRegistration().getRegistrationId(); // "google", "github"
        OAuth2User oauthUser = super.loadUser(userRequest);
        Map<String, Object> attributes = oauthUser.getAttributes();

        System.out.println("OAuth2 attributes: " + attributes);

        String email = (String) attributes.get("email");
        String login = (String) attributes.get("login"); // fallback for GitHub
        String name = (String) attributes.get("name");

        String username = email != null ? email : login;
        if (username == null) {
            throw new OAuth2AuthenticationException("Không lấy được email hoặc login từ OAuth2 provider.");
        }

        Role role = roleRepository.findByRoleName("USER")
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setRoleName("USER");
                    return roleRepository.save(newRole);
                });

        User user;
        Optional<User> existingUser = userRepository.findByEmail(username);
        if (existingUser.isEmpty()) {
            user = new User();
            user.setEmail(username);
            user.setUsername(username);
            user.setName(name != null ? name : "No Name");
            user.setPassword(null);
            user.setPhoneNumber(null);
            user.setGender(null);
            user.setDateOfBirth(null);
            user.setHireDate(LocalDate.now());
            user.setCreatedAt(LocalDateTime.now());
            user.setStatus("ACTIVE");
            user.setRole(role);

            userRepository.save(user);
            System.out.println("User created: " + username);
        } else {
            user = existingUser.get();
            user.setName(name);
            user.setRole(role);
            userRepository.save(user);
            System.out.println("User updated: " + username);
        }

        return new DefaultOAuth2User(
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.getRoleName())),
                attributes,
                registrationId.equals("github") ? "login" : "email"
        );
    }
}
