package com.example.transaction_service.Client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import java.util.List;
import java.util.UUID;

/**
 * HTTP client for communicating with Wallet Service.
 * Uses Spring WebClient for reactive, non-blocking HTTP requests.
 */
@Component
public class WalletServiceClient {

    private final WebClient webClient;

    public WalletServiceClient(@Value("${wallet.service.url}") String walletServiceUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(walletServiceUrl)
                .build();
    }

    /**
     * Gets all wallets for a user via HTTP GET request
     * Sends X-User-Id header so wallet service can filter wallets by user
     * 
     * @param userId The user ID to fetch wallets for
     * @return List of WalletDTO, empty list if error occurs
     */
    public List<WalletDTO> getUserWallets(UUID userId) {
        try {
            return webClient.get()
                    .uri("/wallets")
                    .header("X-User-Id", userId.toString()) // Pass user ID in header
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<WalletDTO>>() {})
                    .block();
        } catch (WebClientResponseException e) {
            // Handle HTTP errors (4xx, 5xx)
            System.err.println("HTTP Error getting user wallets: " + e.getStatusCode() + " - " + e.getMessage());
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                // User has no wallets, return empty list
                return List.of();
            }
            return List.of();
        } catch (Exception e) {
            // Handle other errors (network, timeout, etc.)
            System.err.println("Error getting user wallets: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * DTO for wallet data received from Wallet Service
     * Matches the WalletDTO structure from wallet-service
     */
    public static class WalletDTO {
        private UUID id;
        private UUID userId;
        private String walletName;
        private String accountNumber;
        private Double balance;
        private java.time.LocalDateTime createdAt;
        
        // Getters and Setters
        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        
        public UUID getUserId() { return userId; }
        public void setUserId(UUID userId) { this.userId = userId; }
        
        public String getWalletName() { return walletName; }
        public void setWalletName(String walletName) { this.walletName = walletName; }
        
        public String getAccountNumber() { return accountNumber; }
        public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
        
        public Double getBalance() { return balance; }
        public void setBalance(Double balance) { this.balance = balance; }
        
        public java.time.LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }
    }
}

