package com.example.E_Wallet.Controllers;

import com.example.E_Wallet.DTO.TransactionDTO;
import com.example.E_Wallet.Service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
}

