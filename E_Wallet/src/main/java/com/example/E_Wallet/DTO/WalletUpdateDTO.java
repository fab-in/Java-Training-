package com.example.E_Wallet.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WalletUpdateDTO {
    
    @NotNull(message = "Wallet ID is required")
    private Long walletId;
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    private String walletName;
    
    private String accountNumber;
    
    @Min(value = 0, message = "Balance must be 0 or greater")
    private Double balance;
    
    @Pattern(regexp = "^\\d{4}$", message = "Passcode must be exactly 4 digits")
    private String passcode;
}

