package com.example.Expense_Tracker.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {
    @RequestMapping("/")
    public String home() {
        return "Welcome to Expense Tracker Application";
    }

    @RequestMapping("/about")
    public String about() {
        return "This is an application to track your expenses.";
    }

}
