package com.example.Expense_Tracker.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "expenses")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Expense {

    @Id
    private int id;
    
    @Column(name = "title")
    private String title;
    
    @Column(name = "amount")
    private double amount;
    
    @Column(name = "date")
    private String date;
    
    @Column(name = "category")
    private String category;

}
