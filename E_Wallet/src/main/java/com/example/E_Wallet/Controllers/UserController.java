package com.example.E_Wallet.Controllers;

import org.springframework.web.bind.annotation.RestController;

import com.example.E_Wallet.Service.UserService;
import com.example.E_Wallet.DTO.UserDTO;
import com.example.E_Wallet.DTO.UserCreateDTO;
import com.example.E_Wallet.DTO.LoginRequestDTO;
import com.example.E_Wallet.DTO.AuthResponseDTO;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;
import java.util.UUID;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/users")
    public List<UserDTO> getUsers() {
        return userService.getUsers();
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable UUID id) {
        UserDTO userDTO = userService.getUserById(id);
        return ResponseEntity.ok(userDTO);
    }

    @PostMapping("/users")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserCreateDTO userCreateDTO) {
        UserDTO userDTO = userService.createUser(userCreateDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(userDTO);
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable UUID id, @Valid @RequestBody UserCreateDTO userCreateDTO) {
        UserDTO userDTO = userService.updateUser(id, userCreateDTO);
        return ResponseEntity.ok(userDTO);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/auth/signup")
    public ResponseEntity<AuthResponseDTO> signup(@Valid @RequestBody UserCreateDTO userCreateDTO) {
        AuthResponseDTO response = userService.signup(userCreateDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/auth/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        AuthResponseDTO response = userService.login(loginRequest);
        return ResponseEntity.ok(response);
    }
}
