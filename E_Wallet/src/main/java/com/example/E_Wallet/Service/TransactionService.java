package com.example.E_Wallet.Service;

import com.example.E_Wallet.DTO.TransactionDTO;
import com.example.E_Wallet.Model.Transaction;
import com.example.E_Wallet.Model.User;
import com.example.E_Wallet.Repository.TransactionRepo;
import com.example.E_Wallet.Security.SecurityUtil;
import com.example.E_Wallet.Exceptions.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TransactionService {

    @Autowired
    private TransactionRepo transactionRepo;

    @Autowired
    private SecurityUtil securityUtil;

    public List<TransactionDTO> getTransactions() {
        User currentUser = securityUtil.getCurrentUser();

        if (currentUser == null) {
            throw new ValidationException("User not authenticated");
        }

        List<Transaction> transactions;

        if (securityUtil.isAdmin()) {
            // Admin can view all transactions
            transactions = transactionRepo.findAllWithDetails();
        } else {
            // Users can only see their own transactions (as sender or receiver)
            transactions = transactionRepo.findByUserId(currentUser.getId());
        }

        return transactions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private TransactionDTO convertToDTO(Transaction transaction) {
        TransactionDTO dto = new TransactionDTO();
        dto.setId(transaction.getId());
        dto.setSenderWalletId(transaction.getSenderWallet().getId());
        dto.setReceiverWalletId(transaction.getReceiverWallet().getId());
        dto.setAmount(transaction.getAmount());
        dto.setTransactionDate(transaction.getTransactionDate());
        dto.setStatus(transaction.getStatus());
        dto.setRemarks(transaction.getRemarks());
        return dto;
    }
}

