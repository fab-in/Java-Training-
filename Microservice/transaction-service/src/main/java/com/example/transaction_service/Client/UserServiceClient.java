package com.example.transaction_service.Client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import java.util.UUID;

/**
 * HTTP client for communicating with User Service.
 * Uses Spring WebClient for reactive, non-blocking HTTP requests.
 * This is used to fetch user details for transaction statements.
 */
@Component
public class UserServiceClient {

    private final WebClient webClient;

    public UserServiceClient(@Value("${user.service.url}") String userServiceUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(userServiceUrl)
                .build();
    }

    public UserDTO getUserDetails(UUID userId) {
        try {
            return webClient.get()
                    .uri("/users/{id}", userId)
                    .retrieve()
                    .bodyToMono(UserDTO.class)
                    .block();
        } catch (WebClientResponseException.NotFound e) {
            
            System.err.println("User not found: " + userId);
            return null;
        } catch (WebClientResponseException e) {
            
            System.err.println("HTTP Error getting user details: " + e.getStatusCode() + " - " + e.getMessage());
            return null;
        } catch (Exception e) {
            
            System.err.println("Error getting user details: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

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

