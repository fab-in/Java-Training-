package com.example.E_Wallet.Controllers;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.E_Wallet.Model.Wallet;
import com.example.E_Wallet.Service.WalletService;

@RestController
@RequestMapping
public class WalletController {

    @Autowired
    private WalletService walletService;

    @PostMapping("/users/{userId}/wallets")
    public Wallet createWallet(@PathVariable Long userId, @RequestBody Wallet wallet) {
        return walletService.createWalletForUser(userId, wallet);
    }

    @GetMapping("/users/{userId}/wallets")
    public List<Wallet> getWalletsByUser(@PathVariable Long userId) {
        return walletService.getWalletsByUser(userId);
    }

    @GetMapping("/wallets/{walletId}")
    public Wallet getWalletById(@PathVariable Long walletId) {
        return walletService.getWalletById(walletId);
    }
}


