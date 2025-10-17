package com.example.Expense_Tracker.Model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Expense {

    private int id;
    private String Title;
    private double amount;
    private String date;
    private String category;

}
