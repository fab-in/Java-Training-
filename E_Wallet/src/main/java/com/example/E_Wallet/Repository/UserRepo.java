package com.example.E_Wallet.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;
import com.example.E_Wallet.Model.User;

public interface UserRepo extends JpaRepository<User, UUID> {

    boolean existsByEmail(String email);
    
    /**
     * Find a user by their email address
     * Used for login authentication
     */
    Optional<User> findByEmail(String email);
}
