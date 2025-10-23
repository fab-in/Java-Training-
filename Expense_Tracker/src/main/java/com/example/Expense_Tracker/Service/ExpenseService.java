package com.example.Expense_Tracker.Service;

import java.util.List;
import com.example.Expense_Tracker.Model.Expense;
import com.example.Expense_Tracker.Exception.ExpenseNotFoundException;
import com.example.Expense_Tracker.Exception.DuplicateExpenseException;
import com.example.Expense_Tracker.Exception.ValidationException;
import com.example.Expense_Tracker.Repository.ExpenseRepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExpenseService {

    @Autowired
    private ExpenseRepo expenseRepo;

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
        return expenseRepo.findAll();
    }

    public List<Expense> getExpenseById(int id) {
        List<Expense> result = expenseRepo.findAll().stream()
                .filter(expense -> expense.getId() == id)
                .toList();
        if (result.isEmpty()) {
            throw new ExpenseNotFoundException(id);
        }
        return result;
    }

    public List<Expense> getExpenseByTitle(String title) {
        List<Expense> result = expenseRepo.findByTitleIgnoreCase(title);
        if (result.isEmpty()) {
            throw new ExpenseNotFoundException("Expense with title '" + title + "' not found");
        }
        return result;
    }

    public List<Expense> getExpenseByDate(String date) {
        List<Expense> result = expenseRepo.findByDate(date);
        if (result.isEmpty()) {
            throw new ExpenseNotFoundException("Expense with date '" + date + "' not found");
        }
        return result;
    }

    public List<Expense> getExpenseByCategory(String category) {
        List<Expense> result = expenseRepo.findByCategoryIgnoreCase(category);
        if (result.isEmpty()) {
            throw new ExpenseNotFoundException("Expense with category '" + category + "' not found");
        }
        return result;
    }

    public void addExpense(Expense expense) {
        validateExpense(expense);

        if (expenseRepo.existsById(expense.getId())) {
            throw new DuplicateExpenseException("Expense with ID " + expense.getId() + " already exists");
        }

        expenseRepo.save(expense);
    }

    public void updateExpense(int id, Expense expense) {
        validateExpense(expense);

        List<Expense> existingExpense = expenseRepo.findAll().stream()
                .filter(e -> e.getId() == id)
                .toList();
        if (existingExpense.isEmpty()) {
            throw new ExpenseNotFoundException(id);
        }

        expenseRepo.save(expense);
    }

    public void deleteExpense(int id) {
        if (!expenseRepo.existsById(id)) {
            throw new ExpenseNotFoundException(id);
        }
        expenseRepo.deleteById(id);
    }

    public double getTotalExpense() {
        return expenseRepo.findAll().stream().mapToDouble(Expense::getAmount).sum();
    }

}
