package com.example.E_Wallet.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Authentication Response DTO
 * 
 * This DTO is returned after successful login or signup.
 * It contains:
 * - token: The JWT token that the user will use for authenticated requests
 * - message: A success message
 * - user: Basic user information (without password)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponseDTO {
    private String token;
    private String message;
    private UserDTO user;
}

