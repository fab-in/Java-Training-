package com.example.transaction_service.Service;

import com.example.transaction_service.Config.RabbitMQConfig;
import com.example.transaction_service.DTO.TransactionCreatedEvent;
import com.example.transaction_service.DTO.TransactionCompletedEvent;
import com.example.transaction_service.Model.Transaction;
import com.example.transaction_service.Repository.TransactionRepo;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service for consuming RabbitMQ messages
 * Listens for transaction events from Wallet Service
 */
@Service
public class TransactionEventConsumer {
    
    @Autowired
    private TransactionRepo transactionRepo;
    
    @Autowired
    private OtpService otpService;
    
    /**
     * Consumes transaction created events from Wallet Service
     * Creates transaction record and generates OTP
     */
    @RabbitListener(queues = RabbitMQConfig.TRANSACTION_CREATED_QUEUE)
    @Transactional
    public void handleTransactionCreated(TransactionCreatedEvent event) {
        System.out.println("Received transaction.created event: " + event.getTransactionId());
        
        try {
            // Check if transaction already exists (idempotency check)
            // This prevents processing the same message multiple times
            if (transactionRepo.existsById(event.getTransactionId())) {
                System.out.println("Transaction already exists, skipping: " + event.getTransactionId());
                return; // Exit early - transaction already processed
            }
            
            // Create transaction record
            Transaction transaction = new Transaction();
            transaction.setId(event.getTransactionId()); // Set ID before save
            transaction.setSenderWalletId(event.getSenderWalletId());
            transaction.setReceiverWalletId(event.getReceiverWalletId());
            transaction.setAmount(event.getAmount());
            transaction.setTransactionDate(LocalDateTime.now());
            transaction.setStatus("PENDING");
            transaction.setRemarks(event.getRemarks() != null ? event.getRemarks() : 
                event.getTransactionType() + " transaction pending OTP verification");
            
            // Use persist instead of save to avoid merge issues
            transactionRepo.save(transaction);
            
            // Generate and send OTP
            String userEmail = event.getUserEmail();
            if (userEmail == null || userEmail.trim().isEmpty()) {
                userEmail = "user@example.com"; // Fallback
            }
            
            otpService.createAndSendOtp(
                event.getTransactionId(),
                event.getUserId(),
                userEmail,
                event.getTransactionType()
            );
            
            System.out.println("Transaction created and OTP sent: " + event.getTransactionId());
            
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // Transaction already exists (race condition or duplicate message)
            System.out.println("Transaction already exists (duplicate): " + event.getTransactionId());
            // Don't throw exception - acknowledge message to prevent retry loop
        } catch (org.springframework.orm.ObjectOptimisticLockingFailureException e) {
            // Transaction was updated by another process
            System.out.println("Transaction was modified by another process: " + event.getTransactionId());
            // Check if it was already processed
            if (transactionRepo.existsById(event.getTransactionId())) {
                System.out.println("Transaction already processed, skipping: " + event.getTransactionId());
                // Don't throw - acknowledge message
            } else {
                // Retry might help
                throw new RuntimeException("Optimistic locking failure, will retry", e);
            }
        } catch (Exception e) {
            System.err.println("Error processing transaction.created event: " + e.getMessage());
            e.printStackTrace();
            // Only throw if it's not a duplicate/race condition
            // This allows RabbitMQ to retry, but idempotency check prevents infinite loop
            throw new RuntimeException("Failed to process transaction event", e);
        }
    }
    
    /**
     * Consumes transaction completed events from Wallet Service
     * Updates transaction status
     */
    @RabbitListener(queues = RabbitMQConfig.TRANSACTION_COMPLETED_QUEUE)
    @Transactional
    public void handleTransactionCompleted(TransactionCompletedEvent event) {
        System.out.println("Received transaction.completed event: " + event.getTransactionId());
        
        try {
            Transaction transaction = transactionRepo.findById(event.getTransactionId())
                    .orElseThrow(() -> new RuntimeException(
                            "Transaction not found: " + event.getTransactionId()));
            
            transaction.setStatus(event.getStatus());
            transaction.setRemarks(event.getRemarks());
            
            transactionRepo.save(transaction);
            
            System.out.println("Transaction status updated: " + event.getTransactionId() + " - " + event.getStatus());
            
        } catch (Exception e) {
            System.err.println("Error processing transaction.completed event: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

