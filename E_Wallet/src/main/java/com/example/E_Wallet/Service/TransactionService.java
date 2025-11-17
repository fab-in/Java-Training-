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

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@Transactional
public class TransactionService {

    @Autowired
    private TransactionRepo transactionRepo;

    @Autowired
    private SecurityUtil securityUtil;

    public List<TransactionDTO> getTransactions() {
        return fetchTransactionsForCurrentUser().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<TransactionDTO> getCreditTransactions() {
        return filterTransactionsByRemark("Credit transaction");
    }

    public List<TransactionDTO> getWithdrawalTransactions() {
        return filterTransactionsByRemark("Withdrawal transaction");
    }

    public List<TransactionDTO> getTransferTransactions() {
        return filterTransactionsByRemark("Fund transfer");
    }

    public List<TransactionDTO> getFailedTransactions() {
        return filterTransactions(transaction -> "failed".equalsIgnoreCase(transaction.getStatus()));
    }

    public List<TransactionDTO> getTransactionsSorted(String sortOrder) {
        List<Transaction> transactions = fetchTransactionsForCurrentUser();

        Comparator<Transaction> comparator = Comparator.comparing(Transaction::getTransactionDate);
        if (!"oldest".equalsIgnoreCase(sortOrder)) {
            comparator = comparator.reversed();
        }

        return transactions.stream()
                .sorted(comparator)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private List<TransactionDTO> filterTransactionsByRemark(String remark) {
        return filterTransactions(transaction ->
                transaction.getRemarks() != null && transaction.getRemarks().equalsIgnoreCase(remark));
    }

    private List<TransactionDTO> filterTransactions(Predicate<Transaction> predicate) {
        return fetchTransactionsForCurrentUser().stream()
                .filter(predicate)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private List<Transaction> fetchTransactionsForCurrentUser() {
        User currentUser = securityUtil.getCurrentUser();

        if (currentUser == null) {
            throw new ValidationException("User not authenticated");
        }

        if (securityUtil.isAdmin()) {
            return transactionRepo.findAllWithDetails();
        }

        return transactionRepo.findByUserId(currentUser.getId());
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
