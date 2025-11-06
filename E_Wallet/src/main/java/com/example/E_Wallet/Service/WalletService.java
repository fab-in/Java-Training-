package com.example.E_Wallet.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.E_Wallet.Repository.WalletRepo;
import com.example.E_Wallet.Repository.UserRepo;
import com.example.E_Wallet.Model.Wallet;
import com.example.E_Wallet.Model.User;
import com.example.E_Wallet.DTO.WalletDTO;
import com.example.E_Wallet.DTO.WalletCreateDTO;
import com.example.E_Wallet.DTO.WalletUpdateDTO;
import com.example.E_Wallet.Exceptions.DuplicateResourceException;
import com.example.E_Wallet.Exceptions.ResourceNotFoundException;
import com.example.E_Wallet.Exceptions.ValidationException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

        // Validate account number is unique
        if (walletRepo.existsByAccountNumber(walletCreateDTO.getAccountNumber())) {
            throw new DuplicateResourceException("Account number '" + walletCreateDTO.getAccountNumber() + "' already exists");
        }

        // Validate balance
        if (walletCreateDTO.getBalance() < 0) {
            throw new ValidationException("Balance must be 0 or greater");
        }

        // Validate passcode is 4 digits
        if (walletCreateDTO.getPasscode() == null || !walletCreateDTO.getPasscode().matches("^\\d{4}$")) {
            throw new ValidationException("Passcode must be exactly 4 digits");
        }

        Wallet wallet = convertToEntity(walletCreateDTO, user);
        wallet.setCreatedAt(LocalDateTime.now());
        
        Wallet savedWallet = walletRepo.save(wallet);
        return convertToDTO(savedWallet);
    }

    public WalletDTO updateWallet(WalletUpdateDTO walletUpdateDTO) {
        // Validate wallet exists
        Wallet wallet = walletRepo.findById(walletUpdateDTO.getWalletId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found with id: " + walletUpdateDTO.getWalletId()));

        // Validate user exists
        User user = userRepo.findById(walletUpdateDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + walletUpdateDTO.getUserId()));

        // Validate wallet belongs to user
        if (!wallet.getUser().getId().equals(walletUpdateDTO.getUserId())) {
            throw new ValidationException("Wallet does not belong to the specified user");
        }

        // Update wallet name if provided
        if (walletUpdateDTO.getWalletName() != null && !walletUpdateDTO.getWalletName().trim().isEmpty()) {
            wallet.setWalletName(walletUpdateDTO.getWalletName());
        }

        // Update account number if provided and validate uniqueness
        if (walletUpdateDTO.getAccountNumber() != null && !walletUpdateDTO.getAccountNumber().trim().isEmpty()) {
            if (!walletUpdateDTO.getAccountNumber().equals(wallet.getAccountNumber()) &&
                walletRepo.existsByAccountNumber(walletUpdateDTO.getAccountNumber())) {
                throw new DuplicateResourceException("Account number '" + walletUpdateDTO.getAccountNumber() + "' already exists");
            }
            wallet.setAccountNumber(walletUpdateDTO.getAccountNumber());
        }

        // Update balance if provided
        if (walletUpdateDTO.getBalance() != null) {
            if (walletUpdateDTO.getBalance() < 0) {
                throw new ValidationException("Balance must be 0 or greater");
            }
            wallet.setBalance(walletUpdateDTO.getBalance());
        }

        // Update passcode if provided
        if (walletUpdateDTO.getPasscode() != null && !walletUpdateDTO.getPasscode().trim().isEmpty()) {
            if (!walletUpdateDTO.getPasscode().matches("^\\d{4}$")) {
                throw new ValidationException("Passcode must be exactly 4 digits");
            }
            wallet.setPasscode(walletUpdateDTO.getPasscode());
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
        walletDTO.setUserId(wallet.getUser().getId());
        walletDTO.setWalletName(wallet.getWalletName());
        walletDTO.setCreatedAt(wallet.getCreatedAt());
        return walletDTO;
    }

    private Wallet convertToEntity(WalletCreateDTO walletCreateDTO, User user) {
        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setWalletName(walletCreateDTO.getWalletName());
        wallet.setAccountNumber(walletCreateDTO.getAccountNumber());
        wallet.setBalance(walletCreateDTO.getBalance());
        wallet.setPasscode(walletCreateDTO.getPasscode());
        return wallet;
    }
}

