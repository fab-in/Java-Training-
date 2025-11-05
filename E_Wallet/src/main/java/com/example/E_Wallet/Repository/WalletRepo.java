package com.example.E_Wallet.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.E_Wallet.Model.Wallet;

public interface WalletRepo extends JpaRepository<Wallet, Long> {
    
    boolean existsByAccountNumber(String accountNumber);
    
    @Query("SELECT COUNT(w) > 0 FROM Wallet w WHERE w.accountNumber = :accountNumber AND w.id != :id")
    boolean existsByAccountNumberExcludingId(@Param("accountNumber") String accountNumber, @Param("id") Long id);
}
