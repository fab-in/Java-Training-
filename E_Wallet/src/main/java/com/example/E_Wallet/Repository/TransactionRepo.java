package com.example.E_Wallet.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.E_Wallet.Model.Transaction;
import java.util.List;
import java.util.UUID;

public interface TransactionRepo extends JpaRepository<Transaction, UUID> {
    
    @Query("SELECT DISTINCT t FROM Transaction t " +
           "LEFT JOIN FETCH t.senderWallet sw " +
           "LEFT JOIN FETCH sw.user su " +
           "LEFT JOIN FETCH t.receiverWallet rw " +
           "LEFT JOIN FETCH rw.user ru " +
           "WHERE su.id = :userId OR ru.id = :userId " +
           "ORDER BY t.transactionDate DESC")
    List<Transaction> findByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT DISTINCT t FROM Transaction t " +
           "LEFT JOIN FETCH t.senderWallet sw " +
           "LEFT JOIN FETCH sw.user su " +
           "LEFT JOIN FETCH t.receiverWallet rw " +
           "LEFT JOIN FETCH rw.user ru " +
           "ORDER BY t.transactionDate DESC")
    List<Transaction> findAllWithDetails();
}

