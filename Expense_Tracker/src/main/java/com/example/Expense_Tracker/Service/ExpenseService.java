package com.example.Expense_Tracker.Service;

import java.util.ArrayList;
import java.util.*;
import com.example.Expense_Tracker.Model.Expense;

import org.springframework.stereotype.Component;

@Component
public class ExpenseService {

    List<Expense> expenses = new ArrayList<>();

    public List<Expense> getExpenses() {
        return expenses;
    }

    public List<Expense> getExpenseById(int id) {
        return expenses.stream().filter(e -> e.getId() == id).toList();
    }

    public void addExpense(Expense expense) {
        expenses.add(expense);
    }

    public void updateExpense(int id, Expense expense) {
        for (int i = 0; i < expenses.size(); i++) {
            Expense e = expenses.get(i);
            if (e.getId() == id) {
                expenses.set(i, expense);
                return;
            }
        }
    }

    public void deleteExpense(int id) {
        expenses.removeIf(e -> e.getId() == id);
    }

    public List<Expense> getExpenseByCategory(String category) {
        System.out.println(expenses);
        return expenses.stream().filter(e -> e.getCategory().equalsIgnoreCase(category)).toList();
    }

    public double getTotalExpense() {
        return expenses.stream().mapToDouble(Expense::getAmount).sum();
    }

}
