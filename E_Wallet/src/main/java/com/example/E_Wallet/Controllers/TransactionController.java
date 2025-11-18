package com.example.E_Wallet.Controllers;

import com.example.E_Wallet.DTO.PaginatedResponse;
import com.example.E_Wallet.DTO.TransactionDTO;
import com.example.E_Wallet.Service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    // Default pagination values
    private static final int DEFAULT_PAGE = 0;  // First page (0-indexed)
    private static final int DEFAULT_SIZE = 20; // 20 items per page

    @GetMapping("/transactions")
    public ResponseEntity<PaginatedResponse<TransactionDTO>> getTransactions(
            @RequestParam(name = "type", required = false, defaultValue = "all") String type,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", required = false) String sort,
            @RequestParam(name = "order", required = false) String order) {
        
        
        Pageable pageable = createPageable(page, size, sort, order);
        
        
        PaginatedResponse<TransactionDTO> response = transactionService.getTransactions(type, pageable);
        return ResponseEntity.ok(response);
    }

    private Pageable createPageable(int page, int size, String sort, String order) {
        
        if (page < 0) {
            page = DEFAULT_PAGE;
        }
        
        
        if (size < 1) {
            size = DEFAULT_SIZE;
        }
        
        
        if (order != null && !order.trim().isEmpty()) {
            Sort.Direction sortDirection = "oldest".equalsIgnoreCase(order.trim())
                ? Sort.Direction.ASC
                : Sort.Direction.DESC; // "newest" or default = DESC
            
            Sort sortObj = Sort.by(sortDirection, "transactionDate");
            return PageRequest.of(page, size, sortObj);
        }
        
        
        if (sort != null && !sort.trim().isEmpty()) {
            
            String[] sortParts = sort.split(",");
            if (sortParts.length == 2) {
                String field = sortParts[0].trim();
                String direction = sortParts[1].trim().toLowerCase();
                
                
                Sort.Direction sortDirection = "desc".equals(direction) 
                    ? Sort.Direction.DESC 
                    : Sort.Direction.ASC;
                Sort sortObj = Sort.by(sortDirection, field);
                
                
                return PageRequest.of(page, size, sortObj);
            }
        }
        return PageRequest.of(page, size);
    }
}

