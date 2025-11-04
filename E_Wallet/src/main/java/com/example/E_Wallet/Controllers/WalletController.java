package com.example.E_Wallet.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.example.E_Wallet.Service.WalletService;
import com.example.E_Wallet.DTO.WalletDTO;
import com.example.E_Wallet.DTO.WalletCreateDTO;
import jakarta.validation.Valid;
import java.util.List;

@RestController
public class WalletController {

    @Autowired
    private WalletService walletService;

    @GetMapping("/wallets")
    public List<WalletDTO> getWallets() {
        return walletService.getWallets();
    }

    @GetMapping("/wallets/{id}")
    public ResponseEntity<WalletDTO> getWalletById(@PathVariable Long id) {
        WalletDTO walletDTO = walletService.getWalletById(id);
        return ResponseEntity.ok(walletDTO);
    }

    @PostMapping("/wallets")
    public ResponseEntity<WalletDTO> createWallet(@Valid @RequestBody WalletCreateDTO walletCreateDTO) {
        WalletDTO walletDTO = walletService.createWallet(walletCreateDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(walletDTO);
    }

    @PutMapping("/wallets/{id}")
    public ResponseEntity<WalletDTO> updateWallet(@PathVariable Long id, @Valid @RequestBody WalletCreateDTO walletCreateDTO) {
        WalletDTO walletDTO = walletService.updateWallet(id, walletCreateDTO);
        return ResponseEntity.ok(walletDTO);
    }

    @DeleteMapping("/wallets/{id}")
    public ResponseEntity<Void> deleteWallet(@PathVariable Long id) {
        walletService.deleteWallet(id);
        return ResponseEntity.noContent().build();
    }
}
