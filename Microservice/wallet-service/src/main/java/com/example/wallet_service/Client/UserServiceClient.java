package com.example.wallet_service.Client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import java.util.UUID;

/**
 * HTTP client for communicating with User Service.
 * Uses Spring WebClient for reactive, non-blocking HTTP requests.
 * This is used to validate users and fetch user information.
 */
@Component
public class UserServiceClient {

    private final WebClient webClient;

    public UserServiceClient(@Value("${user.service.url}") String userServiceUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(userServiceUrl)
                .build();
    }

    /**
     * Validates if a user exists in User Service via HTTP GET request
     * 
     * @param userId The user ID to validate
     * @return true if user exists, false otherwise
     */
    public boolean validateUser(UUID userId) {
        try {
            UserDTO user = webClient.get()
                    .uri("/users/{id}", userId)
                    .retrieve()
                    .bodyToMono(UserDTO.class)
                    .block();
            return user != null && user.getId() != null;
        } catch (WebClientResponseException.NotFound e) {
            // User not found - this is expected for validation
            return false;
        } catch (WebClientResponseException e) {
            // Other HTTP errors (5xx, etc.)
            System.err.println("HTTP Error validating user: " + e.getStatusCode() + " - " + e.getMessage());
            return false;
        } catch (Exception e) {
            // Network errors, timeouts, etc.
            System.err.println("Error validating user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Gets user email by user ID via HTTP GET request
     * 
     * @param userId The user ID
     * @return User email or null if not found or error occurs
     */
    public String getUserEmail(UUID userId) {
        try {
            UserDTO user = webClient.get()
                    .uri("/users/{id}", userId)
                    .retrieve()
                    .bodyToMono(UserDTO.class)
                    .block();
            return user != null ? user.getEmail() : null;
        } catch (WebClientResponseException.NotFound e) {
            // User not found
            return null;
        } catch (Exception e) {
            System.err.println("Error getting user email: " + e.getMessage());
            return null;
        }
    }

    /**
     * Gets full user details by user ID
     * 
     * @param userId The user ID
     * @return UserDTO or null if not found
     */
    public UserDTO getUserDetails(UUID userId) {
        try {
            return webClient.get()
                    .uri("/users/{id}", userId)
                    .retrieve()
                    .bodyToMono(UserDTO.class)
                    .block();
        } catch (WebClientResponseException.NotFound e) {
            return null;
        } catch (Exception e) {
            System.err.println("Error getting user details: " + e.getMessage());
            return null;
        }
    }

    /**
     * DTO for user data received from User Service
     * Matches the UserDTO structure from user-service
     */
    public static class UserDTO {
        private UUID id;
        private String name;
        private String email;
        
        // Getters and Setters
        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
}

