package com.example.E_Wallet.Util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {

    // Secret key generated at application startup
    private SecretKey secretKey;
    
    private static final long EXPIRATION_TIME = 15 * 60 * 1000; // 15 minutes

    
    @PostConstruct
    public void initializeSecretKey() {
        try {
            // Create a KeyGenerator for HMAC-SHA algorithms
            KeyGenerator keyGenerator = KeyGenerator.getInstance("HmacSHA256");
            
            // Initialize with 256 bits (32 bytes) key size for HMAC-SHA256
            keyGenerator.init(256);
            
            // Generate a secure random key
            SecretKey generatedKey = keyGenerator.generateKey();
            
            // Convert the key bytes and use Keys.hmacShaKeyFor to ensure proper format
            byte[] keyBytes = generatedKey.getEncoded();
            
            // Ensure we have at least 32 bytes (256 bits) for HMAC-SHA256
            byte[] finalKeyBytes = new byte[32];
            if (keyBytes.length >= 32) {
                System.arraycopy(keyBytes, 0, finalKeyBytes, 0, 32);
            } else {
                // If generated key is shorter, pad it (shouldn't happen with proper init)
                System.arraycopy(keyBytes, 0, finalKeyBytes, 0, keyBytes.length);
                // Fill remaining bytes with secure random padding
                java.security.SecureRandom random = new java.security.SecureRandom();
                byte[] padding = new byte[32 - keyBytes.length];
                random.nextBytes(padding);
                System.arraycopy(padding, 0, finalKeyBytes, keyBytes.length, padding.length);
            }
            
            // Create the SecretKey for JWT operations
            this.secretKey = Keys.hmacShaKeyFor(finalKeyBytes);
            
            System.out.println("JWT Secret Key generated successfully using KeyGenerator");
            
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to initialize JWT secret key", e);
        }
    }

    /**
     * Generate a JWT token for the given user
     * 
     * @param userId User ID
     * @param email User email
     * @return JWT token string
     */
    public String generateToken(UUID userId, String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);

        // Build the JWT token using the generated secret key
        return Jwts.builder()
                .subject(email)                          // Set subject (email)
                .claim("userId", userId)                  // Add user ID as a claim
                .issuedAt(now)                            // Set issue time
                .expiration(expiryDate)                   // Set expiration time
                .signWith(secretKey)                      // Sign with generated secret key
                .compact();                               // Build and return as string
    }
}

