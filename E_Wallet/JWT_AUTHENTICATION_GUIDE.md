# JWT Authentication Implementation Guide

## Overview

This guide explains how JWT (JSON Web Token) authentication has been implemented in your E-Wallet application. JWT authentication allows users to authenticate once (via login) and then use a token to access protected endpoints without sending their password with every request.

---

## How JWT Authentication Works

### The Flow:

1. **User Signs Up/Logs In** → Server validates credentials and generates a JWT token
2. **Client Receives Token** → Client stores the token (usually in localStorage or memory)
3. **Client Makes Protected Requests** → Client sends token in `Authorization: Bearer <token>` header
4. **Server Validates Token** → Server checks if token is valid, not expired, and not tampered with
5. **Server Processes Request** → If valid, request is processed; if invalid, request is rejected

---

## Components Explained

### 1. **JwtUtil** (`Util/JwtUtil.java`)

**Purpose**: Handles all JWT token operations (generation, validation, extraction)

**Key Methods**:
- `generateToken(userId, email)`: Creates a new JWT token containing user info
- `validateToken(token)`: Checks if token is valid (not expired, not tampered)
- `getEmailFromToken(token)`: Extracts email from token
- `getUserIdFromToken(token)`: Extracts user ID from token

**Why It's Needed**: 
- Centralizes all JWT operations
- Ensures consistent token handling
- Makes it easy to change token format later

---

### 2. **JwtAuthenticationFilter** (`Security/JwtAuthenticationFilter.java`)

**Purpose**: Intercepts every HTTP request and validates JWT tokens

**What It Does**:
1. Extracts token from `Authorization` header
2. Validates the token using `JwtUtil`
3. Loads user from database
4. Sets authentication in Spring Security context
5. Allows request to continue (or blocks if invalid)

**Why It's Needed**:
- Spring Security needs to know WHO is making each request
- This filter runs BEFORE your controllers, so authentication is handled automatically
- Without this, Spring Security wouldn't know how to validate JWT tokens

**Key Concept**: This is a **Filter**, which means it runs for EVERY request. It's like a security guard checking IDs at the door.

---

### 3. **SecurityConfig** (`Security/SecurityConfig.java`)

**Purpose**: Configures Spring Security to use JWT authentication

**What It Does**:
- Defines which endpoints are public (no auth needed)
- Defines which endpoints require authentication
- Configures Spring Security to use our JWT filter
- Sets up stateless sessions (no server-side sessions)

**Key Configuration**:
```java
.requestMatchers("/auth/signup", "/auth/login").permitAll()  // Public
.anyRequest().authenticated()  // Everything else needs auth
```

**Why It's Needed**:
- Without this, Spring Security would block ALL requests
- This tells Spring Security: "Use JWT tokens, not form login"
- Configures the security rules for your entire application

---

## How to Use JWT Authentication

### Step 1: User Logs In

**Request**:
```http
POST /auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "message": "Login successful",
  "user": {
    "id": "...",
    "email": "user@example.com",
    "name": "John Doe"
  }
}
```

### Step 2: Client Stores Token

Store the token securely:
- **Web App**: localStorage or sessionStorage
- **Mobile App**: Secure storage (Keychain/Keystore)
- **Server-to-Server**: Memory or secure config

### Step 3: Client Makes Protected Requests

Include the token in the Authorization header:

```http
GET /wallets
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Step 4: Server Validates and Processes

The JWT filter automatically:
1. Extracts token from header
2. Validates it
3. Loads user info
4. Allows request to proceed

---

## Endpoint Protection

### Public Endpoints (No Authentication Required):
- `POST /auth/signup` - User registration
- `POST /auth/login` - User login

### Protected Endpoints (Authentication Required):
- `GET /users` - Get all users
- `GET /users/{id}` - Get user by ID
- `POST /users` - Create user
- `PUT /users/{id}` - Update user
- `DELETE /users/{id}` - Delete user
- `GET /wallets` - Get all wallets
- `GET /wallets/{id}` - Get wallet by ID
- `POST /wallets` - Create wallet
- `PUT /wallets` - Update wallet
- `DELETE /wallets/{id}` - Delete wallet

**Any request to protected endpoints without a valid token will receive a 401 Unauthorized response.**

---

## Testing JWT Authentication

### Using cURL:

1. **Login**:
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}'
```

2. **Copy the token from response**

3. **Make Protected Request**:
```bash
curl -X GET http://localhost:8080/wallets \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### Using Postman:

1. **Login**: POST to `/auth/login` with email/password
2. **Copy token** from response
3. **Set Authorization**: 
   - Type: Bearer Token
   - Token: (paste your token)
4. **Make requests** to protected endpoints

---

## Security Features

### 1. **Token Expiration**
- Tokens expire after 15 minutes (configurable in `JwtUtil`)
- After expiration, user must login again
- Prevents indefinite access if token is stolen

### 2. **Token Signature**
- Tokens are signed with a secret key
- If token is tampered with, signature won't match
- Server rejects tampered tokens

### 3. **Password Hashing**
- Passwords are hashed using BCrypt before storage
- Even if database is compromised, passwords are unreadable
- BCrypt includes salt to prevent rainbow table attacks

### 4. **Stateless Sessions**
- No server-side session storage
- Each request is independent
- Makes API scalable (can run on multiple servers)

---

## Common Issues and Solutions

### Issue 1: "401 Unauthorized" on Protected Endpoints

**Causes**:
- Token not included in request
- Token expired
- Token format incorrect
- Token tampered with

**Solution**:
- Check that `Authorization: Bearer <token>` header is present
- Verify token hasn't expired (login again if needed)
- Ensure token is copied correctly (no extra spaces)

### Issue 2: Token Works But User Info Not Available

**Cause**: User might have been deleted from database after token was issued

**Solution**: Token is valid but user doesn't exist - this is handled gracefully

### Issue 3: "403 Forbidden" (Different from 401)

**401 Unauthorized**: No token or invalid token
**403 Forbidden**: Valid token but user doesn't have permission

Currently, all authenticated users have the same permissions. To add role-based access, you'd need to:
1. Add roles to User entity
2. Configure role-based access in SecurityConfig
3. Use `@PreAuthorize` annotations on controllers

---

## Advanced: Accessing Authenticated User in Controllers

If you need to get the current authenticated user in your controllers:

```java
@GetMapping("/wallets")
public ResponseEntity<List<WalletDTO>> getWallets() {
    // Get the authenticated user's email
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String userEmail = authentication.getName(); // This is the email from token
    
    // Or load full user from database
    User user = userRepo.findByEmail(userEmail)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    
    // Use user info in your logic
    return ResponseEntity.ok(walletService.getWallets());
}
```

---

## Token Structure

A JWT token has 3 parts separated by dots (`.`):

```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwidXNlcklkIjoiMTIzNCIsImV4cCI6MTYwOTUwMDAwMH0.signature
```

1. **Header**: Algorithm and token type
2. **Payload**: User info (email, userId, expiration)
3. **Signature**: Ensures token hasn't been tampered with

You can decode the payload (but not modify it) at https://jwt.io

---

## Summary

JWT authentication provides:
- ✅ Secure authentication without sending passwords repeatedly
- ✅ Stateless API (scalable)
- ✅ Token-based access control
- ✅ Automatic token validation on every request
- ✅ Easy to use from any client (web, mobile, etc.)

The implementation consists of:
1. **JwtUtil**: Token operations
2. **JwtAuthenticationFilter**: Request interception and validation
3. **SecurityConfig**: Spring Security configuration

All protected endpoints automatically require a valid JWT token!

