package com.example.E_Wallet.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.E_Wallet.Model.Wallet;
import com.example.E_Wallet.Model.User;
import com.example.E_Wallet.DTO.WalletDTO;
import com.example.E_Wallet.DTO.WalletCreateDTO;
import com.example.E_Wallet.DTO.WalletUpdateDTO;
import com.example.E_Wallet.Repository.WalletRepo;
import com.example.E_Wallet.Repository.UserRepo;
import com.example.E_Wallet.Exceptions.ResourceNotFoundException;
import com.example.E_Wallet.Exceptions.DuplicateResourceException;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

@Service
public class WalletService {

    @Autowired
    private WalletRepo walletRepo;

    @Autowired
    private UserRepo userRepo;

    public List<WalletDTO> getWallets() {
        return walletRepo.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public WalletDTO getWalletById(Long id) {
        Wallet wallet = walletRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found with id: " + id));
        return convertToDTO(wallet);
    }

    public WalletDTO createWallet(WalletCreateDTO walletCreateDTO) {
        // Validate user exists
        User user = userRepo.findById(walletCreateDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + walletCreateDTO.getUserId()));

        // Check if account number already exists
        if (walletRepo.existsByAccountNumber(walletCreateDTO.getAccountNumber())) {
            throw new DuplicateResourceException("Wallet with account number '" + walletCreateDTO.getAccountNumber() + "' already exists");
        }

        // Create wallet entity
        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setWalletName(walletCreateDTO.getWalletName());
        wallet.setAccountNumber(walletCreateDTO.getAccountNumber());
        wallet.setBalance(walletCreateDTO.getBalance() != null ? walletCreateDTO.getBalance() : 0.0);
        wallet.setPasscode(walletCreateDTO.getPasscode());
        wallet.setCreatedAt(LocalDateTime.now());

        Wallet savedWallet = walletRepo.save(wallet);
        return convertToDTO(savedWallet);
    }

    public WalletDTO updateWallet(Long id, WalletUpdateDTO walletUpdateDTO) {
        Wallet wallet = walletRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found with id: " + id));

        // Update wallet name if provided
        if (walletUpdateDTO.getWalletName() != null && !walletUpdateDTO.getWalletName().trim().isEmpty()) {
            wallet.setWalletName(walletUpdateDTO.getWalletName());
        }

        // Update account number only if provided, different from current, and not already taken
        if (walletUpdateDTO.getAccountNumber() != null && !walletUpdateDTO.getAccountNumber().trim().isEmpty()) {
            String newAccountNumber = walletUpdateDTO.getAccountNumber().trim();
            String currentAccountNumber = wallet.getAccountNumber() != null ? wallet.getAccountNumber().trim() : "";
            
            // Only check for duplicates if the account number is actually changing
            // Compare both values after trimming to ensure exact match
            if (!newAccountNumber.equals(currentAccountNumber)) {
                // Check if new account number already exists in other wallets
                if (walletRepo.existsByAccountNumberExcludingId(newAccountNumber, id)) {
                    throw new DuplicateResourceException("Wallet with account number '" + newAccountNumber + "' already exists");
                }
                wallet.setAccountNumber(newAccountNumber);
            }
            // If account number is the same, do nothing - no need to update
        }

        // Update balance if provided
        if (walletUpdateDTO.getBalance() != null) {
            wallet.setBalance(walletUpdateDTO.getBalance());
        }

        // Update passcode if provided
        if (walletUpdateDTO.getPasscode() != null && !walletUpdateDTO.getPasscode().trim().isEmpty()) {
            wallet.setPasscode(walletUpdateDTO.getPasscode());
        }

        // Update user if provided and different from current
        if (walletUpdateDTO.getUserId() != null && !walletUpdateDTO.getUserId().equals(wallet.getUser().getId())) {
            User user = userRepo.findById(walletUpdateDTO.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + walletUpdateDTO.getUserId()));
            wallet.setUser(user);
        }

        Wallet updatedWallet = walletRepo.save(wallet);
        return convertToDTO(updatedWallet);
    }

    public void deleteWallet(Long id) {
        Wallet wallet = walletRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found with id: " + id));
        walletRepo.delete(wallet);
    }

    private WalletDTO convertToDTO(Wallet wallet) {
        WalletDTO walletDTO = new WalletDTO();
        walletDTO.setId(wallet.getId());
        walletDTO.setUserId(wallet.getUser().getId());
        walletDTO.setWalletName(wallet.getWalletName());
        walletDTO.setCreatedAt(wallet.getCreatedAt());
        return walletDTO;
    }
}
