package com.example.E_Wallet.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.E_Wallet.Model.Wallet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WalletRepo extends JpaRepository<Wallet, UUID> {
    boolean existsByAccountNumber(String accountNumber);
    List<Wallet> findByUserId(UUID userId);
    Optional<Wallet> findByWalletNameAndUserEmail(String walletName, String userEmail);
    Optional<Wallet> findByWalletNameAndUserName(String walletName, String userName);
}

