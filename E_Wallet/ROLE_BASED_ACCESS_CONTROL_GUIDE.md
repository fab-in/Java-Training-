# Role-Based Access Control (RBAC) Implementation Guide

## Overview

This guide explains how role-based access control (RBAC) is implemented in the E-Wallet application. The system ensures that:
- **ADMIN users** can access ALL wallet records
- **Regular USER** can only access THEIR OWN wallet records

---

## Architecture Overview

### Components Involved

1. **JwtAuthenticationFilter** - Extracts user info from JWT and sets authentication with roles
2. **SecurityUtil** - Helper class to get current authenticated user and check roles
3. **WalletRepo** - Repository with method to find wallets by user ID
4. **WalletService** - Service layer with role-based filtering logic
5. **WalletController** - REST endpoints (no changes needed, works automatically)

---

## How It Works: Step-by-Step Flow

### 1. User Authentication (JwtAuthenticationFilter)

**Location:** `Security/JwtAuthenticationFilter.java`

**What happens:**
1. User sends request with JWT token in `Authorization: Bearer <token>` header
2. Filter extracts token and validates it
3. Filter loads full User object from database (including role)
4. Filter creates `UsernamePasswordAuthenticationToken` with:
   - **Principal:** Full User object (so we can access it later)
   - **Authorities:** List containing `ROLE_ADMIN` or `ROLE_USER` (Spring Security convention)

**Why store User object as principal:**
- Easy access to user ID, email, role without extra database queries
- SecurityUtil can extract it directly from SecurityContext

**Why "ROLE_" prefix:**
- Spring Security convention for role-based authorization
- Enables use of `@PreAuthorize("hasRole('ADMIN')")` annotations (if needed later)

---

### 2. Getting Current User (SecurityUtil)

**Location:** `Security/SecurityUtil.java`

**What it does:**
- Provides helper methods to access the authenticated user
- Extracts User object from Spring Security's SecurityContext
- Provides convenient methods: `getCurrentUser()`, `getCurrentUserId()`, `isAdmin()`

**Why we need it:**
- Centralized way to get authenticated user
- Avoids repeating code in every service method
- Makes code cleaner and more maintainable

**Example usage:**
```java
User currentUser = securityUtil.getCurrentUser();
UUID userId = securityUtil.getCurrentUserId();
boolean isAdmin = securityUtil.isAdmin();
```

---

### 3. Repository Method for Filtering (WalletRepo)

**Location:** `Repository/WalletRepo.java`

**New method:**
```java
List<Wallet> findByUser_Id(UUID userId);
```

**What it does:**
- Spring Data JPA automatically generates SQL query based on method name
- `findByUser_Id` means: find wallets where `wallet.user.id = userId`
- Generates: `SELECT * FROM wallets WHERE user_id = ?`

**Why we need it:**
- Efficient way to query only a specific user's wallets
- Used when regular users request their wallets
- Better than loading all wallets and filtering in Java

---

### 4. Service Layer with RBAC (WalletService)

**Location:** `Service/WalletService.java`

This is where the main role-based logic is implemented. Each method checks the user's role and applies appropriate filtering.

#### A. `getWallets()` - Get All Wallets

**Logic:**
```java
if (user is ADMIN) {
    return ALL wallets from database
} else {
    return ONLY wallets belonging to current user
}
```

**Implementation:**
- Gets current user from SecurityUtil
- If admin: calls `walletRepo.findAll()` (all wallets)
- If regular user: calls `walletRepo.findByUser_Id(userId)` (filtered)
- Converts to DTOs and returns

**Example scenarios:**
- **Admin calls GET /wallets:** Returns all wallets in system
- **User1 calls GET /wallets:** Returns only User1's wallets
- **User2 calls GET /wallets:** Returns only User2's wallets

---

#### B. `getWalletById(Long id)` - Get Specific Wallet

**Logic:**
```java
1. Find wallet by ID
2. if (user is ADMIN) {
       return wallet (can access any wallet)
   } else {
       if (wallet belongs to user) {
           return wallet
       } else {
           throw "Access denied" exception
       }
   }
```

