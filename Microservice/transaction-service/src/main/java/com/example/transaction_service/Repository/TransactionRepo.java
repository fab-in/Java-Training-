package com.example.transaction_service.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.transaction_service.Model.Transaction;
import java.util.List;
import java.util.UUID;

public interface TransactionRepo extends JpaRepository<Transaction, UUID> {
    
    /**
     * Find transactions where user's wallet is sender or receiver.
     * Note: This requires wallet IDs to be passed in, not user ID directly.
     * The service layer will need to fetch wallet IDs from Wallet Service first.
     */
    @Query("SELECT t FROM Transaction t " +
           "WHERE t.senderWalletId IN :walletIds OR t.receiverWalletId IN :walletIds " +
           "ORDER BY t.transactionDate DESC")
    Page<Transaction> findByWalletIds(@Param("walletIds") List<UUID> walletIds, Pageable pageable);
    
    /**
     * Find all transactions (for admin)
     */
    Page<Transaction> findAll(Pageable pageable);
    
    /**
     * Find transactions by remark where user's wallet is sender or receiver
     */
    @Query("SELECT t FROM Transaction t " +
           "WHERE (t.senderWalletId IN :walletIds OR t.receiverWalletId IN :walletIds) " +
           "AND t.remarks = :remark " +
           "ORDER BY t.transactionDate DESC")
    Page<Transaction> findByWalletIdsAndRemark(@Param("walletIds") List<UUID> walletIds, 
                                                @Param("remark") String remark, 
                                                Pageable pageable);
    
    /**
     * Find all transactions by remark (for admin)
     */
    @Query("SELECT t FROM Transaction t WHERE t.remarks = :remark ORDER BY t.transactionDate DESC")
    Page<Transaction> findAllByRemark(@Param("remark") String remark, Pageable pageable);
    
    /**
     * Find failed transactions where user's wallet is sender or receiver
     */
    @Query("SELECT t FROM Transaction t " +
           "WHERE (t.senderWalletId IN :walletIds OR t.receiverWalletId IN :walletIds) " +
           "AND LOWER(t.status) = 'failed' " +
           "ORDER BY t.transactionDate DESC")
    Page<Transaction> findFailedTransactionsByWalletIds(@Param("walletIds") List<UUID> walletIds, Pageable pageable);
    
    /**
     * Find all failed transactions (for admin)
     */
    @Query("SELECT t FROM Transaction t WHERE LOWER(t.status) = 'failed' ORDER BY t.transactionDate DESC")
    Page<Transaction> findAllFailedTransactions(Pageable pageable);
    
    /**
     * Get all transactions for wallets (without pagination, for statement generation)
     */
    @Query("SELECT t FROM Transaction t " +
           "WHERE t.senderWalletId IN :walletIds OR t.receiverWalletId IN :walletIds " +
           "ORDER BY t.transactionDate DESC")
    List<Transaction> findAllByWalletIds(@Param("walletIds") List<UUID> walletIds);
}
