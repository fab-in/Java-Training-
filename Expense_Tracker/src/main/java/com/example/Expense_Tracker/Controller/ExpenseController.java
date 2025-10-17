package com.example.Expense_Tracker.Controller;

import java.util.*;
import com.example.Expense_Tracker.Model.Expense;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/expenses/category/{category}")
    public List<Expense> getExpenseByCategory(@PathVariable String category) {
        return service.getExpenseByCategory(category);
    }

    @GetMapping("/expenses/title/{title}")
    public List<Expense> getExpenseByTitle(@PathVariable String title) {
        return service.getExpenseByTitle(title);
    }

    @GetMapping("/expenses/date/{date}")
    public List<Expense> getExpenseByDate(@PathVariable String date) {
        return service.getExpenseByDate(date);
    }

    @PostMapping("/expenses")
    public ResponseEntity<String> addExpense(@RequestBody Expense expense) {
        service.addExpense(expense);
        return ResponseEntity.ok("Expense added successfully");
    }

    @PutMapping("/expenses/{id}")
    public ResponseEntity<String> updateExpense(@PathVariable int id, @RequestBody Expense expense) {
        service.updateExpense(id, expense);
        return ResponseEntity.ok("Expense updated successfully");
    }

    @DeleteMapping("/expenses/{id}")
    public ResponseEntity<String> deleteExpense(@PathVariable int id) {
        service.deleteExpense(id);
        return ResponseEntity.ok("Expense deleted successfully");
    }

    @RequestMapping("/expenses/total")
    public double totalExpense() {
        return service.getTotalExpense();
    }

}
