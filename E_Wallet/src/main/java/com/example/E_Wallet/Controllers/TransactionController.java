package com.example.E_Wallet.Controllers;

import com.example.E_Wallet.DTO.TransactionDTO;
import com.example.E_Wallet.Service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionDTO>> getTransactions() {
        List<TransactionDTO> transactions = transactionService.getTransactions();
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/transactions/credits")
    public ResponseEntity<List<TransactionDTO>> getCreditTransactions() {
        return ResponseEntity.ok(transactionService.getCreditTransactions());
    }

    @GetMapping("/transactions/withdrawals")
    public ResponseEntity<List<TransactionDTO>> getWithdrawalTransactions() {
        return ResponseEntity.ok(transactionService.getWithdrawalTransactions());
    }

    @GetMapping("/transactions/transfers")
    public ResponseEntity<List<TransactionDTO>> getTransferTransactions() {
        return ResponseEntity.ok(transactionService.getTransferTransactions());
    }

    @GetMapping("/transactions/failed")
    public ResponseEntity<List<TransactionDTO>> getFailedTransactions() {
        return ResponseEntity.ok(transactionService.getFailedTransactions());
    }

    @GetMapping("/transactions/sorted")
    public ResponseEntity<List<TransactionDTO>> getTransactionsSorted(
            @RequestParam(name = "order", defaultValue = "newest") String sortOrder) {
        return ResponseEntity.ok(transactionService.getTransactionsSorted(sortOrder));
    }
}

