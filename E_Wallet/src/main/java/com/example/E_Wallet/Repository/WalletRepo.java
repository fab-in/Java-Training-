package com.example.E_Wallet.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.E_Wallet.Model.Wallet;

public interface WalletRepo extends JpaRepository<Wallet, Long> {

}
