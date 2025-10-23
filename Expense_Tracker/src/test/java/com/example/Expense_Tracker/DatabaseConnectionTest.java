package com.example.Expense_Tracker;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.properties")
public class DatabaseConnectionTest {

    @Autowired
    private DataSource dataSource;

    @Test
    public void testDatabaseConnection() {
        try (Connection connection = dataSource.getConnection()) {
            assertNotNull(connection, "Database connection should not be null");
            assertFalse(connection.isClosed(), "Database connection should be open");
            
            // Test if we can execute a simple query
            var statement = connection.createStatement();
            var resultSet = statement.executeQuery("SELECT 1");
            assertTrue(resultSet.next(), "Should be able to execute a simple query");
            
            System.out.println("✅ MySQL connection test PASSED!");
            System.out.println("Database URL: " + connection.getMetaData().getURL());
            System.out.println("Database Product: " + connection.getMetaData().getDatabaseProductName());
            System.out.println("Database Version: " + connection.getMetaData().getDatabaseProductVersion());
            
        } catch (SQLException e) {
            fail("Database connection failed: " + e.getMessage());
        }
    }

    @Test
    public void testDatabaseExists() {
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.createStatement();
            var resultSet = statement.executeQuery("SHOW DATABASES LIKE 'expense_tracker_db'");
            
            if (resultSet.next()) {
                System.out.println("✅ Database 'expense_tracker_db' exists!");
            } else {
                System.out.println("❌ Database 'expense_tracker_db' does not exist!");
                System.out.println("You need to create the database first.");
            }
            
        } catch (SQLException e) {
            System.err.println("Error checking database existence: " + e.getMessage());
        }
    }
}
