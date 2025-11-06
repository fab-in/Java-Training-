package com.example.E_Wallet.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.E_Wallet.Repository.UserRepo;
import com.example.E_Wallet.Model.User;
import com.example.E_Wallet.DTO.UserDTO;
import com.example.E_Wallet.DTO.UserCreateDTO;
import com.example.E_Wallet.DTO.LoginRequestDTO;
import com.example.E_Wallet.DTO.AuthResponseDTO;
import com.example.E_Wallet.Util.JwtUtil;
import com.example.E_Wallet.Exceptions.DuplicateResourceException;
import com.example.E_Wallet.Exceptions.ResourceNotFoundException;
import com.example.E_Wallet.Exceptions.ValidationException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepo userRepo;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    // BCrypt password encoder - securely hashes passwords
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

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
            // Hash the password before saving
            String hashedPassword = passwordEncoder.encode(userCreateDTO.getPassword());
            user.setPassword(hashedPassword);
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

    
    public AuthResponseDTO login(LoginRequestDTO loginRequest) {
        
        if (loginRequest.getEmail() == null || loginRequest.getEmail().trim().isEmpty()) {
            throw new ValidationException("Email is required");
        }
        if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
            throw new ValidationException("Password is required");
        }

        // Find user by email
        Optional<User> userOptional = userRepo.findByEmail(loginRequest.getEmail());
        
        if (userOptional.isEmpty()) {
            throw new ValidationException("Invalid email or password");
        }

        User user = userOptional.get();

        
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new ValidationException("Invalid email or password");
        }

        
        String token = jwtUtil.generateToken(user.getId(), user.getEmail());

        
        UserDTO userDTO = convertToDTO(user);

        
        AuthResponseDTO response = new AuthResponseDTO();
        response.setToken(token);
        response.setMessage("Login successful");
        response.setUser(userDTO);

        return response;
    }

    /**
     * Signup method - creates a new user and returns JWT token
     * This is essentially the same as createUser but returns a JWT token
     * 
     * Steps:
     * 1. Validate input
     * 2. Check if email already exists
     * 3. Hash password using BCrypt
     * 4. Save user to database
     * 5. Generate JWT token
     * 6. Return token and user info
     * 
     * @param userCreateDTO User information for signup
     * @return AuthResponseDTO with JWT token and user info
     */
    public AuthResponseDTO signup(UserCreateDTO userCreateDTO) {
        // Use existing createUser logic but return token
        UserDTO userDTO = createUser(userCreateDTO);
        
        // Get the saved user to generate token
        User user = userRepo.findById(userDTO.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found after creation"));

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getId(), user.getEmail());

        // Return response with token and user info
        AuthResponseDTO response = new AuthResponseDTO();
        response.setToken(token);
        response.setMessage("Signup successful");
        response.setUser(userDTO);

        return response;
    }

    private User convertToEntity(UserCreateDTO userCreateDTO) {
        User user = new User();
        user.setName(userCreateDTO.getName());

        user.setEmail(userCreateDTO.getEmail());
        // Hash the password before saving (BCrypt automatically generates salt)
        String hashedPassword = passwordEncoder.encode(userCreateDTO.getPassword());
        user.setPassword(hashedPassword);
        user.setPhoneNumber(userCreateDTO.getPhoneNumber());
        user.setRole(userCreateDTO.getRole() != null ? userCreateDTO.getRole() : "USER");
        return user;
    }
}
