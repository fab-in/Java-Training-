package com.example.E_Wallet.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.E_Wallet.Model.Wallet;
import java.util.List;
import java.util.UUID;

public interface WalletRepo extends JpaRepository<Wallet, Long> {
    boolean existsByAccountNumber(String accountNumber);
    
    /**
     * Finds all wallets belonging to a specific user.
     * 
     * WHY THIS METHOD:
     * - Used to filter wallets by userId for regular users
     * - Spring Data JPA automatically generates the query based on method name
     * - "findByUser_Id" means: find wallets where wallet.user.id equals the parameter
     * 
     * HOW IT WORKS:
     * - Spring Data JPA parses the method name "findByUser_Id"
     * - It understands "User" refers to the Wallet.user field
     * - It understands "_Id" refers to the User.id field
     * - It generates: SELECT * FROM wallets WHERE user_id = ?
     */
    List<Wallet> findByUser_Id(UUID userId);
}

