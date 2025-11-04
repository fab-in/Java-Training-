package com.example.E_Wallet.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.E_Wallet.Repository.UserRepo;
import com.example.E_Wallet.Model.User;
import com.example.E_Wallet.DTO.UserDTO;
import com.example.E_Wallet.DTO.UserCreateDTO;
import com.example.E_Wallet.Exceptions.DuplicateResourceException;
import com.example.E_Wallet.Exceptions.ResourceNotFoundException;
import com.example.E_Wallet.Exceptions.ValidationException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepo userRepo;

    public List<UserDTO> getUsers() {
        return userRepo.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public UserDTO createUser(UserCreateDTO userCreateDTO) {
        if (userCreateDTO.getEmail() == null || userCreateDTO.getEmail().trim().isEmpty()) {
            throw new ValidationException("Email is required and cannot be empty");
        }

        if (userCreateDTO.getPassword() == null || userCreateDTO.getPassword().trim().isEmpty()) {
            throw new ValidationException("Password is required and cannot be empty");
        }

        if (userRepo.existsByEmail(userCreateDTO.getEmail())) {
            throw new DuplicateResourceException("User with email '" + userCreateDTO.getEmail() + "' already exists");
        }

        User user = convertToEntity(userCreateDTO);

        List<Long> existingIds = userRepo.findAll().stream()
                .map(User::getId)
                .sorted()
                .collect(Collectors.toList());

        long nextId = 1;
        for (Long existingId : existingIds) {
            if (existingId == nextId) {
                nextId++;
            } else {
                break;
            }
        }
        user.setId(nextId);

        User savedUser = userRepo.save(user);
        return convertToDTO(savedUser);
    }

    public UserDTO getUserById(Long id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return convertToDTO(user);
    }

    public UserDTO updateUser(Long id, UserCreateDTO userCreateDTO) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (userCreateDTO.getEmail() != null && !userCreateDTO.getEmail().equals(user.getEmail())) {
            if (userRepo.existsByEmail(userCreateDTO.getEmail())) {
                throw new DuplicateResourceException(
                        "User with email '" + userCreateDTO.getEmail() + "' already exists");
            }
        }

        if (userCreateDTO.getName() != null) {
            user.setName(userCreateDTO.getName());
        }
        if (userCreateDTO.getEmail() != null) {
            user.setEmail(userCreateDTO.getEmail());
        }
        if (userCreateDTO.getPassword() != null) {
            user.setPassword(userCreateDTO.getPassword());
        }
        if (userCreateDTO.getPhoneNumber() != null) {
            user.setPhoneNumber(userCreateDTO.getPhoneNumber());
        }
        if (userCreateDTO.getRole() != null) {
            user.setRole(userCreateDTO.getRole());
        }

        User updatedUser = userRepo.save(user);
        return convertToDTO(updatedUser);
    }

    public void deleteUser(Long id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        userRepo.delete(user);
    }

    private UserDTO convertToDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setName(user.getName());
        userDTO.setEmail(user.getEmail());
        userDTO.setPhoneNumber(user.getPhoneNumber());
        userDTO.setRole(user.getRole());
        return userDTO;

    }

    private User convertToEntity(UserCreateDTO userCreateDTO) {
        User user = new User();
        user.setName(userCreateDTO.getName());

        user.setEmail(userCreateDTO.getEmail());
        user.setPassword(userCreateDTO.getPassword());
        user.setPhoneNumber(userCreateDTO.getPhoneNumber());
        user.setRole(userCreateDTO.getRole() != null ? userCreateDTO.getRole() : "USER");
        return user;
    }
}
