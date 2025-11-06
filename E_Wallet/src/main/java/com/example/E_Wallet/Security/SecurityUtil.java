package com.example.E_Wallet.Security;

import com.example.E_Wallet.Model.User;
import com.example.E_Wallet.Repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Security Utility Class
 * 
 * WHAT IT DOES:
 * This utility class provides helper methods to easily access the currently
 * authenticated user and their information from Spring Security's SecurityContext.
 * 
 * WHY IT'S NEEDED:
 * 1. Controllers and Services need to know WHO is making the request
 * 2. We need to check user roles (ADMIN vs USER) for authorization
 * 3. We need to get the user's ID to filter data (e.g., users can only see their own wallets)
 * 4. Instead of repeating the same code everywhere, we centralize it here
 * 
 * HOW IT WORKS:
 * - Spring Security stores the authenticated user in SecurityContextHolder
 * - The JwtAuthenticationFilter sets the User object as the principal
 * - This utility extracts that User object and provides convenient methods
 */
@Component
public class SecurityUtil {

    @Autowired
    private UserRepo userRepo;

    /**
     * Gets the currently authenticated user from Spring Security context.
     * 
     * @return The authenticated User object, or null if not authenticated
     * 
     * WHY:
     * - After JwtAuthenticationFilter processes the request, the User object
     *   is stored in SecurityContext as the principal
     * - This method extracts it so services can use it
     * - Returns null if user is not authenticated (shouldn't happen for protected endpoints)
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        // The principal is the User object we set in JwtAuthenticationFilter
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof User) {
            return (User) principal;
        }
        
        // Fallback: if principal is email string, load user from database
        if (principal instanceof String) {
            String email = (String) principal;
            Optional<User> userOptional = userRepo.findByEmail(email);
            return userOptional.orElse(null);
        }
        
        return null;
    }

    /**
     * Gets the currently authenticated user's ID.
     * 
     * @return The UUID of the authenticated user, or null if not authenticated
     * 
     * WHY:
     * - Convenient method to get just the user ID without loading full user
     * - Used for filtering queries (e.g., find wallets by userId)
     */
    public java.util.UUID getCurrentUserId() {
        User user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    /**
     * Checks if the currently authenticated user has the ADMIN role.
     * 
     * @return true if user is ADMIN, false otherwise
     * 
     * WHY:
     * - Used throughout the application to check if user has admin privileges
     * - Admins can access all records, regular users can only access their own
     */
    public boolean isAdmin() {
        User user = getCurrentUser();
        return user != null && "ADMIN".equalsIgnoreCase(user.getRole());
    }

    /**
     * Checks if the currently authenticated user has a specific role.
     * 
     * @param role The role to check (e.g., "ADMIN", "USER")
     * @return true if user has the role, false otherwise
     * 
     * WHY:
     * - More flexible than isAdmin() for checking any role
     * - Useful if you add more roles in the future
     */
    public boolean hasRole(String role) {
        User user = getCurrentUser();
        return user != null && role != null && role.equalsIgnoreCase(user.getRole());
    }
}

