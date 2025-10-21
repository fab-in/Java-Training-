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
    
    
    private String title;
    
    
    private double amount;
    
    private String date;
    
    
    private String category;

}
