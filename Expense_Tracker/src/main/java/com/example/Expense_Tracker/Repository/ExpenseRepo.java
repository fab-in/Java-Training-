package com.example.Expense_Tracker.Repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.Expense_Tracker.Model.Expense;

@Repository
public interface ExpenseRepo extends JpaRepository<Expense, Integer> {
    
    List<Expense> findByTitleIgnoreCase(String title);
    List<Expense> findByDate(String date);
    List<Expense> findByCategoryIgnoreCase(String category);
}
