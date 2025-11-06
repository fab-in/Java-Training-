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
import com.example.E_Wallet.Security.SecurityUtil;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Wallet Service with Role-Based Access Control (RBAC)
 * 
 * WHAT IT DOES:
 * This service handles all wallet operations (CRUD) with role-based access control.
 * 
 * ACCESS RULES:
 * - ADMIN users: Can access ALL wallets (view, update, delete any wallet)
 * - Regular USER: Can only access THEIR OWN wallets (filtered by userId)
 * 
 * WHY THIS APPROACH:
 * 1. Security: Prevents users from accessing other users' data
 * 2. Data Privacy: Each user only sees their own financial information
 * 3. Admin Oversight: Admins can manage all wallets for support/maintenance
 * 4. Principle of Least Privilege: Users get minimum access needed
 */
@Service
public class WalletService {

    @Autowired
    private WalletRepo walletRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private SecurityUtil securityUtil;

    /**
     * Gets all wallets with role-based filtering.
     * 
     * ACCESS LOGIC:
     * - If current user is ADMIN: Returns ALL wallets in the system
     * - If current user is regular USER: Returns ONLY wallets belonging to that user
     * 
     * WHY:
     * - Admins need to see all wallets for management/support
     * - Regular users should only see their own wallets for privacy
     * 
     * HOW IT WORKS:
     * 1. Gets the currently authenticated user from SecurityUtil
     * 2. Checks if user has ADMIN role
     * 3. If admin: queries all wallets (walletRepo.findAll())
     * 4. If regular user: queries only their wallets (walletRepo.findByUser_Id())
     * 5. Converts to DTOs and returns
     */
    public List<WalletDTO> getWallets() {
        User currentUser = securityUtil.getCurrentUser();
        
        if (currentUser == null) {
            throw new ValidationException("User not authenticated");
        }
        
        List<Wallet> wallets;
        
        // ROLE-BASED FILTERING:
        // Admin can see all wallets, regular users only see their own
        if (securityUtil.isAdmin()) {
            // ADMIN: Get all wallets
            wallets = walletRepo.findAll();
        } else {
            // REGULAR USER: Get only their wallets
            wallets = walletRepo.findByUser_Id(currentUser.getId());
        }
        
        return wallets.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Gets a specific wallet by ID with ownership verification.
     * 
     * ACCESS LOGIC:
     * - If current user is ADMIN: Can access ANY wallet
     * - If current user is regular USER: Can only access wallets they own
     * 
     * WHY:
     * - Prevents users from accessing other users' wallets by guessing IDs
     * - Admins need access to any wallet for support purposes
     * 
     * HOW IT WORKS:
     * 1. Finds the wallet by ID
     * 2. Gets the currently authenticated user
     * 3. If user is NOT admin AND wallet doesn't belong to user: throws exception
     * 4. Otherwise: returns the wallet DTO
     */
    public WalletDTO getWalletById(Long id) {
        Wallet wallet = walletRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found with id: " + id));
        
        User currentUser = securityUtil.getCurrentUser();
        
        if (currentUser == null) {
            throw new ValidationException("User not authenticated");
        }
        
        // OWNERSHIP CHECK:
        // Regular users can only access their own wallets
        // Admins can access any wallet
        if (!securityUtil.isAdmin()) {
            // Check if wallet belongs to current user
            if (!wallet.getUser().getId().equals(currentUser.getId())) {
                throw new ValidationException("Access denied: You can only access your own wallets");
            }
        }
        
        return convertToDTO(wallet);
    }

    public WalletDTO createWallet(WalletCreateDTO walletCreateDTO) {

        User user = userRepo.findById(walletCreateDTO.getUserId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("User not found with id: " + walletCreateDTO.getUserId()));

        if (walletRepo.existsByAccountNumber(walletCreateDTO.getAccountNumber())) {
            throw new DuplicateResourceException(
                    "Account number '" + walletCreateDTO.getAccountNumber() + "' already exists");
        }

        if (walletCreateDTO.getBalance() < 0) {
            throw new ValidationException("Balance must be 0 or greater");
        }

        if (walletCreateDTO.getPasscode() == null || !walletCreateDTO.getPasscode().matches("^\\d{4}$")) {
            throw new ValidationException("Passcode must be exactly 4 digits");
        }

        Wallet wallet = convertToEntity(walletCreateDTO, user);
        wallet.setCreatedAt(LocalDateTime.now());

        Wallet savedWallet = walletRepo.save(wallet);
        return convertToDTO(savedWallet);
    }

    /**
     * Updates a wallet with role-based access control.
     * 
     * ACCESS LOGIC:
     * - If current user is ADMIN: Can update ANY wallet
     * - If current user is regular USER: Can only update their own wallets
     * 
     * WHY:
     * - Prevents users from modifying other users' wallets
     * - Admins may need to update wallets for support/maintenance
     * 
     * HOW IT WORKS:
     * 1. Finds the wallet to update
     * 2. Gets the currently authenticated user
     * 3. If user is NOT admin: verifies wallet belongs to them
     * 4. If admin: can update any wallet (but still validates userId exists)
     * 5. Updates wallet fields and saves
     */
    public WalletDTO updateWallet(WalletUpdateDTO walletUpdateDTO) {

        Wallet wallet = walletRepo.findById(walletUpdateDTO.getWalletId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Wallet not found with id: " + walletUpdateDTO.getWalletId()));

        User currentUser = securityUtil.getCurrentUser();
        
        if (currentUser == null) {
            throw new ValidationException("User not authenticated");
        }

        // OWNERSHIP CHECK:
        // Regular users can only update their own wallets
        // Admins can update any wallet
        if (!securityUtil.isAdmin()) {
            // Regular user: verify wallet belongs to them
            if (!wallet.getUser().getId().equals(currentUser.getId())) {
                throw new ValidationException("Access denied: You can only update your own wallets");
            }
            
            // Also verify the userId in DTO matches current user (prevent changing ownership)
            if (!walletUpdateDTO.getUserId().equals(currentUser.getId())) {
                throw new ValidationException("You cannot change wallet ownership");
            }
        }

        // Load the user from DTO (for admin updates or validation)
        User user = userRepo.findById(walletUpdateDTO.getUserId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("User not found with id: " + walletUpdateDTO.getUserId()));

        // Additional validation: wallet must belong to the user specified in DTO
        // (This prevents admins from accidentally assigning wallets to wrong users)
        if (!wallet.getUser().getId().equals(walletUpdateDTO.getUserId())) {
            throw new ValidationException("Wallet does not belong to the specified user");
        }

        if (walletUpdateDTO.getWalletName() != null && !walletUpdateDTO.getWalletName().trim().isEmpty()) {
            wallet.setWalletName(walletUpdateDTO.getWalletName());
        }

        if (walletUpdateDTO.getAccountNumber() != null && !walletUpdateDTO.getAccountNumber().trim().isEmpty()) {
            if (!walletUpdateDTO.getAccountNumber().equals(wallet.getAccountNumber()) &&
                    walletRepo.existsByAccountNumber(walletUpdateDTO.getAccountNumber())) {
                throw new DuplicateResourceException(
                        "Account number '" + walletUpdateDTO.getAccountNumber() + "' already exists");
            }
            wallet.setAccountNumber(walletUpdateDTO.getAccountNumber());
        }

        if (walletUpdateDTO.getBalance() != null) {
            if (walletUpdateDTO.getBalance() < 0) {
                throw new ValidationException("Balance must be 0 or greater");
            }
            wallet.setBalance(walletUpdateDTO.getBalance());
        }

        if (walletUpdateDTO.getPasscode() != null && !walletUpdateDTO.getPasscode().trim().isEmpty()) {
            if (!walletUpdateDTO.getPasscode().matches("^\\d{4}$")) {
                throw new ValidationException("Passcode must be exactly 4 digits");
            }
            wallet.setPasscode(walletUpdateDTO.getPasscode());
        }

        Wallet updatedWallet = walletRepo.save(wallet);
        return convertToDTO(updatedWallet);
    }

    /**
     * Deletes a wallet with role-based access control.
     * 
     * ACCESS LOGIC:
     * - If current user is ADMIN: Can delete ANY wallet
     * - If current user is regular USER: Can only delete their own wallets
     * 
     * WHY:
     * - Prevents users from deleting other users' wallets
     * - Admins may need to delete wallets for account management
     * 
     * HOW IT WORKS:
     * 1. Finds the wallet to delete
     * 2. Gets the currently authenticated user
     * 3. If user is NOT admin: verifies wallet belongs to them
     * 4. If authorized: deletes the wallet
     */
    public void deleteWallet(Long id) {
        Wallet wallet = walletRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found with id: " + id));
        
        User currentUser = securityUtil.getCurrentUser();
        
        if (currentUser == null) {
            throw new ValidationException("User not authenticated");
        }
        
        // OWNERSHIP CHECK:
        // Regular users can only delete their own wallets
        // Admins can delete any wallet
        if (!securityUtil.isAdmin()) {
            // Check if wallet belongs to current user
            if (!wallet.getUser().getId().equals(currentUser.getId())) {
                throw new ValidationException("Access denied: You can only delete your own wallets");
            }
        }
        
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