**Why this is important:**
- Prevents users from accessing other users' wallets by guessing IDs
- Example: User1 tries `GET /wallets/999` (User2's wallet) → Access denied
- Admin can access any wallet for support purposes

---

#### C. `updateWallet(WalletUpdateDTO)` - Update Wallet

**Logic:**
```java
1. Find wallet to update
2. if (user is NOT admin) {
       if (wallet does NOT belong to user) {
           throw "Access denied" exception
       }
       if (DTO.userId != current user ID) {
           throw "Cannot change ownership" exception
       }
   }
3. Update wallet fields
4. Save and return
```

**Additional security:**
- Regular users cannot change wallet ownership (cannot assign wallet to another user)
- Admins can update any wallet but still must provide valid userId

---

#### D. `deleteWallet(Long id)` - Delete Wallet

**Logic:**
```java
1. Find wallet to delete
2. if (user is NOT admin) {
       if (wallet does NOT belong to user) {
           throw "Access denied" exception
       }
   }
3. Delete wallet
```

**Why:**
- Prevents users from deleting other users' wallets
- Admins can delete any wallet for account management

---

## Security Flow Diagram

```
┌─────────────────┐
│  Client Request │
│  (with JWT)     │
└────────┬────────┘
         │
         ▼
┌─────────────────────────┐
│ JwtAuthenticationFilter │
│ - Validates JWT         │
│ - Loads User from DB    │
│ - Sets User + Role in   │
│   SecurityContext       │
└────────┬────────────────┘
         │
         ▼
┌─────────────────┐
│ WalletController│
│ (REST endpoint) │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  WalletService  │
│ - Gets user via │
│   SecurityUtil  │
│ - Checks role   │
│ - Filters data  │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│   WalletRepo    │
│ - Queries DB    │
│   (filtered if  │
│    needed)      │
└─────────────────┘
```

---

## Example Scenarios

### Scenario 1: Admin Views All Wallets

**Request:**
```
GET /wallets
Authorization: Bearer <admin_jwt_token>
```

**Flow:**
1. JwtAuthenticationFilter extracts admin user, sets role as `ROLE_ADMIN`
2. WalletController calls `walletService.getWallets()`
3. WalletService checks: `isAdmin() == true`
4. Calls `walletRepo.findAll()` → Returns ALL wallets
5. Returns all wallet DTOs

**Result:** Admin sees all wallets in the system

---

### Scenario 2: Regular User Views Their Wallets

**Request:**
```
GET /wallets
Authorization: Bearer <user1_jwt_token>
```

**Flow:**
1. JwtAuthenticationFilter extracts User1, sets role as `ROLE_USER`
2. WalletController calls `walletService.getWallets()`
3. WalletService checks: `isAdmin() == false`
4. Gets User1's ID: `currentUser.getId()`
5. Calls `walletRepo.findByUser_Id(user1Id)` → Returns only User1's wallets
6. Returns filtered wallet DTOs

**Result:** User1 sees only their own wallets

---

### Scenario 3: User Tries to Access Another User's Wallet

**Request:**
```
GET /wallets/999
Authorization: Bearer <user1_jwt_token>
(Wallet 999 belongs to User2)
```

**Flow:**
1. JwtAuthenticationFilter extracts User1, sets role as `ROLE_USER`
2. WalletController calls `walletService.getWalletById(999)`
3. WalletService finds wallet 999 (belongs to User2)
4. Checks: `isAdmin() == false`
5. Checks ownership: `wallet.getUser().getId() != user1.getId()` → **FAILS**
6. Throws `ValidationException("Access denied: You can only access your own wallets")`

**Result:** 403 Forbidden / Access Denied error

---

### Scenario 4: Admin Accesses Any Wallet

**Request:**
```
GET /wallets/999
Authorization: Bearer <admin_jwt_token>
(Wallet 999 belongs to User2)
```

**Flow:**
1. JwtAuthenticationFilter extracts admin user, sets role as `ROLE_ADMIN`
2. WalletController calls `walletService.getWalletById(999)`
3. WalletService finds wallet 999
4. Checks: `isAdmin() == true` → **SKIPS ownership check**
5. Returns wallet DTO

**Result:** Admin successfully accesses User2's wallet

---

## Key Security Principles

### 1. **Principle of Least Privilege**
- Regular users get minimum access needed (only their own data)
- Admins get elevated privileges only when necessary

### 2. **Defense in Depth**
- Multiple layers of security:
  - JWT validation (authentication)
  - Role checking (authorization)
  - Ownership verification (data access control)

### 3. **Fail Secure**
- If authentication fails → request is rejected
- If authorization fails → access is denied
- If ownership check fails → access is denied

### 4. **Explicit Permission Checks**
- Every method explicitly checks role and ownership
- No assumptions about access rights

---

## Testing the Implementation

### Test as Admin:
1. Create a user with role "ADMIN"
2. Login to get JWT token
3. Call `GET /wallets` → Should return ALL wallets
4. Call `GET /wallets/{anyId}` → Should return wallet regardless of owner

### Test as Regular User:
1. Create a user with role "USER" (or default)
2. Login to get JWT token
3. Call `GET /wallets` → Should return ONLY your wallets
4. Call `GET /wallets/{yourWalletId}` → Should return your wallet
5. Call `GET /wallets/{otherUserWalletId}` → Should return 403 Access Denied

---

## Future Enhancements

### Possible Improvements:
1. **Method-level security:** Use `@PreAuthorize("hasRole('ADMIN')")` annotations
2. **Audit logging:** Log all access attempts (successful and failed)
3. **Fine-grained permissions:** More roles (e.g., MODERATOR, SUPPORT)
4. **Resource-level permissions:** Permissions per wallet (e.g., read-only access)

---

## Summary

The RBAC implementation ensures:
- ✅ Admins can access all wallet records
- ✅ Regular users can only access their own wallet records
- ✅ Security is enforced at the service layer
- ✅ Clear error messages when access is denied
- ✅ Efficient database queries (filtered at DB level, not in Java)

All wallet operations (GET, UPDATE, DELETE) respect these rules automatically!

