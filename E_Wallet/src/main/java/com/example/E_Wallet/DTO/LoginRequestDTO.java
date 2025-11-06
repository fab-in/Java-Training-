package com.example.E_Wallet.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Login Request DTO
 * 
 * This DTO is used when a user wants to log in.
 * It contains:
 * - email: The user's email address
 * - password: The user's password (plain text, will be verified)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequestDTO {
    private String email;
    private String password;
}

