package com.example.kingpool.config;

import com.example.kingpool.entity.User;
import com.example.kingpool.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        User user;

        if (authentication.getPrincipal() instanceof org.springframework.security.oauth2.core.user.OAuth2User oauth2User) {
            String emailAttr = oauth2User.getAttribute("email");
            String fallbackAttr = oauth2User.getAttribute("login");
            String email = emailAttr != null ? emailAttr : fallbackAttr;

            user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("OAuth2 user not found: " + email));
        } else {
            String username = authentication.getName();
            user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));
        }

        HttpSession session = request.getSession();
        session.setAttribute("username", user.getUsername());
        session.setAttribute("name", user.getName());
        session.setAttribute("role", user.getRole().getRoleName());
        session.setAttribute("image", user.getImage() != null ? user.getImage() : "default-profile.jpg");

        String redirectUrl = "Admin".toLowerCase().equals(user.getRole().getRoleName().toLowerCase()) ? "/dashboard" : "/homepage";
        response.sendRedirect(redirectUrl);
    }
}