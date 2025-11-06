package com.example.E_Wallet.Security;

import com.example.E_Wallet.Repository.UserRepo;
import com.example.E_Wallet.Model.User;
import com.example.E_Wallet.Util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * JWT Authentication Filter
 * 
 * WHAT IT DOES:
 * This filter intercepts every HTTP request before it reaches your controllers.
 * It extracts the JWT token from the "Authorization" header, validates it,
 * and if valid, sets the authenticated user in Spring Security's context.
 * 
 * WHY IT'S NEEDED:
 * 1. Spring Security needs to know WHO is making the request
 * 2. We need to validate JWT tokens on every protected request
 * 3. Once validated, we load the user from database and tell Spring Security
 *    "This user is authenticated" so your controllers can access user info
 * 
 * HOW IT WORKS:
 * 1. Checks if request has "Authorization: Bearer <token>" header
 * 2. Extracts the token
 * 3. Validates the token using JwtUtil
 * 4. Extracts user info from token
 * 5. Loads full user from database
 * 6. Creates an Authentication object and sets it in SecurityContext
 * 7. If no token or invalid token, request continues but user is not authenticated
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepo userRepo;

    /**
     * This method is called for EVERY HTTP request.
     * It's where we check for JWT tokens and authenticate users.
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // Step 1: Extract the JWT token from the Authorization header
        // Format: "Authorization: Bearer <token>"
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String email = null;

        // Check if header exists and starts with "Bearer "
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // Extract the token (remove "Bearer " prefix)
            token = authHeader.substring(7); // "Bearer " is 7 characters

            try {
                // Step 2: Extract email from token
                // We do this BEFORE validation to get user info, but we'll validate next
                email = jwtUtil.getEmailFromToken(token);
            } catch (Exception e) {
                // If we can't extract email, token is invalid - continue without authentication
                logger.warn("Could not extract email from token: " + e.getMessage());
            }
        }

        // Step 3: Validate token and set authentication
        // We check if:
        // - Token exists
        // - Email was extracted successfully
        // - SecurityContext doesn't already have an authentication (avoid re-authenticating)
        // - Token is valid (not expired, not tampered with)
        if (token != null && email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            // Validate the token
            if (jwtUtil.validateToken(token)) {
                
                // Step 4: Load user from database using email from token
                // WHY: The token only contains basic info (email, userId).
                // We need the full User object with roles, permissions, etc.
                Optional<User> userOptional = userRepo.findByEmail(email);
                
                if (userOptional.isPresent()) {
                    User user = userOptional.get();
                    
                    // Step 5: Create an Authentication object
                    // UsernamePasswordAuthenticationToken is Spring Security's way of
                    // representing an authenticated user
                    // 
                    // Parameters:
                    // - user.getEmail(): The principal (who the user is)
                    // - null: Credentials (we don't need password here, token is proof)
                    // - authorities: Authorities/roles (ROLE_ADMIN or ROLE_USER)
                    // 
                    // WHY WE NEED ROLES:
                    // - Spring Security uses "ROLE_" prefix for role-based authorization
                    // - We store the full User object as principal so we can access it later
                    // - Authorities are used by @PreAuthorize and method security
                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    if (user.getRole() != null) {
                        // Add role with "ROLE_" prefix (Spring Security convention)
                        String roleName = "ROLE_" + user.getRole().toUpperCase();
                        authorities.add(new SimpleGrantedAuthority(roleName));
                    }
                    
                    UsernamePasswordAuthenticationToken authenticationToken = 
                        new UsernamePasswordAuthenticationToken(
                            user,              // Principal (store full User object for easy access)
                            null,              // Credentials (not needed, token is proof)
                            authorities        // Authorities (roles: ROLE_ADMIN or ROLE_USER)
                        );
                    
                    // Step 6: Add request details to authentication
                    // This includes IP address, session ID, etc. (useful for security logging)
                    authenticationToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    
                    // Step 7: Set the authentication in Spring Security's context
                    // This tells Spring Security: "This user is authenticated for this request"
                    // Now your controllers can access the authenticated user
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    
                    logger.info("User authenticated: " + email);
                } else {
                    logger.warn("User not found in database: " + email);
                }
            } else {
                logger.warn("Invalid JWT token for email: " + email);
            }
        }

        // Step 8: Continue the filter chain
        // This passes the request to the next filter or to your controller
        // If authentication was set above, the request will be authenticated
        // If not, the request will be unauthenticated (and may be rejected by SecurityConfig)
        filterChain.doFilter(request, response);
    }
}

