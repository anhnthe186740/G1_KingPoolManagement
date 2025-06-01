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

        String username = authentication.getName();
        System.out.println("Authentication success for user: " + username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    System.out.println("User not found: " + username);
                    return new RuntimeException("User not found");
                });

        HttpSession session = request.getSession();
        session.setAttribute("username", user.getUsername());
        session.setAttribute("name", user.getName());
        session.setAttribute("role", user.getRole().getRoleName());

        // Redirect: Admin vào /dashboard, còn lại (Customer, Coach) vào /user-homepage
        String redirectUrl = user.getRole().getRoleName().equals("Admin") ? "/dashboard" : "/user-homepage";
        System.out.println("Redirecting user " + username + " to " + redirectUrl);
        response.sendRedirect(redirectUrl);
    }
}