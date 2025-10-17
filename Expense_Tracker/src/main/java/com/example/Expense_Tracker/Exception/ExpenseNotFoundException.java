package com.example.Expense_Tracker.Exception;

public class ExpenseNotFoundException extends RuntimeException {
    
    public ExpenseNotFoundException(String message) {
        super(message);
    }
    
    public ExpenseNotFoundException(int id) {
        super("Expense with ID " + id + " not found");
    }
}
