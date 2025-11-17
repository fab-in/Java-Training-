package com.example.E_Wallet.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import com.example.E_Wallet.Repository.WalletRepo;
import com.example.E_Wallet.Repository.UserRepo;
import com.example.E_Wallet.Repository.TransactionRepo;
import com.example.E_Wallet.Model.Wallet;
import com.example.E_Wallet.Model.User;
import com.example.E_Wallet.Model.Transaction;
import com.example.E_Wallet.DTO.WalletDTO;
import com.example.E_Wallet.DTO.WalletCreateDTO;
import com.example.E_Wallet.DTO.WalletUpdateDTO;
import com.example.E_Wallet.DTO.CreditRequestDTO;
import com.example.E_Wallet.DTO.WithdrawalRequestDTO;
import com.example.E_Wallet.DTO.TransferRequestDTO;
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
    private TransactionRepo transactionRepo;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private PlatformTransactionManager transactionManager;

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
        if (walletUpdateDTO.getNewUserIdentifier() != null
                && !walletUpdateDTO.getNewUserIdentifier().trim().isEmpty()) {
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

    public String creditWallet(CreditRequestDTO creditRequestDTO) {
        Wallet wallet = null;
        String failureReason = null;

        try {
            if (creditRequestDTO.getAmount() == null || creditRequestDTO.getAmount() <= 0) {
                failureReason = "Invalid amount";
                throw new ValidationException("Amount must be greater than 0");
            }

            wallet = walletRepo.findById(creditRequestDTO.getWalletId())
                    .orElseThrow(() -> {
                        createFailedTransaction(creditRequestDTO.getWalletId(), creditRequestDTO.getWalletId(),
                                creditRequestDTO.getAmount(), "Invalid account number");
                        return new ResourceNotFoundException(
                                "Wallet not found with id: " + creditRequestDTO.getWalletId());
                    });

            User currentUser = securityUtil.getCurrentUser();
            if (currentUser == null) {
                failureReason = "User not authenticated";
                throw new ValidationException("User not authenticated");
            }

            // Only wallet owners can credit their wallets, admins cannot credit wallets
            // that don't belong to them
            if (!wallet.getUser().getId().equals(currentUser.getId())) {
                failureReason = "Invalid details";
                createFailedTransaction(creditRequestDTO.getWalletId(), creditRequestDTO.getWalletId(),
                        creditRequestDTO.getAmount(), "Invalid details");
                throw new ValidationException("Access denied: You can only credit your own wallets");
            }

            if (!wallet.getPasscode().equals(creditRequestDTO.getPasscode())) {
                failureReason = "Wrong passcode";
                createFailedTransaction(creditRequestDTO.getWalletId(), creditRequestDTO.getWalletId(),
                        creditRequestDTO.getAmount(), "Wrong passcode");
                throw new ValidationException("Invalid passcode");
            }

            wallet.setBalance(wallet.getBalance() + creditRequestDTO.getAmount());
            walletRepo.save(wallet);
            createSuccessfulTransaction(creditRequestDTO.getWalletId(), creditRequestDTO.getWalletId(),
                    creditRequestDTO.getAmount(), "Credit transaction");
            return "Transaction successful, account has been credited successfully";
        } catch (ValidationException | ResourceNotFoundException e) {
            if (failureReason == null && wallet == null) {
                // Wallet not found case already handled
                throw e;
            }
            if (failureReason == null) {
                failureReason = "Invalid details";
                createFailedTransaction(creditRequestDTO.getWalletId(), creditRequestDTO.getWalletId(),
                        creditRequestDTO.getAmount(), "Invalid details");
            }
            throw e;
        }
    }

    public String withdrawWallet(WithdrawalRequestDTO withdrawalRequestDTO) {
        Wallet wallet = null;
        String failureReason = null;

        try {
            if (withdrawalRequestDTO.getAmount() == null || withdrawalRequestDTO.getAmount() <= 0) {
                failureReason = "Invalid amount";
                throw new ValidationException("Amount must be greater than 0");
            }

            wallet = walletRepo.findById(withdrawalRequestDTO.getWalletId())
                    .orElseThrow(() -> {
                        createFailedTransaction(withdrawalRequestDTO.getWalletId(), withdrawalRequestDTO.getWalletId(),
                                withdrawalRequestDTO.getAmount(), "Invalid account number");
                        return new ResourceNotFoundException(
                                "Wallet not found with id: " + withdrawalRequestDTO.getWalletId());
                    });

            User currentUser = securityUtil.getCurrentUser();
            if (currentUser == null) {
                failureReason = "User not authenticated";
                throw new ValidationException("User not authenticated");
            }

            // Only wallet owners can withdraw from their wallets, admins cannot withdraw
            // from wallets that don't belong to them
            if (!wallet.getUser().getId().equals(currentUser.getId())) {
                failureReason = "Invalid details";
                createFailedTransaction(withdrawalRequestDTO.getWalletId(), withdrawalRequestDTO.getWalletId(),
                        withdrawalRequestDTO.getAmount(), "Invalid details");
                throw new ValidationException("Access denied: You can only withdraw from your own wallets");
            }

            if (!wallet.getPasscode().equals(withdrawalRequestDTO.getPasscode())) {
                failureReason = "Wrong passcode";
                createFailedTransaction(withdrawalRequestDTO.getWalletId(), withdrawalRequestDTO.getWalletId(),
                        withdrawalRequestDTO.getAmount(), "Wrong passcode");
                throw new ValidationException("Invalid passcode");
            }

            if (wallet.getBalance() < withdrawalRequestDTO.getAmount()) {
                failureReason = "Insufficient balance";
                createFailedTransaction(withdrawalRequestDTO.getWalletId(), withdrawalRequestDTO.getWalletId(),
                        withdrawalRequestDTO.getAmount(), "Insufficient balance");
                throw new ValidationException("Insufficient balance. Available balance: " + wallet.getBalance());
            }

            wallet.setBalance(wallet.getBalance() - withdrawalRequestDTO.getAmount());
            walletRepo.save(wallet);
            createSuccessfulTransaction(withdrawalRequestDTO.getWalletId(), withdrawalRequestDTO.getWalletId(),
                    withdrawalRequestDTO.getAmount(), "Withdrawal transaction");
            return "Amount has been withdrawn successfully";
        } catch (ValidationException | ResourceNotFoundException e) {
            if (failureReason == null && wallet == null) {

                throw e;
            }
            if (failureReason == null) {
                failureReason = "Invalid details";
                createFailedTransaction(withdrawalRequestDTO.getWalletId(), withdrawalRequestDTO.getWalletId(),
                        withdrawalRequestDTO.getAmount(), "Invalid details");
            }
            throw e;
        }
    }

    public String transferFunds(TransferRequestDTO transferRequestDTO) {
        Wallet sourceWallet = null;
        Wallet destinationWallet = null;
        String failureReason = null;

        try {
            if (transferRequestDTO.getAmount() == null || transferRequestDTO.getAmount() <= 0) {
                failureReason = "Invalid amount";
                throw new ValidationException("Amount must be greater than 0");
            }

            if (transferRequestDTO.getSourceWalletId().equals(transferRequestDTO.getDestinationWalletId())) {
                failureReason = "Invalid details";
                throw new ValidationException("Source and destination wallets cannot be the same");
            }

            sourceWallet = walletRepo.findById(transferRequestDTO.getSourceWalletId())
                    .orElseThrow(() -> {
                        createFailedTransaction(transferRequestDTO.getSourceWalletId(),
                                transferRequestDTO.getDestinationWalletId(),
                                transferRequestDTO.getAmount(), "Invalid account number");
                        return new ResourceNotFoundException(
                                "Source wallet not found with id: " + transferRequestDTO.getSourceWalletId());
                    });

            destinationWallet = walletRepo.findById(transferRequestDTO.getDestinationWalletId())
                    .orElseThrow(() -> {
                        createFailedTransaction(transferRequestDTO.getSourceWalletId(),
                                transferRequestDTO.getDestinationWalletId(),
                                transferRequestDTO.getAmount(), "Invalid account number");
                        return new ResourceNotFoundException(
                                "Destination wallet not found with id: " + transferRequestDTO.getDestinationWalletId());
                    });

            User currentUser = securityUtil.getCurrentUser();
            if (currentUser == null) {
                failureReason = "User not authenticated";
                throw new ValidationException("User not authenticated");
            }

            if (!sourceWallet.getUser().getId().equals(currentUser.getId())) {
                failureReason = "Invalid details";
                createFailedTransaction(transferRequestDTO.getSourceWalletId(),
                        transferRequestDTO.getDestinationWalletId(),
                        transferRequestDTO.getAmount(), "Invalid details");
                throw new ValidationException("Access denied: You can only transfer from your own wallets");
            }

            if (!sourceWallet.getPasscode().equals(transferRequestDTO.getPasscode())) {
                failureReason = "Wrong passcode";
                createFailedTransaction(transferRequestDTO.getSourceWalletId(),
                        transferRequestDTO.getDestinationWalletId(),
                        transferRequestDTO.getAmount(), "Wrong passcode");
                throw new ValidationException("Invalid passcode");
            }

            if (sourceWallet.getBalance() < transferRequestDTO.getAmount()) {
                failureReason = "Insufficient balance";
                createFailedTransaction(transferRequestDTO.getSourceWalletId(),
                        transferRequestDTO.getDestinationWalletId(),
                        transferRequestDTO.getAmount(), "Insufficient balance");
                throw new ValidationException("Insufficient balance. Available balance: " + sourceWallet.getBalance());
            }

            sourceWallet.setBalance(sourceWallet.getBalance() - transferRequestDTO.getAmount());
            destinationWallet.setBalance(destinationWallet.getBalance() + transferRequestDTO.getAmount());

            walletRepo.save(sourceWallet);
            walletRepo.save(destinationWallet);
            createSuccessfulTransaction(transferRequestDTO.getSourceWalletId(),
                    transferRequestDTO.getDestinationWalletId(),
                    transferRequestDTO.getAmount(),
                    "Fund transfer");
            return "Transaction successful, funds have been transferred successfully";
        } catch (ValidationException | ResourceNotFoundException e) {
            if (failureReason == null && (sourceWallet == null || destinationWallet == null)) {

                throw e;
            }
            if (failureReason == null) {
                failureReason = "Invalid details";
                createFailedTransaction(transferRequestDTO.getSourceWalletId(),
                        transferRequestDTO.getDestinationWalletId(),
                        transferRequestDTO.getAmount(), "Invalid details");
            }
            throw e;
        }
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

    private void createSuccessfulTransaction(UUID senderWalletId, UUID receiverWalletId, Double amount,
            String remarks) {
        try {

            Optional<Wallet> senderWalletOpt = walletRepo.findById(senderWalletId);
            Optional<Wallet> receiverWalletOpt = walletRepo.findById(receiverWalletId);

            if (senderWalletOpt.isPresent() && receiverWalletOpt.isPresent()) {
                Wallet senderWallet = senderWalletOpt.get();
                Wallet receiverWallet = receiverWalletOpt.get();

                Transaction transaction = new Transaction();
                transaction.setSenderWallet(senderWallet);
                transaction.setReceiverWallet(receiverWallet);
                transaction.setAmount(amount != null ? amount : 0.0);
                transaction.setTransactionDate(LocalDateTime.now());
                transaction.setStatus("success");
                transaction.setRemarks(remarks != null ? remarks : "Transaction completed");

                transactionRepo.save(transaction);
            }
        } catch (Exception e) {

            System.err.println("Failed to create successful transaction record: " + e.getMessage());
        }
    }

    private void createFailedTransaction(UUID senderWalletId, UUID receiverWalletId, Double amount, String remarks) {

        TransactionTemplate newTransactionTemplate = new TransactionTemplate(transactionManager);
        newTransactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        newTransactionTemplate.executeWithoutResult(status -> {
            try {

                Optional<Wallet> senderWalletOpt = walletRepo.findById(senderWalletId);
                Optional<Wallet> receiverWalletOpt = walletRepo.findById(receiverWalletId);

                if (senderWalletOpt.isPresent()) {
                    Wallet senderWallet = senderWalletOpt.get();
                    Wallet receiverWallet = receiverWalletOpt.orElse(senderWallet); // Use sender as receiver if
                                                                                    // receiver doesn't exist

                    Transaction transaction = new Transaction();
                    transaction.setSenderWallet(senderWallet);
                    transaction.setReceiverWallet(receiverWallet);
                    transaction.setAmount(amount != null ? amount : 0.0);
                    transaction.setTransactionDate(LocalDateTime.now());
                    transaction.setStatus("failed");
                    transaction.setRemarks(remarks);

                    transactionRepo.save(transaction);
                    transactionRepo.flush(); // Force immediate commit
                }
            } catch (Exception e) {

                System.err.println("Failed to create transaction record: " + e.getMessage());
            }
        });
    }
}
