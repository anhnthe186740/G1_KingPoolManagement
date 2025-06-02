package com.example.kingpool.config;

import com.example.kingpool.service.CustomOAuth2UserService;
import com.example.kingpool.service.CustomUserDetailsService;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Autowired
        private CustomUserDetailsService customUserDetailsService;

        @Autowired
        private CustomOAuth2UserService customOAuth2UserService;

        @Autowired
        private CustomSuccessHandler customSuccessHandler;

        @Autowired
private DataSource dataSource;

@Bean
public PersistentTokenRepository persistentTokenRepository() {
    JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
    tokenRepository.setDataSource(dataSource);
//     tokenRepository.setCreateTableOnStartup(true); 
    return tokenRepository;
}


        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/", "/homepage", "/login", "/register").permitAll()
                                                .requestMatchers("/forgot", "/forgot/**").permitAll()
                                                .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
                                                .requestMatchers(HttpMethod.POST, "/forgot/resend").permitAll()
                                                .requestMatchers("/api/auth/profile").authenticated()
                                                .anyRequest().authenticated())

                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .successHandler(customSuccessHandler)
                                                .permitAll())
                                .oauth2Login(oauth2 -> oauth2
                                                .loginPage("/login")
                                                .userInfoEndpoint(userInfo -> userInfo
                                                                .userService(customOAuth2UserService))
                                                .successHandler(customSuccessHandler))
                                .rememberMe(remember -> remember
                                                .tokenRepository(persistentTokenRepository())
                                                .tokenValiditySeconds(7 * 24 * 60 * 60)
                                                .userDetailsService(customUserDetailsService)
                                                .key("your-secure-key"))
                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessUrl("/login?logout")
                                                .permitAll());

                return http.build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
                        throws Exception {
                DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
                authProvider.setUserDetailsService(customUserDetailsService);
                authProvider.setPasswordEncoder(passwordEncoder());
                return new ProviderManager(authProvider);
        }
}
