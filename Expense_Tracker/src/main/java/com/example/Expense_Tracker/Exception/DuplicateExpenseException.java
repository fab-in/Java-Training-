package com.example.Expense_Tracker.Exception;

public class DuplicateExpenseException extends RuntimeException {
    
    public DuplicateExpenseException(String message) {
        super(message);
    }
}
