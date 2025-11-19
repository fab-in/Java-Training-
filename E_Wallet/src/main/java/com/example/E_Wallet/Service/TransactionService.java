package com.example.E_Wallet.Service;

import com.example.E_Wallet.DTO.PaginatedResponse;
import com.example.E_Wallet.DTO.TransactionDTO;
import com.example.E_Wallet.Model.Transaction;
import com.example.E_Wallet.Model.User;
import com.example.E_Wallet.Repository.TransactionRepo;
import com.example.E_Wallet.Repository.WalletRepo;
import com.example.E_Wallet.Security.SecurityUtil;
import com.example.E_Wallet.Exceptions.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TransactionService {

    @Autowired
    private TransactionRepo transactionRepo;

    @Autowired
    private WalletRepo walletRepo;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private StatementCsvBuilder statementCsvBuilder;

    @Autowired
    private EmailService emailService;

    
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100; // Maximum allowed page size to prevent abuse

    
    public PaginatedResponse<TransactionDTO> getTransactions(String type, Pageable pageable) {
        Pageable validatedPageable = validateAndAdjustPageable(pageable);

        String normalizedType = (type == null || type.trim().isEmpty()) 
            ? "all" 
            : type.trim().toLowerCase();
        
        Page<Transaction> transactionPage;
        switch (normalizedType) {
            case "credits":
                transactionPage = filterTransactionsByRemark("Credit transaction", validatedPageable);
                break;
                
            case "withdrawals":
                transactionPage = filterTransactionsByRemark("Withdrawal transaction", validatedPageable);
                break;
                
            case "transfers":
                transactionPage = filterTransactionsByRemark("Fund transfer", validatedPageable);
                break;
                
            case "failed":
                transactionPage = filterFailedTransactions(validatedPageable);
                break;
                
            case "all":
            default:
                transactionPage = fetchTransactionsForCurrentUser(validatedPageable);
                break;
        }
        
        return convertToPaginatedResponse(transactionPage);
    }

    private Page<Transaction> filterTransactionsByRemark(String remark, Pageable pageable) {
        User currentUser = securityUtil.getCurrentUser();

        if (currentUser == null) {
            throw new ValidationException("User not authenticated");
        }

        if (securityUtil.isAdmin()) {
            return transactionRepo.findAllByRemark(remark, pageable);
        } else {
            return transactionRepo.findByUserIdAndRemark(currentUser.getId(), remark, pageable);
        }
    }
    
    private Page<Transaction> filterFailedTransactions(Pageable pageable) {
        User currentUser = securityUtil.getCurrentUser();

        if (currentUser == null) {
            throw new ValidationException("User not authenticated");
        }

        if (securityUtil.isAdmin()) {
            return transactionRepo.findAllFailedTransactions(pageable);
        } else {
            return transactionRepo.findFailedTransactionsByUserId(currentUser.getId(), pageable);
        }
    }

    private Page<Transaction> fetchTransactionsForCurrentUser(Pageable pageable) {
        User currentUser = securityUtil.getCurrentUser();

        if (currentUser == null) {
            throw new ValidationException("User not authenticated");
        }

        if (securityUtil.isAdmin()) {
            return transactionRepo.findAllWithDetails(pageable);
        }
        return transactionRepo.findByUserId(currentUser.getId(), pageable);
    }

    private PaginatedResponse<TransactionDTO> convertToPaginatedResponse(Page<Transaction> transactionPage) {
        
        List<TransactionDTO> content = transactionPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

       
        PaginatedResponse<TransactionDTO> response = new PaginatedResponse<>();
        response.setContent(content);
        response.setTotalElements(transactionPage.getTotalElements());
        response.setTotalPages(transactionPage.getTotalPages());
        response.setCurrentPage(transactionPage.getNumber());
        response.setPageSize(transactionPage.getSize());
        response.setHasNext(transactionPage.hasNext());
        response.setHasPrevious(transactionPage.hasPrevious());
        response.setFirst(transactionPage.isFirst());
        response.setLast(transactionPage.isLast());

        return response;
    }

    private Pageable validateAndAdjustPageable(Pageable pageable) {
        int pageSize = pageable.getPageSize();
        int pageNumber = pageable.getPageNumber();

        
        if (pageSize > MAX_PAGE_SIZE) {
            pageSize = MAX_PAGE_SIZE;
        } else if (pageSize < 1) {
            pageSize = DEFAULT_PAGE_SIZE;
        }

        
        if (pageNumber < 0) {
            pageNumber = 0;
        }

        
        return PageRequest.of(pageNumber, pageSize, pageable.getSort());
    }

    private TransactionDTO convertToDTO(Transaction transaction) {
        TransactionDTO dto = new TransactionDTO();
        dto.setId(transaction.getId());
        dto.setSenderWalletId(transaction.getSenderWallet().getId());
        dto.setReceiverWalletId(transaction.getReceiverWallet().getId());
        dto.setAmount(transaction.getAmount());
        dto.setTransactionDate(transaction.getTransactionDate());
        dto.setStatus(transaction.getStatus());
        dto.setRemarks(transaction.getRemarks());
        return dto;
    }

    public void generateAndEmailStatement() throws jakarta.mail.MessagingException {
        User currentUser = securityUtil.getCurrentUser();

        if (currentUser == null) {
            throw new ValidationException("User not authenticated");
        }

        // Fetch transactions and wallets within the transactional method
        List<Transaction> transactions = transactionRepo.findAllByUserId(currentUser.getId());
        List<com.example.E_Wallet.Model.Wallet> userWallets = walletRepo.findByUserId(currentUser.getId());

        byte[] csvBytes = statementCsvBuilder.buildStatementCsv(currentUser, transactions, userWallets);

        emailService.sendStatementEmail(
                currentUser.getEmail(),
                currentUser.getName(),
                csvBytes
        );
    }
}
