package com.example.E_Wallet.Service;

import com.example.E_Wallet.Model.Transaction;
import com.example.E_Wallet.Model.User;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class StatementCsvBuilder {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(Locale.US);

    /**
     * Builds a CSV statement from user and transaction data
     * 
     * @param user The user for whom the statement is generated
     * @param transactions List of transactions to include in the statement
     * @return CSV content as byte array
     */
    public byte[] buildStatementCsv(User user, List<Transaction> transactions) {
        StringBuilder csv = new StringBuilder();

        // Add header information
        csv.append("E-Wallet Transaction Statement\n");
        csv.append("================================\n\n");
        csv.append("User Information:\n");
        csv.append("Name,").append(escapeCsvField(user.getName())).append("\n");
        csv.append("Email,").append(escapeCsvField(user.getEmail())).append("\n");
        csv.append("Phone Number,").append(escapeCsvField(user.getPhoneNumber())).append("\n");
        csv.append("Statement Generated At,").append(java.time.LocalDateTime.now().format(DATE_FORMATTER)).append("\n");
        csv.append("\n");

        // Add transaction summary
        double totalDebits = transactions.stream()
                .filter(t -> t.getSenderWallet().getUser().getId().equals(user.getId()))
                .mapToDouble(Transaction::getAmount)
                .sum();
        
        double totalCredits = transactions.stream()
                .filter(t -> t.getReceiverWallet().getUser().getId().equals(user.getId()))
                .mapToDouble(Transaction::getAmount)
                .sum();

        csv.append("Summary:\n");
        csv.append("Total Transactions,").append(transactions.size()).append("\n");
        csv.append("Total Debits,").append(CURRENCY_FORMATTER.format(totalDebits)).append("\n");
        csv.append("Total Credits,").append(CURRENCY_FORMATTER.format(totalCredits)).append("\n");
        csv.append("\n");

        // Add transaction table header
        csv.append("Transaction Details:\n");
        csv.append("Transaction ID,Date,Type,Amount,Status,Remarks,Sender Wallet,Receiver Wallet\n");

        // Add transaction rows
        for (Transaction transaction : transactions) {
            String transactionId = transaction.getId().toString();
            String date = transaction.getTransactionDate().format(DATE_FORMATTER);
            
            // Determine transaction type from user's perspective
            String type;
            double amount;
            if (transaction.getSenderWallet().getUser().getId().equals(user.getId())) {
                type = "DEBIT";
                amount = -transaction.getAmount(); // Negative for debits
            } else {
                type = "CREDIT";
                amount = transaction.getAmount();
            }
            
            String formattedAmount = CURRENCY_FORMATTER.format(amount);
            String status = transaction.getStatus();
            String remarks = escapeCsvField(transaction.getRemarks());
            String senderWallet = transaction.getSenderWallet().getAccountNumber();
            String receiverWallet = transaction.getReceiverWallet().getAccountNumber();

            csv.append(transactionId).append(",")
               .append(date).append(",")
               .append(type).append(",")
               .append(formattedAmount).append(",")
               .append(status).append(",")
               .append(remarks).append(",")
               .append(senderWallet).append(",")
               .append(receiverWallet).append("\n");
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Escapes CSV fields that contain commas, quotes, or newlines
     */
    private String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }
        
        // If field contains comma, quote, or newline, wrap in quotes and escape internal quotes
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        
        return field;
    }
}

