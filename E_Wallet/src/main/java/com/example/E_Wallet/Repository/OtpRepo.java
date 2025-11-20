package com.example.E_Wallet.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.E_Wallet.Model.Otp;
import java.util.Optional;
import java.util.UUID;

public interface OtpRepo extends JpaRepository<Otp, UUID> {
    
    Optional<Otp> findByTransactionId(UUID transactionId);
    
    Optional<Otp> findByTransactionIdAndIsVerifiedFalse(UUID transactionId);
}


