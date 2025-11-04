package com.example.E_Wallet.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.E_Wallet.Model.Wallet;
import com.example.E_Wallet.Model.User;
import com.example.E_Wallet.DTO.WalletDTO;
import com.example.E_Wallet.DTO.WalletCreateDTO;
import com.example.E_Wallet.Repository.WalletRepo;
import com.example.E_Wallet.Repository.UserRepo;
import com.example.E_Wallet.Exceptions.ResourceNotFoundException;
import com.example.E_Wallet.Exceptions.ValidationException;
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
        // Validate required fields
        if (walletCreateDTO.getUserId() == null) {
            throw new ValidationException("User ID is required");
        }

        if (walletCreateDTO.getWalletName() == null || walletCreateDTO.getWalletName().trim().isEmpty()) {
            throw new ValidationException("Wallet name is required and cannot be empty");
        }

        if (walletCreateDTO.getAccountNumber() == null || walletCreateDTO.getAccountNumber().trim().isEmpty()) {
            throw new ValidationException("Account number is required and cannot be empty");
        }

        if (walletCreateDTO.getPasscode() == null || walletCreateDTO.getPasscode().trim().isEmpty()) {
            throw new ValidationException("Passcode is required and cannot be empty");
        }

        // Validate user exists
        User user = userRepo.findById(walletCreateDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + walletCreateDTO.getUserId()));

        // Check if account number already exists
        boolean accountNumberExists = walletRepo.findAll().stream()
                .anyMatch(w -> w.getAccountNumber().equals(walletCreateDTO.getAccountNumber()));
        if (accountNumberExists) {
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

    public WalletDTO updateWallet(Long id, WalletCreateDTO walletCreateDTO) {
        Wallet wallet = walletRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found with id: " + id));

        // Update wallet name if provided
        if (walletCreateDTO.getWalletName() != null && !walletCreateDTO.getWalletName().trim().isEmpty()) {
            wallet.setWalletName(walletCreateDTO.getWalletName());
        }

        // Update account number if provided and different from current
        if (walletCreateDTO.getAccountNumber() != null && !walletCreateDTO.getAccountNumber().trim().isEmpty()) {
            if (!walletCreateDTO.getAccountNumber().equals(wallet.getAccountNumber())) {
                // Check if new account number already exists
                boolean accountNumberExists = walletRepo.findAll().stream()
                        .anyMatch(w -> w.getAccountNumber().equals(walletCreateDTO.getAccountNumber()));
                if (accountNumberExists) {
                    throw new DuplicateResourceException("Wallet with account number '" + walletCreateDTO.getAccountNumber() + "' already exists");
                }
                wallet.setAccountNumber(walletCreateDTO.getAccountNumber());
            }
        }

        // Update balance if provided
        if (walletCreateDTO.getBalance() != null) {
            if (walletCreateDTO.getBalance() < 0) {
                throw new ValidationException("Balance cannot be negative");
            }
            wallet.setBalance(walletCreateDTO.getBalance());
        }

        // Update passcode if provided
        if (walletCreateDTO.getPasscode() != null && !walletCreateDTO.getPasscode().trim().isEmpty()) {
            wallet.setPasscode(walletCreateDTO.getPasscode());
        }

        // Update user if provided
        if (walletCreateDTO.getUserId() != null && !walletCreateDTO.getUserId().equals(wallet.getUser().getId())) {
            User user = userRepo.findById(walletCreateDTO.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + walletCreateDTO.getUserId()));
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
