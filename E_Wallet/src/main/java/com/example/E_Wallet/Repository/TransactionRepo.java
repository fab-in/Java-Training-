package com.example.E_Wallet.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.E_Wallet.Model.Transaction;
import java.util.UUID;

public interface TransactionRepo extends JpaRepository<Transaction, UUID> {
}

