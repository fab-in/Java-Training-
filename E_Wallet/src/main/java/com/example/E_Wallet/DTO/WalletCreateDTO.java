package com.example.E_Wallet.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WalletCreateDTO {
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotNull(message = "Wallet name is required")
    @Size(min = 1, max = 100, message = "Wallet name must be between 1 and 100 characters")
    private String walletName;
    
    @NotNull(message = "Account number is required")
    @Size(min = 1, message = "Account number cannot be empty")
    private String accountNumber;
    
    @PositiveOrZero(message = "Balance must be zero or positive")
    private Double balance;
    
    @Size(min = 4, max = 6, message = "Passcode must be between 4 and 6 characters")
    private String passcode;
}

