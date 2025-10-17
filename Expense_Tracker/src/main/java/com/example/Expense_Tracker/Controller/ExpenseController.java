package com.example.Expense_Tracker.Controller;

import java.util.*;
import com.example.Expense_Tracker.Model.Expense;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import com.example.Expense_Tracker.Service.ExpenseService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
public class ExpenseController {

    @Autowired
    ExpenseService service;

    @GetMapping("/expenses")
    public List<Expense> getExpenses() {
        return service.getExpenses();
    }

    @GetMapping("/expenses/{id}")
    public List<Expense> getExpenseById(@PathVariable int id) {
        return service.getExpenseById(id);
    }

    @PostMapping("/expenses")
    public void addExpense(@RequestBody Expense expense) {
        service.addExpense(expense);
    }

    @PutMapping("/expenses/{id}")
    public void updateExpense(@PathVariable int id, @RequestBody Expense expense) {
        service.updateExpense(id, expense);
    }

    @DeleteMapping("/expenses/{id}")
    public void deleteExpense(@PathVariable int id) {
        service.deleteExpense(id);
    }

    @GetMapping("/expenses/category/{category}")
    public List<Expense> getExpenseByCategory(@PathVariable String category) {
        return service.getExpenseByCategory(category);
    }

    @RequestMapping("/expenses/total")
    public double totalExpense() {
        return service.getTotalExpense();
    }

}
