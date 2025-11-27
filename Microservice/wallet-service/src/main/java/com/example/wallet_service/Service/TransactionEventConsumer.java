package com.example.wallet_service.Service;

import com.example.wallet_service.Config.RabbitMQConfig;
import com.example.wallet_service.DTO.OtpVerifiedEvent;
import com.example.wallet_service.Exceptions.ResourceNotFoundException;
import com.example.wallet_service.Exceptions.ValidationException;
import com.example.wallet_service.Model.Wallet;
import com.example.wallet_service.Repository.WalletRepo;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TransactionEventConsumer {

    @Autowired
    private WalletRepo walletRepo;

    @Autowired
    private TransactionEventPublisher transactionEventPublisher;

    @RabbitListener(queues = RabbitMQConfig.OTP_VERIFIED_QUEUE)
    @Transactional
    public void handleOtpVerified(OtpVerifiedEvent event) {
        System.out.println("Received OTP verified event for transaction: " + event.getTransactionId());

        try {
            UUID transactionId = event.getTransactionId();
            UUID senderWalletId = event.getSenderWalletId();
            UUID receiverWalletId = event.getReceiverWalletId();
            Double amount = event.getAmount();
            String transactionType = event.getTransactionType();

            Wallet senderWallet = walletRepo.findById(senderWalletId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Sender wallet not found: " + senderWalletId));

            Wallet receiverWallet = walletRepo.findById(receiverWalletId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Receiver wallet not found: " + receiverWalletId));

            // Process transaction based on type
            if ("CREDIT".equalsIgnoreCase(transactionType)) {
                // Credit: Add money to wallet
                senderWallet.setBalance(senderWallet.getBalance() + amount);
                walletRepo.save(senderWallet);

                transactionEventPublisher.publishTransactionCompleted(
                        transactionId,
                        "SUCCESS",
                        "Credit transaction completed successfully");

            } else if ("WITHDRAW".equalsIgnoreCase(transactionType)) {
                // Withdraw: Deduct money from wallet
                if (senderWallet.getBalance() < amount) {
                    transactionEventPublisher.publishTransactionCompleted(
                            transactionId,
                            "FAILED",
                            "Insufficient balance");
                    throw new ValidationException("Insufficient balance");
                }

                senderWallet.setBalance(senderWallet.getBalance() - amount);
                walletRepo.save(senderWallet);

                transactionEventPublisher.publishTransactionCompleted(
                        transactionId,
                        "SUCCESS",
                        "Withdrawal transaction completed successfully");

            } else if ("TRANSFER".equalsIgnoreCase(transactionType)) {
                // Transfer: Deduct from sender, add to receiver
                if (senderWallet.getBalance() < amount) {
                    transactionEventPublisher.publishTransactionCompleted(
                            transactionId,
                            "FAILED",
                            "Insufficient balance");
                    throw new ValidationException("Insufficient balance");
                }

                senderWallet.setBalance(senderWallet.getBalance() - amount);
                receiverWallet.setBalance(receiverWallet.getBalance() + amount);
                walletRepo.save(senderWallet);
                walletRepo.save(receiverWallet);

                transactionEventPublisher.publishTransactionCompleted(
                        transactionId,
                        "SUCCESS",
                        "Transfer transaction completed successfully");
            }

            System.out.println("Transaction processed successfully: " + transactionId);

        } catch (Exception e) {
            System.err.println("Error processing OTP verified event: " + e.getMessage());
            // Publish failure event
            transactionEventPublisher.publishTransactionCompleted(
                    event.getTransactionId(),
                    "FAILED",
                    "Transaction processing failed: " + e.getMessage());
        }
    }
}
