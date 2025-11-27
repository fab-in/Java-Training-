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

@Service
public class TransactionEventConsumer {

    @Autowired
    private TransactionRepo transactionRepo;

    @Autowired
    private OtpService otpService;

    @RabbitListener(queues = RabbitMQConfig.TRANSACTION_CREATED_QUEUE)
    @Transactional
    public void handleTransactionCreated(TransactionCreatedEvent event) {
        System.out.println("Received transaction.created event: " + event.getTransactionId());

        try {

            if (transactionRepo.existsById(event.getTransactionId())) {
                System.out.println("Transaction already exists, skipping: " + event.getTransactionId());
                return;
            }

            Transaction transaction = new Transaction();
            transaction.setId(event.getTransactionId());
            transaction.setSenderWalletId(event.getSenderWalletId());
            transaction.setReceiverWalletId(event.getReceiverWalletId());
            transaction.setAmount(event.getAmount());
            transaction.setTransactionDate(LocalDateTime.now());
            transaction.setStatus("PENDING");
            transaction.setRemarks(event.getRemarks() != null ? event.getRemarks()
                    : event.getTransactionType() + " transaction pending OTP verification");

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
                    event.getTransactionType());

            System.out.println("Transaction created and OTP sent: " + event.getTransactionId());

        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            System.out.println("Transaction already exists (duplicate): " + event.getTransactionId());
        } catch (org.springframework.orm.ObjectOptimisticLockingFailureException e) {
            System.out.println("Transaction was modified by another process: " + event.getTransactionId());
            if (transactionRepo.existsById(event.getTransactionId())) {
                System.out.println("Transaction already processed, skipping: " + event.getTransactionId());
            } else {
                throw new RuntimeException("Optimistic locking failure, will retry", e);
            }
        } catch (Exception e) {
            System.err.println("Error processing transaction.created event: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to process transaction event", e);
        }
    }

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
