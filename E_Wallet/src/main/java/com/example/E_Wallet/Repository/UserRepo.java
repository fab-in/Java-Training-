package com.example.E_Wallet.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.E_Wallet.Model.User;

public interface UserRepo extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);
}
