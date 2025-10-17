package com.example.Expense_Tracker.Service;

import java.util.List;
import java.util.ArrayList;
import java.util.*;
import com.example.Expense_Tracker.Model.Expense;
import com.example.Expense_Tracker.Exception.ExpenseNotFoundException;
import com.example.Expense_Tracker.Exception.DuplicateExpenseException;
import com.example.Expense_Tracker.Exception.ValidationException;

import org.springframework.stereotype.Component;

@Component
public class ExpenseService {

    List<Expense> expenses = new ArrayList<>(Arrays.asList(
            new Expense(1, "Uber", 350, "10.10.2025", "Work"),
            new Expense(2, "StarBucks", 500, "10.10.2025", "Personal"),
            new Expense(3, "Food", 250, "11.10.2025", "Personal"),
            new Expense(4, "Cousera", 1700, "12.10.2025", "Work")));

    private void validateExpense(Expense expense) {
        if (expense.getId() <= 0) {
            throw new ValidationException("ID required and must be greater than 0");
        }
        if (expense.getAmount() <= 0) {
            throw new ValidationException("Amount required and must be greater than 0");
        }
        if (expense.getDate() == null || expense.getDate().trim().isEmpty()) {
            throw new ValidationException("Date is required and cannot be empty");
        }
        if (expense.getTitle() == null || expense.getTitle().trim().isEmpty()) {
            throw new ValidationException("Title required and cannot be empty");
        }
        if (expense.getCategory() == null || expense.getCategory().trim().isEmpty()) {
            throw new ValidationException("Category required and cannot be empty");
        }
    }

    public List<Expense> getExpenses() {
        return expenses;
    }

    public List<Expense> getExpenseById(int id) {
        List<Expense> result = expenses.stream().filter(e -> e.getId() == id).toList();
        if (result.isEmpty()) {
            throw new ExpenseNotFoundException(id);
        }
        return result;
    }

    public List<Expense> getExpenseByTitle(String title) {
        List<Expense> result = expenses.stream().filter(e -> e.getTitle().equalsIgnoreCase(title)).toList();
        if (result.isEmpty()) {
            throw new ExpenseNotFoundException("Expense with title '" + title + "' not found");
        }
        return result;
    }

    public List<Expense> getExpenseByDate(String date) {
        List<Expense> result = expenses.stream().filter(e -> e.getDate().equalsIgnoreCase(date)).toList();
        if (result.isEmpty()) {
            throw new ExpenseNotFoundException("Expense with date '" + date + "' not found");
        }
        return result;
    }

    public List<Expense> getExpenseByCategory(String category) {
        List<Expense> result = expenses.stream().filter(e -> e.getCategory().equalsIgnoreCase(category)).toList();
        if (result.isEmpty()) {
            throw new ExpenseNotFoundException("Expense with category '" + category + "' not found");
        }
        return result;
    }

    public void addExpense(Expense expense) {
        validateExpense(expense);

        boolean idExists = expenses.stream().anyMatch(e -> e.getId() == expense.getId());

        if (idExists) {
            throw new DuplicateExpenseException("Expense with ID " + expense.getId() + " already exists");
        }

        expenses.add(expense);
    }

    public void updateExpense(int id, Expense expense) {
        validateExpense(expense);

        boolean found = false;
        for (int i = 0; i < expenses.size(); i++) {
            Expense e = expenses.get(i);
            if (e.getId() == id) {
                expenses.set(i, expense);
                found = true;
                break;
            }
        }

        if (!found) {
            throw new ExpenseNotFoundException(id);
        }
    }

    public void deleteExpense(int id) {
        boolean removed = expenses.removeIf(e -> e.getId() == id);
        if (!removed) {
            throw new ExpenseNotFoundException(id);
        }
    }

    public double getTotalExpense() {
        return expenses.stream().mapToDouble(Expense::getAmount).sum();
    }

}
