package com.example.Expense_Tracker.Exception;

public class ValidationException extends RuntimeException {
    
    public ValidationException(String reason) {
        super(reason);
    }
}
