package com.example.wallet_service.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

/**
 * Message DTO for Transaction Completed Event
 * Published by Wallet Service after processing transaction
 * Consumed by Transaction Service to update transaction status
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionCompletedEvent {
    private UUID transactionId;
    private String status; // "SUCCESS" or "FAILED"
    private String remarks;
    private Long timestamp;
}

