package com.example.E_Wallet.Util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {

    private SecretKey secretKey;

    private static final long EXPIRATION_TIME = 15 * 60 * 1000; // 15 minutes

    @PostConstruct
    public void initializeSecretKey() {
        try {

            KeyGenerator keyGenerator = KeyGenerator.getInstance("HmacSHA256");

            keyGenerator.init(256);

            SecretKey generatedKey = keyGenerator.generateKey();

            byte[] keyBytes = generatedKey.getEncoded();

            byte[] finalKeyBytes = new byte[32];
            if (keyBytes.length >= 32) {
                System.arraycopy(keyBytes, 0, finalKeyBytes, 0, 32);
            } else {
                System.arraycopy(keyBytes, 0, finalKeyBytes, 0, keyBytes.length);
                java.security.SecureRandom random = new java.security.SecureRandom();
                byte[] padding = new byte[32 - keyBytes.length];
                random.nextBytes(padding);
                System.arraycopy(padding, 0, finalKeyBytes, keyBytes.length, padding.length);
            }

            this.secretKey = Keys.hmacShaKeyFor(finalKeyBytes);

            System.out.println("JWT Secret Key generated successfully using KeyGenerator");

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to initialize JWT secret key", e);
        }
    }

    public String generateToken(UUID userId, String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);

        return Jwts.builder()
                .subject(email)
                .claim("userId", userId.toString())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Validates a JWT token by checking its signature and expiration.
     * 
     * @param token The JWT token string to validate
     * @return true if the token is valid, false otherwise
     * 
     * WHY: This method ensures that:
     * 1. The token hasn't been tampered with (signature verification)
     * 2. The token hasn't expired
     * 3. The token format is correct
     */
    public boolean validateToken(String token) {
        try {
            // Parse and verify the token using the secret key
            // If the token is invalid, expired, or tampered with, an exception will be thrown
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            // Token has expired
            System.err.println("JWT token has expired: " + e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            // Token format is invalid
            System.err.println("Invalid JWT token format: " + e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            // Token is not supported
            System.err.println("Unsupported JWT token: " + e.getMessage());
            return false;
        } catch (SignatureException e) {
            // Token signature doesn't match (token was tampered with)
            System.err.println("Invalid JWT signature: " + e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            // Token is null or empty
            System.err.println("JWT token is null or empty: " + e.getMessage());
            return false;
        } catch (Exception e) {
            // Catch any other unexpected exceptions
            System.err.println("Error validating JWT token: " + e.getMessage());
            return false;
        }
    }

    /**
     * Extracts the email (subject) from a JWT token.
     * 
     * @param token The JWT token string
     * @return The email address from the token's subject claim
     * 
     * WHY: The email is stored as the "subject" of the JWT token.
     * We need this to identify which user the token belongs to.
     */
    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    /**
     * Extracts the user ID from a JWT token.
     * 
     * @param token The JWT token string
     * @return The UUID of the user from the token's userId claim
     * 
     * WHY: The userId is stored as a custom claim in the JWT.
     * We need this to load the full user details from the database.
     */
    public UUID getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        String userIdString = claims.get("userId", String.class);
        return UUID.fromString(userIdString);
    }

    /**
     * Checks if a JWT token has expired.
     * 
     * @param token The JWT token string
     * @return true if the token is expired, false otherwise
     * 
     * WHY: Even if validateToken() checks expiration, sometimes we need
     * to check this separately for more specific error handling.
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true; // If we can't parse, consider it expired
        }
    }
}
