package com.example.E_Wallet.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security Configuration
 * 
 * WHAT IT DOES:
 * This is the central configuration for Spring Security in your application.
 * It tells Spring Security:
 * - Which endpoints are public (no authentication needed)
 * - Which endpoints require authentication
 * - How to handle authentication (using JWT)
 * - How to handle sessions (stateless = no server-side sessions)
 * 
 * WHY IT'S NEEDED:
 * Without this configuration, Spring Security would:
 * 1. Block ALL requests by default
 * 2. Try to use form-based login (not what we want for REST API)
 * 3. Create server-side sessions (not needed for JWT)
 * 
 * This configuration customizes Spring Security to work with JWT tokens.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * SecurityFilterChain Bean
     * 
     * This is the main security configuration method.
     * It configures how Spring Security handles HTTP requests.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        
        http
            // Disable CSRF (Cross-Site Request Forgery) protection
            // WHY: CSRF is mainly for browser-based apps with cookies.
            // For REST APIs using JWT tokens, CSRF is not needed because:
            // 1. Tokens are sent in headers, not cookies
            // 2. REST APIs are typically used by mobile apps, SPAs, or other services
            //    that don't have the same CSRF vulnerabilities
            .csrf(csrf -> csrf.disable())
            
            // Configure authorization rules (which endpoints need authentication)
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - anyone can access these without authentication
                .requestMatchers("/auth/signup", "/auth/login").permitAll()
                
                // All other endpoints require authentication
                // This includes:
                // - /users/** (all user endpoints)
                // - /wallets/** (all wallet endpoints)
                // - Any other endpoints you add later
                .anyRequest().authenticated()
            )
            
            // Configure session management
            // WHY STATELESS: 
            // - JWT tokens are self-contained (they contain all user info)
            // - We don't need server-side sessions
            // - Each request is independent (stateless)
            // - This makes the API scalable (can run on multiple servers)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Add our custom JWT filter
            // WHY: This tells Spring Security to use our JwtAuthenticationFilter
            // to authenticate requests using JWT tokens.
            // 
            // The filter is added BEFORE UsernamePasswordAuthenticationFilter
            // because we want to check JWT tokens before Spring Security tries
            // to do form-based authentication.
            .addFilterBefore(
                jwtAuthenticationFilter, 
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }

    /**
     * Password Encoder Bean
     * 
     * WHY: This is used to hash passwords before storing them in the database.
     * BCrypt is a strong, one-way hashing algorithm that:
     * 1. Makes passwords unreadable even if database is compromised
     * 2. Includes a "salt" (random data) to prevent rainbow table attacks
     * 3. Is computationally expensive (slows down brute force attacks)
     * 
     * This bean is already being used in UserService for password hashing.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Authentication Manager Bean
     * 
     * WHY: This is required by Spring Security for authentication operations.
     * Even though we're using JWT (which doesn't use traditional username/password
     * authentication), Spring Security still needs this bean to be present.
     * 
     * In a traditional setup, this would handle username/password authentication.
     * In our JWT setup, we're bypassing this, but Spring Security still requires it.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}

