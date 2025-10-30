package com.example.E_Wallet.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.E_Wallet.Model.User;
import com.example.E_Wallet.Model.Wallet;
import com.example.E_Wallet.Repository.UserRepo;
import com.example.E_Wallet.Repository.WalletRepo;

@Service
public class WalletService {

    @Autowired
    private WalletRepo walletRepo;

    @Autowired
    private UserRepo userRepo;

    public Wallet createWalletForUser(Long userId, Wallet wallet) {
        Optional<User> userOpt = userRepo.findById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found with id: " + userId);
        }
        User user = userOpt.get();
        wallet.setUser(user);
        if (wallet.getCreatedAt() == null) {
            wallet.setCreatedAt(LocalDateTime.now());
        }
        return walletRepo.save(wallet);
    }

    public List<Wallet> getWalletsByUser(Long userId) {
        return walletRepo.findByUserId(userId);
    }

    public Wallet getWalletById(Long walletId) {
        return walletRepo.findById(walletId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found with id: " + walletId));
    }
}


