package com.example.E_Wallet.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.E_Wallet.Model.User;
import java.util.Optional;

public interface UserRepo extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
