package com.example.E_Wallet.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class WalletService {

    @Autowired
    private WalletRepo walletRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private SecurityUtil securityUtil;

    public List<WalletDTO> getWallets() {
        User currentUser = securityUtil.getCurrentUser();

        if (currentUser == null) {
            throw new ValidationException("User not authenticated");
        }

        List<Wallet> wallets;

        if (securityUtil.isAdmin()) {
            wallets = walletRepo.findAll();
        } else {
            wallets = walletRepo.findByUserId(currentUser.getId());
        }

        return wallets.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public WalletDTO getWalletById(UUID id) {
        Wallet wallet = walletRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found with id: " + id));

        User currentUser = securityUtil.getCurrentUser();

        if (currentUser == null) {
            throw new ValidationException("User not authenticated");
        }

        if (!securityUtil.isAdmin()) {
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

    public WalletDTO updateWallet(WalletUpdateDTO walletUpdateDTO) {
        if (walletUpdateDTO.getWalletName() == null || walletUpdateDTO.getWalletName().trim().isEmpty()) {
            throw new ValidationException("Wallet name is required");
        }

        if (walletUpdateDTO.getUserIdentifier() == null || walletUpdateDTO.getUserIdentifier().trim().isEmpty()) {
            throw new ValidationException("User identifier (name or email) is required");
        }

        Wallet wallet = findWalletByNameAndUser(walletUpdateDTO.getWalletName(), walletUpdateDTO.getUserIdentifier())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Wallet not found with name '" + walletUpdateDTO.getWalletName() + "' for user '"
                                + walletUpdateDTO.getUserIdentifier() + "'"));

        User currentUser = securityUtil.getCurrentUser();

        if (currentUser == null) {
            throw new ValidationException("User not authenticated");
        }

        boolean isAdmin = securityUtil.isAdmin();

        if (!isAdmin) {

            if (!wallet.getUser().getId().equals(currentUser.getId())) {
                throw new ValidationException("Access denied: You can only update your own wallets");
            }
        }

        User user = wallet.getUser(); // Default to current wallet owner
        if (walletUpdateDTO.getNewUserIdentifier() != null && !walletUpdateDTO.getNewUserIdentifier().trim().isEmpty()) {
            if (!isAdmin) {
                throw new ValidationException("You cannot change wallet ownership");
            }
            user = findUserByIdentifier(walletUpdateDTO.getNewUserIdentifier())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "User not found with identifier: " + walletUpdateDTO.getNewUserIdentifier()));
            wallet.setUser(user);
        }

        if (walletUpdateDTO.getNewWalletName() != null && !walletUpdateDTO.getNewWalletName().trim().isEmpty()) {
            wallet.setWalletName(walletUpdateDTO.getNewWalletName());
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

    public void deleteWallet(String walletName, String userIdentifier) {
        if (walletName == null || walletName.trim().isEmpty()) {
            throw new ValidationException("Wallet name cannot be null or empty");
        }

        if (userIdentifier == null || userIdentifier.trim().isEmpty()) {
            throw new ValidationException("User identifier (name or email) cannot be null or empty");
        }

        Wallet wallet = findWalletByNameAndUser(walletName, userIdentifier)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Wallet not found with name '" + walletName + "' for user '" + userIdentifier + "'"));

        User currentUser = securityUtil.getCurrentUser();

        if (currentUser == null) {
            throw new ValidationException("User not authenticated");
        }

        if (!securityUtil.isAdmin()) {
            // Access user relationship to check ownership
            UUID walletOwnerId = wallet.getUser().getId();
            if (!walletOwnerId.equals(currentUser.getId())) {
                throw new ValidationException("Access denied: You can only delete your own wallets");
            }
        }

        walletRepo.delete(wallet);
    }

    private Optional<User> findUserByIdentifier(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            return Optional.empty();
        }

        if (identifier.contains("@")) {
            return userRepo.findByEmail(identifier);
        } else {
            return userRepo.findByName(identifier);
        }
    }

    private Optional<Wallet> findWalletByNameAndUser(String walletName, String userIdentifier) {
        if (walletName == null || walletName.trim().isEmpty() ||
                userIdentifier == null || userIdentifier.trim().isEmpty()) {
            return Optional.empty();
        }

        if (userIdentifier.contains("@")) {
            return walletRepo.findByWalletNameAndUserEmail(walletName, userIdentifier);
        } else {
            return walletRepo.findByWalletNameAndUserName(walletName, userIdentifier);
        }
    }

    private WalletDTO convertToDTO(Wallet wallet) {
        WalletDTO walletDTO = new WalletDTO();
        walletDTO.setId(wallet.getId());
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
