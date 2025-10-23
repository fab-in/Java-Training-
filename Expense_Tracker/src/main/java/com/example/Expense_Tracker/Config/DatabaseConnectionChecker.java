package com.example.Expense_Tracker.Config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
public class DatabaseConnectionChecker implements CommandLineRunner {

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🔍 Checking MySQL database connection...");
        
        try (Connection connection = dataSource.getConnection()) {
            System.out.println("✅ MySQL connection successful!");
            System.out.println("📊 Database URL: " + connection.getMetaData().getURL());
            System.out.println("📊 Database Product: " + connection.getMetaData().getDatabaseProductName());
            System.out.println("📊 Database Version: " + connection.getMetaData().getDatabaseProductVersion());
            System.out.println("📊 Driver Name: " + connection.getMetaData().getDriverName());
            System.out.println("📊 Driver Version: " + connection.getMetaData().getDriverVersion());
            
            // Check if the specific database exists
            var statement = connection.createStatement();
            var resultSet = statement.executeQuery("SHOW DATABASES LIKE 'expense_tracker_db'");
            
            if (resultSet.next()) {
                System.out.println("✅ Database 'expense_tracker_db' exists!");
                
                // Check if the expenses table exists
                var tableCheck = connection.createStatement();
                var tableResult = tableCheck.executeQuery("SHOW TABLES LIKE 'expenses'");
                
                if (tableResult.next()) {
                    System.out.println("✅ Table 'expenses' exists!");
                } else {
                    System.out.println("⚠️  Table 'expenses' does not exist. It will be created automatically by JPA.");
                }
                
            } else {
                System.out.println("❌ Database 'expense_tracker_db' does not exist!");
                System.out.println("💡 Please create the database using: CREATE DATABASE expense_tracker_db;");
            }
            
        } catch (SQLException e) {
            System.err.println("❌ MySQL connection failed!");
            System.err.println("Error: " + e.getMessage());
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("SQL State: " + e.getSQLState());
            
            // Provide helpful suggestions
            if (e.getMessage().contains("Access denied")) {
                System.err.println("💡 Check your username and password in application.properties");
            } else if (e.getMessage().contains("Communications link failure")) {
                System.err.println("💡 Make sure MySQL server is running on localhost:3306");
            } else if (e.getMessage().contains("Unknown database")) {
                System.err.println("💡 Create the database: CREATE DATABASE expense_tracker_db;");
            }
        }
        
        System.out.println("🔍 Database connection check completed.\n");
    }
}
