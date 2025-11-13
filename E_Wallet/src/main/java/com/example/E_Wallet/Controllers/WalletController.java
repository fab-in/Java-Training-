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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.E_Wallet.Service.WalletService;
import com.example.E_Wallet.DTO.WalletDTO;
import com.example.E_Wallet.DTO.WalletCreateDTO;
import com.example.E_Wallet.DTO.WalletUpdateDTO;
import com.example.E_Wallet.DTO.CreditRequestDTO;
import com.example.E_Wallet.DTO.WithdrawalRequestDTO;
import com.example.E_Wallet.DTO.TransferRequestDTO;
import com.example.E_Wallet.DTO.MessageResponseDTO;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public class WalletController {

    @Autowired
    private WalletService walletService;

    @GetMapping("/wallets")
    public ResponseEntity<List<WalletDTO>> getWallets() {
        List<WalletDTO> wallets = walletService.getWallets();
        return ResponseEntity.ok(wallets);
    }

    @GetMapping("/wallets/{id}")
    public ResponseEntity<WalletDTO> getWalletById(@PathVariable UUID id) {
        WalletDTO walletDTO = walletService.getWalletById(id);
        return ResponseEntity.ok(walletDTO);
    }

    @PostMapping("/wallets")
    public ResponseEntity<WalletDTO> createWallet(@Valid @RequestBody WalletCreateDTO walletCreateDTO) {
        WalletDTO walletDTO = walletService.createWallet(walletCreateDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body("wallet added sucesfully");
    }

    @PutMapping("/wallets")
    public ResponseEntity<WalletDTO> updateWallet(@Valid @RequestBody WalletUpdateDTO walletUpdateDTO) {
        WalletDTO walletDTO = walletService.updateWallet(walletUpdateDTO);
        return ResponseEntity.ok(walletDTO);
    }

    @DeleteMapping("/wallets")
    public ResponseEntity<Map<String, String>> deleteWallet(
            @RequestParam String walletName,
            @RequestParam String userIdentifier) {
        walletService.deleteWallet(walletName, userIdentifier);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Wallet deleted successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/wallets/credit")
    public ResponseEntity<MessageResponseDTO> creditWallet(@Valid @RequestBody CreditRequestDTO creditRequestDTO) {
        String message = walletService.creditWallet(creditRequestDTO);
        return ResponseEntity.ok(new MessageResponseDTO(message));
    }

    @PostMapping("/wallets/withdraw")
    public ResponseEntity<MessageResponseDTO> withdrawWallet(
            @Valid @RequestBody WithdrawalRequestDTO withdrawalRequestDTO) {
        String message = walletService.withdrawWallet(withdrawalRequestDTO);
        return ResponseEntity.ok(new MessageResponseDTO(message));
    }

    @PostMapping("/wallets/transfer")
    public ResponseEntity<MessageResponseDTO> transferFunds(@Valid @RequestBody TransferRequestDTO transferRequestDTO) {
        String message = walletService.transferFunds(transferRequestDTO);
        return ResponseEntity.ok(new MessageResponseDTO(message));
    }
}
