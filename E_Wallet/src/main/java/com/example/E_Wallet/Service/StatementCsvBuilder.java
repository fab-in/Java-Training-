package com.example.E_Wallet.Service;

import com.example.E_Wallet.Model.Transaction;
import com.example.E_Wallet.Model.User;
import com.example.E_Wallet.Model.Wallet;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class StatementCsvBuilder {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public byte[] buildStatementCsv(User user, List<Transaction> transactions, List<Wallet> userWallets) {
        StringBuilder csv = new StringBuilder();

        csv.append("Account Holder,").append(escapeCsvField(user != null ? user.getName() : "")).append("\n");
        csv.append("Account Details,").append(escapeCsvField(buildAccountDetails(userWallets))).append("\n\n");

        // CSV Header - matching table structure provided
        csv.append("Transaction ID,Date,Amount,Status,Remarks,Sender Wallet ID,Receiver Wallet ID\n");

        if (transactions.isEmpty()) {
            csv.append("\nFinal Balance,").append(formatAmount(calculateTotalBalance(userWallets))).append("\n");
            return csv.toString().getBytes(StandardCharsets.UTF_8);
        }

        // Sort transactions chronologically (newest first for readability)
        List<Transaction> sortedTransactions = new ArrayList<>(transactions);
        sortedTransactions.sort(Comparator.comparing(Transaction::getTransactionDate));
        for (int i = sortedTransactions.size() - 1; i >= 0; i--) {
            Transaction transaction = sortedTransactions.get(i);
            String statusValue = normalizeStatus(transaction.getStatus());
            boolean isSuccessful = "SUCCESS".equals(statusValue);

            csv.append(valueOrEmpty(transaction != null ? transaction.getId() : null)).append(",")
               .append(formatDate(transaction != null ? transaction.getTransactionDate() : null)).append(",")
               .append(isSuccessful ? formatAmount(transaction.getAmount()) : "")
               .append(",")
               .append(statusValue).append(",")
               .append(escapeCsvField(transaction != null ? transaction.getRemarks() : "")).append(",")
               .append(safeWalletReference(transaction != null ? transaction.getSenderWallet() : null)).append(",")
               .append(safeWalletReference(transaction != null ? transaction.getReceiverWallet() : null))
               .append("\n");
        }

        csv.append("\nFinal Balance,").append(formatAmount(calculateTotalBalance(userWallets))).append("\n");
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String formatDate(java.time.LocalDateTime dateTime) {
        return dateTime == null ? "" : DATE_FORMATTER.format(dateTime);
    }

    private String formatAmount(double amount) {
        return String.format(Locale.US, "%.2f", amount);
    }

    private String buildAccountDetails(List<Wallet> wallets) {
        if (wallets == null || wallets.isEmpty()) {
            return "";
        }
        return wallets.stream()
                .map(wallet -> {
                    if (wallet == null) {
                        return "";
                    }
                    StringBuilder builder = new StringBuilder();
                    if (wallet.getWalletName() != null && !wallet.getWalletName().isBlank()) {
                        builder.append(wallet.getWalletName()).append(" ");
                    }
                    if (wallet.getAccountNumber() != null && !wallet.getAccountNumber().isBlank()) {
                        builder.append("(").append(wallet.getAccountNumber()).append(")");
                    }
                    return builder.toString().trim();
                })
                .filter(entry -> !entry.isEmpty())
                .collect(Collectors.joining(" | "));
    }

    private String safeWalletReference(Wallet wallet) {
        if (wallet == null) {
            return "";
        }
        UUID walletId = wallet.getId();
        return walletId != null ? walletId.toString() : "";
    }

    private String valueOrEmpty(UUID value) {
        return value != null ? value.toString() : "";
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "";
        }
        String upper = status.trim().toUpperCase(Locale.US);
        if (upper.contains("SUCCESS")) {
            return "SUCCESS";
        }
        if (upper.contains("FAIL")) {
            return "FAILED";
        }
        return upper;
    }

    private double calculateTotalBalance(List<Wallet> wallets) {
        if (wallets == null) {
            return 0.0;
        }
        return wallets.stream()
                .filter(wallet -> wallet != null)
                .mapToDouble(Wallet::getBalance)
                .sum();
    }

    private String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }
}

