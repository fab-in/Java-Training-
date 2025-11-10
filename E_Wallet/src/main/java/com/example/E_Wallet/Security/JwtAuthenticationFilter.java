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


@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepo userRepo;

    
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String email = null;

        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            
            token = authHeader.substring(7); 

            try {
                
                email = jwtUtil.getEmailFromToken(token);
            } catch (Exception e) {
                logger.warn("Could not extract email from token: " + e.getMessage());
            }
        }

        
        if (token != null && email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            // Validate the token
            if (jwtUtil.validateToken(token)) {
                
                
                Optional<User> userOptional = userRepo.findByEmail(email);
                
                if (userOptional.isPresent()) {
                    User user = userOptional.get();
                    
                    
                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    if (user.getRole() != null) {
                        
                        String roleName = "ROLE_" + user.getRole().toUpperCase();
                        authorities.add(new SimpleGrantedAuthority(roleName));
                    }
                    
                    UsernamePasswordAuthenticationToken authenticationToken = 
                        new UsernamePasswordAuthenticationToken(
                            user,              // Principal 
                            null,              // Credentials 
                            authorities        // Authorities 
                        );
                    
                    
                    authenticationToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    
                    
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    
                    logger.info("User authenticated: " + email);
                } else {
                    logger.warn("User not found in database: " + email);
                }
            } else {
                logger.warn("Invalid JWT token for email: " + email);
            }
        }
        filterChain.doFilter(request, response);
    }
}

