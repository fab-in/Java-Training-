package com.example.E_Wallet.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
           "WHERE su.id = :userId OR ru.id = :userId")
    Page<Transaction> findByUserId(@Param("userId") UUID userId, Pageable pageable);
   
    @Query("SELECT DISTINCT t FROM Transaction t " +
           "LEFT JOIN FETCH t.senderWallet sw " +
           "LEFT JOIN FETCH sw.user su " +
           "LEFT JOIN FETCH t.receiverWallet rw " +
           "LEFT JOIN FETCH rw.user ru")
    Page<Transaction> findAllWithDetails(Pageable pageable);
   
    @Query("SELECT DISTINCT t FROM Transaction t " +
           "LEFT JOIN FETCH t.senderWallet sw " +
           "LEFT JOIN FETCH sw.user su " +
           "LEFT JOIN FETCH t.receiverWallet rw " +
           "LEFT JOIN FETCH rw.user ru " +
           "WHERE (su.id = :userId OR ru.id = :userId) " +
           "AND t.remarks = :remark")
    Page<Transaction> findByUserIdAndRemark(@Param("userId") UUID userId, 
                                            @Param("remark") String remark, 
                                            Pageable pageable);
    
   
    @Query("SELECT DISTINCT t FROM Transaction t " +
           "LEFT JOIN FETCH t.senderWallet sw " +
           "LEFT JOIN FETCH sw.user su " +
           "LEFT JOIN FETCH t.receiverWallet rw " +
           "LEFT JOIN FETCH rw.user ru " +
           "WHERE t.remarks = :remark")
    Page<Transaction> findAllByRemark(@Param("remark") String remark, Pageable pageable);
    
   
    @Query("SELECT DISTINCT t FROM Transaction t " +
           "LEFT JOIN FETCH t.senderWallet sw " +
           "LEFT JOIN FETCH sw.user su " +
           "LEFT JOIN FETCH t.receiverWallet rw " +
           "LEFT JOIN FETCH rw.user ru " +
           "WHERE (su.id = :userId OR ru.id = :userId) " +
           "AND LOWER(t.status) = 'failed'")
    Page<Transaction> findFailedTransactionsByUserId(@Param("userId") UUID userId, Pageable pageable);
    
   
    @Query("SELECT DISTINCT t FROM Transaction t " +
           "LEFT JOIN FETCH t.senderWallet sw " +
           "LEFT JOIN FETCH sw.user su " +
           "LEFT JOIN FETCH t.receiverWallet rw " +
           "LEFT JOIN FETCH rw.user ru " +
           "WHERE LOWER(t.status) = 'failed'")
    Page<Transaction> findAllFailedTransactions(Pageable pageable);
    
    // Get all transactions for a user without pagination (for statement generation)
    @Query("SELECT DISTINCT t FROM Transaction t " +
           "LEFT JOIN FETCH t.senderWallet sw " +
           "LEFT JOIN FETCH sw.user su " +
           "LEFT JOIN FETCH t.receiverWallet rw " +
           "LEFT JOIN FETCH rw.user ru " +
           "WHERE su.id = :userId OR ru.id = :userId " +
           "ORDER BY t.transactionDate DESC")
    List<Transaction> findAllByUserId(@Param("userId") UUID userId);
}

