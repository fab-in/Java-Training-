package com.example.E_Wallet.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WalletDTO {
    private UUID walletId;
    private UUID userId;
    private String walletName;
    private LocalDateTime createdAt;
}

