# JWT Authentication & Authorization - Interview Q&A
## Based on E-Wallet Project Implementation

---

## **SECTION 1: JWT BASICS**

### **Q1: What is JWT and why did you use it in this project?**

**Answer:**
JWT (JSON Web Token) is a compact, URL-safe token format used for securely transmitting information between parties. In this project, I used JWT for:

1. **Stateless Authentication**: No need to store sessions on the server, making the application scalable
2. **Token-Based Security**: Each request includes a token that proves the user's identity
3. **Microservices Ready**: Tokens can be validated independently without shared session storage
4. **Mobile-Friendly**: Works well with mobile apps and SPAs (Single Page Applications)

The token contains:
- User's email (as subject)
- User's ID (as a claim)
- Expiration time (30 minutes)
- Digital signature (HMAC-SHA256) to prevent tampering

---

### **Q2: Explain the structure of a JWT token.**

**Answer:**
A JWT has three parts separated by dots (`.`):

```
header.payload.signature
```

**1. Header** (Base64 encoded):
```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```
- `alg`: Algorithm used for signature (HMAC-SHA256)
- `typ`: Token type

**2. Payload** (Base64 encoded):
```json
{
  "sub": "user@example.com",     // Subject (email)
  "userId": "uuid-here",          // Custom claim
  "iat": 1234567890,             // Issued at timestamp
  "exp": 1234567890              // Expiration timestamp
}
```

**3. Signature**:
```
HMAC-SHA256(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  secretKey
)
```

In our implementation (`JwtUtil.java`), the token is generated with:
- Subject: user's email
- Claim: userId (UUID)
- Expiration: 30 minutes from creation
- Signature: HMAC-SHA256 with a 256-bit secret key

---

### **Q3: How is the JWT secret key generated in your project?**

**Answer:**
In `JwtUtil.java`, the secret key is generated using Java's `KeyGenerator`:

```java
@PostConstruct
public void initializeSecretKey() {
    KeyGenerator keyGenerator = KeyGenerator.getInstance("HmacSHA256");
    keyGenerator.init(256);  // 256-bit key
    SecretKey generatedKey = keyGenerator.generateKey();
    // Convert to 32-byte array for HMAC-SHA256
    this.secretKey = Keys.hmacShaKeyFor(finalKeyBytes);
}
```

**Key Points:**
- Generated once at application startup (`@PostConstruct`)
- 256-bit key for HMAC-SHA256 algorithm
- Randomly generated for security
- **Note**: In production, this should be stored in environment variables or a secure key management system, not generated at runtime

**Production Recommendation:**
```java
@Value("${jwt.secret}")
private String jwtSecret;

// Then convert to SecretKey
this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
```

---

## **SECTION 2: AUTHENTICATION FLOW**

### **Q4: Walk me through the complete authentication flow from login to accessing a protected endpoint.**

**Answer:**

**Step 1: User Login**
```
POST /auth/login
Body: { "email": "user@example.com", "password": "password123" }
```

**Step 2: UserService.login() validates credentials**
- Checks if user exists by email
- Verifies password using BCrypt password encoder
- If valid, generates JWT token

**Step 3: JWT Token Generation** (`JwtUtil.generateToken()`)
```java
String token = jwtUtil.generateToken(user.getId(), user.getEmail());
// Token contains: email, userId, expiration (30 min)
```

**Step 4: Token Returned to Client**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "message": "Login successful",
  "user": { ... }
}
```

**Step 5: Client Makes Authenticated Request**
```
GET /wallets
Headers: Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Step 6: JwtAuthenticationFilter Intercepts Request**
- Extracts token from `Authorization` header
- Validates token signature and expiration
- Extracts email from token
- Loads User object from database (including role)
- Creates Spring Security Authentication object
- Sets it in SecurityContext

**Step 7: Controller/Service Access**
- `SecurityUtil.getCurrentUser()` retrieves authenticated user
- Business logic executes with user context

---

### **Q5: How does the JwtAuthenticationFilter work?**

**Answer:**
The `JwtAuthenticationFilter` extends `OncePerRequestFilter` and runs before every request:

**Location:** `Security/JwtAuthenticationFilter.java`

**Process:**

1. **Extract Token** (lines 41-48):
```java
String authHeader = request.getHeader("Authorization");
if (authHeader != null && authHeader.startsWith("Bearer ")) {
    token = authHeader.substring(7);  // Remove "Bearer " prefix
}
```

2. **Extract Email from Token** (line 52):
```java
email = jwtUtil.getEmailFromToken(token);
```

3. **Validate Token** (line 62):
```java
if (jwtUtil.validateToken(token)) {
    // Token is valid
}
```

4. **Load User from Database** (line 65):
```java
Optional<User> userOptional = userRepo.findByEmail(email);
User user = userOptional.get();
```

5. **Create Spring Security Authentication** (lines 71-83):
```java
List<SimpleGrantedAuthority> authorities = new ArrayList<>();
String roleName = "ROLE_" + user.getRole().toUpperCase();
authorities.add(new SimpleGrantedAuthority(roleName));

UsernamePasswordAuthenticationToken authenticationToken = 
    new UsernamePasswordAuthenticationToken(
        user,              // Principal = Full User object
        null,              // Credentials (not needed)
        authorities        // Authorities = ["ROLE_ADMIN"] or ["ROLE_USER"]
    );
```

6. **Set in SecurityContext** (line 91):
```java
SecurityContextHolder.getContext().setAuthentication(authenticationToken);
```

**Why load User from database?**
- Token only contains email and userId
- We need the full User object including the role for authorization
- This ensures role changes in database are reflected (though token would need refresh)

---

### **Q6: Why do you store the User object as the Principal instead of just the email?**

**Answer:**

**Advantages:**
1. **Avoids Repeated Database Queries**: Once loaded, the User object is available throughout the request lifecycle
2. **Easy Access to User Properties**: Can access `user.getId()`, `user.getRole()`, `user.getEmail()` without additional queries
3. **Better Performance**: No need to query database in every service method
4. **Type Safety**: `SecurityUtil.getCurrentUser()` returns `User` object directly

**Example Usage:**
```java
// In WalletService
User currentUser = securityUtil.getCurrentUser();  // No DB query needed
UUID userId = currentUser.getId();                  // Direct access
String role = currentUser.getRole();                // Direct access
```

**Alternative Approach:**
If we stored only email, we'd need to query the database every time:
```java
String email = (String) principal;
User user = userRepo.findByEmail(email);  // Extra DB query every time
```

---

### **Q7: How is token validation performed?**

**Answer:**
Token validation happens in `JwtUtil.validateToken()` (lines 72-99):

```java
public boolean validateToken(String token) {
    try {
        Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token);
        return true;
    } catch (ExpiredJwtException e) {
        // Token has expired
        return false;
    } catch (MalformedJwtException e) {
        // Invalid token format
        return false;
    } catch (SignatureException e) {
        // Invalid signature (token was tampered with)
        return false;
    } catch (Exception e) {
        // Other errors
        return false;
    }
}
```

**What it checks:**
1. **Signature Verification**: Ensures token wasn't tampered with
2. **Expiration Check**: Verifies token hasn't expired
3. **Format Validation**: Ensures token structure is correct
4. **Algorithm Verification**: Confirms correct signing algorithm

**Security Benefits:**
- If someone modifies the payload, signature won't match → validation fails
- Expired tokens are automatically rejected
- Invalid tokens are caught before processing

---

## **SECTION 3: AUTHORIZATION & ROLE-BASED ACCESS**

### **Q8: How does role-based authorization work in your project?**

**Answer:**

**Three-Layer Authorization System:**

**Layer 1: Role Storage**
- User model has `role` field (default: "USER")
- Stored in database
- Can be "USER" or "ADMIN"

**Layer 2: Role Extraction in Filter**
- `JwtAuthenticationFilter` loads User from database
- Extracts role and creates authority: `"ROLE_" + user.getRole().toUpperCase()`
- Sets authority in Spring Security Authentication object

**Layer 3: Role Checking in Service Layer**
- `SecurityUtil.isAdmin()` checks if current user has "ADMIN" role
- Service methods check role before allowing operations

**Example in WalletService.getWallets():**
```java
if (securityUtil.isAdmin()) {
    wallets = walletRepo.findAll();  // Admin: all wallets
} else {
    wallets = walletRepo.findByUser_Id(currentUser.getId());  // User: only their wallets
}
```

---

### **Q9: Explain the difference between authentication and authorization in your project.**

**Answer:**

**Authentication (Who are you?):**
- **Purpose**: Verify user identity
- **Implementation**: JWT token validation
- **Location**: `JwtAuthenticationFilter`
- **Checks**: 
  - Is the token valid?
  - Is the token expired?
  - Does the user exist in database?

**Authorization (What can you do?):**
- **Purpose**: Verify user permissions
- **Implementation**: Role-based checks
- **Location**: `WalletService` methods
- **Checks**:
  - Is the user an ADMIN?
  - Does the wallet belong to the user?
  - Can the user perform this operation?

**Example:**
```java
// Authentication happens in filter
if (jwtUtil.validateToken(token)) {
    // User is authenticated
}

// Authorization happens in service
if (!securityUtil.isAdmin()) {
    if (!wallet.getUser().getId().equals(currentUser.getId())) {
        throw new ValidationException("Access denied");
    }
}
```

---

### **Q10: How do you prevent a regular user from accessing another user's wallet?**

**Answer:**

**Multiple Security Checks:**

**1. In `getWalletById()`:**
```java
if (!securityUtil.isAdmin()) {
    if (!wallet.getUser().getId().equals(currentUser.getId())) {
        throw new ValidationException("Access denied: You can only access your own wallets");
    }
}
```

**2. In `getWallets()`:**
```java
if (securityUtil.isAdmin()) {
    wallets = walletRepo.findAll();  // All wallets
} else {
    wallets = walletRepo.findByUser_Id(currentUser.getId());  // Only user's wallets
}
```

**3. In `updateWallet()`:**
```java
if (!isAdmin) {
    // Verify wallet belongs to user
    if (!wallet.getUser().getId().equals(currentUser.getId())) {
        throw new ValidationException("Access denied");
    }
    // Prevent ownership change
    if (walletUpdateDTO.getNewUserName() != null) {
        throw new ValidationException("You cannot change wallet ownership");
    }
}
```

**Security Principle**: Defense in Depth - multiple layers of checks ensure security even if one layer fails.

---

### **Q11: Why do you use "ROLE_" prefix for Spring Security authorities?**

**Answer:**

**Spring Security Convention:**
- Spring Security expects roles to have `ROLE_` prefix when using `hasRole()` method
- This is a framework convention, not a requirement

**In our code:**
```java
String roleName = "ROLE_" + user.getRole().toUpperCase();
// "USER" → "ROLE_USER"
// "ADMIN" → "ROLE_ADMIN"
```

**Benefits:**
1. **Future Compatibility**: Enables use of `@PreAuthorize("hasRole('ADMIN')")` annotations
2. **Standard Practice**: Follows Spring Security best practices
3. **Clear Distinction**: Separates roles from other authorities

**Alternative (without prefix):**
We could use `hasAuthority("ADMIN")` instead, but `hasRole()` is more semantic for role-based access.

---

### **Q12: How does SecurityUtil help with authorization?**

**Answer:**

`SecurityUtil` provides centralized helper methods:

**1. `getCurrentUser()`:**
```java
public User getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    Object principal = authentication.getPrincipal();
    if (principal instanceof User) {
        return (User) principal;
    }
    return null;
}
```
- Extracts authenticated user from SecurityContext
- Avoids repeating code in every service method

**2. `getCurrentUserId()`:**
```java
public UUID getCurrentUserId() {
    User user = getCurrentUser();
    return user != null ? user.getId() : null;
}
```
- Quick access to user ID

**3. `isAdmin()`:**
```java
public boolean isAdmin() {
    User user = getCurrentUser();
    return user != null && "ADMIN".equalsIgnoreCase(user.getRole());
}
```
- Centralized admin check
- Case-insensitive comparison

**4. `hasRole(String role)`:**
```java
public boolean hasRole(String role) {
    User user = getCurrentUser();
    return user != null && role != null && role.equalsIgnoreCase(user.getRole());
}
```
- Generic role checker for future roles

**Benefits:**
- **DRY Principle**: Don't Repeat Yourself
- **Consistency**: Same logic everywhere
- **Maintainability**: Change authorization logic in one place
- **Testability**: Easy to mock SecurityUtil

---

## **SECTION 4: SECURITY CONCERNS**

### **Q13: What security vulnerabilities exist in your current implementation and how would you fix them?**

**Answer:**

**1. Secret Key Generation**
**Issue**: Secret key is generated at runtime, changes on every restart
**Fix**: Store in environment variable or secure key management system
```java
@Value("${jwt.secret}")
private String jwtSecret;
```

**2. Role Assignment During Signup**
**Issue**: Anyone can set role to "ADMIN" during signup
**Fix**: Remove role from signup DTO, assign default "USER", only admins can change roles
```java
// In UserService.signup()
user.setRole("USER");  // Always default, ignore DTO role
```

**3. Token Expiration**
**Issue**: 30 minutes might be too short for some users, too long for security
**Fix**: Implement refresh tokens
- Short-lived access tokens (15 minutes)
- Long-lived refresh tokens (7 days)
- Endpoint to refresh access token

**4. No Token Blacklisting**
**Issue**: Tokens can't be revoked until expiration
**Fix**: Implement token blacklist (Redis) or token versioning
```java
// Add tokenVersion to User model
// Increment on logout/password change
// Check version in token validation
```

**5. Password Storage**
**Current**: Using BCrypt (good!)
**Enhancement**: Add password strength requirements (already done in DTO validation)

**6. Rate Limiting**
**Issue**: No protection against brute force attacks
**Fix**: Implement rate limiting on login endpoint
```java
@RateLimiter(name = "login")
public AuthResponseDTO login(LoginRequestDTO request) { ... }
```

---

### **Q14: How do you handle token expiration?**

**Answer:**

**Current Implementation:**
- Tokens expire after 30 minutes
- Expiration is checked in `JwtUtil.validateToken()`
- If expired, `ExpiredJwtException` is thrown
- User must re-login to get a new token

**In JwtUtil:**
```java
private static final long EXPIRATION_TIME = 30 * 60 * 1000; // 30 minutes

public String generateToken(UUID userId, String email) {
    Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);
    return Jwts.builder()
        .expiration(expiryDate)
        // ...
}
```

**Validation:**
```java
public boolean validateToken(String token) {
    try {
        Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
        return true;
    } catch (ExpiredJwtException e) {
        return false;  // Token expired
    }
}
```

**Improvement - Refresh Tokens:**
```java
// Generate two tokens
String accessToken = generateToken(userId, email, 15 * 60 * 1000);  // 15 min
String refreshToken = generateToken(userId, email, 7 * 24 * 60 * 60 * 1000);  // 7 days

// Store refreshToken in database
// Endpoint: POST /auth/refresh
// If refreshToken valid, issue new accessToken
```

---

### **Q15: What happens if someone steals a JWT token?**

**Answer:**

**Current Risks:**
1. **Token Theft**: If token is intercepted (man-in-the-middle), attacker can use it until expiration
2. **No Revocation**: Can't invalidate token until it expires
3. **XSS Attacks**: If stored in localStorage, vulnerable to XSS

**Mitigation Strategies:**

**1. HTTPS Only**
- Always use HTTPS in production
- Prevents token interception

**2. Short Expiration Time**
- Current: 30 minutes
- Shorter tokens = less damage if stolen

**3. Token Storage**
- Don't store in localStorage (XSS vulnerable)
- Use httpOnly cookies (better, but not perfect)
- Or use in-memory storage

**4. Refresh Tokens**
- Long-lived refresh token stored securely
- Short-lived access tokens
- Can revoke refresh token

**5. Token Blacklisting**
```java
// On logout or suspicious activity
blacklistService.addToBlacklist(tokenId, expirationTime);

// In filter
if (blacklistService.isBlacklisted(tokenId)) {
    throw new SecurityException("Token revoked");
}
```

**6. Additional Security Headers**
```java
// In SecurityConfig
http.headers(headers -> headers
    .httpStrictTransportSecurity(hsts -> hsts.maxAgeInSeconds(31536000))
    .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
);
```

---

### **Q16: Why is the password hashed and how does BCrypt work?**

**Answer:**

**Why Hash Passwords?**
- **Security**: If database is compromised, attackers can't see actual passwords
- **One-Way**: Can't reverse hash to get original password
- **Verification**: Compare hash of input password with stored hash

**BCrypt in Our Project:**
```java
// During signup (UserService.convertToEntity)
String hashedPassword = passwordEncoder.encode(userCreateDTO.getPassword());
user.setPassword(hashedPassword);

// During login (UserService.login)
if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
    throw new ValidationException("Invalid email or password");
}
```

**How BCrypt Works:**
1. **Salt**: Automatically generates random salt for each password
2. **Cost Factor**: Number of rounds (default: 10 = 2^10 = 1024 iterations)
3. **Output**: `$2a$10$N9qo8uLOickgx2ZMRZoMye.IjZAgcfl7p92ldGxad68LJZdL17lhW`
   - `$2a$`: BCrypt version
   - `10`: Cost factor
   - Next 22 chars: Salt
   - Remaining: Hashed password

**Benefits:**
- **Slow by Design**: Resistant to brute force attacks
- **Unique Hashes**: Same password produces different hashes (due to salt)
- **Adaptive**: Can increase cost factor as hardware improves

---

## **SECTION 5: IMPLEMENTATION DETAILS**

### **Q17: Why did you use a filter instead of an interceptor for JWT validation?**

**Answer:**

**Filter vs Interceptor:**

**Filter (Current Implementation):**
- Runs at **servlet level** (before Spring MVC)
- Can intercept **all requests** (including static resources)
- More control over request/response
- Runs **before** Spring Security chain

**Interceptor:**
- Runs at **Spring MVC level** (after servlet)
- Only intercepts **controller requests**
- Less control over request/response
- Runs **after** Spring Security

**Why Filter is Better for JWT:**
1. **Early Validation**: Validates token before Spring Security processes request
2. **Security Context Setup**: Sets authentication in SecurityContext before authorization checks
3. **Consistent**: Works with Spring Security's filter chain
4. **Standard Practice**: JWT validation is typically done in filters

**Our Implementation:**
```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    // Runs once per request
    // Sets SecurityContext before authorization
}
```

**In SecurityConfig:**
```java
.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
```
- Runs before Spring Security's default authentication filter
- Ensures JWT is validated first

---

### **Q18: Explain the SecurityConfig configuration.**

**Answer:**

**Location:** `Security/SecurityConfig.java`

**Key Components:**

**1. Public Endpoints:**
```java
.requestMatchers("/auth/signup", "/auth/login").permitAll()
```
- These endpoints don't require authentication
- Anyone can signup/login

**2. Protected Endpoints:**
```java
.anyRequest().authenticated()
```
- All other endpoints require valid JWT token
- If no token or invalid token → 401 Unauthorized

**3. Stateless Sessions:**
```java
.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
```
- No server-side sessions
- Each request is independent
- JWT token provides authentication

**4. CSRF Disabled:**
```java
.csrf(csrf -> csrf.disable())
```
- CSRF protection not needed for stateless JWT
- CSRF protects against session-based attacks
- JWT tokens are immune to CSRF (stored in header, not cookie)

**5. JWT Filter Integration:**
```java
.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
```
- Custom JWT filter runs before Spring Security's default filter
- Ensures JWT validation happens first

**6. Method Security Enabled:**
```java
@EnableMethodSecurity
```
- Enables `@PreAuthorize`, `@PostAuthorize` annotations
- Currently not used, but available for future

---

### **Q19: How would you implement refresh tokens?**

**Answer:**

**1. Add RefreshToken Entity:**
```java
@Entity
public class RefreshToken {
    @Id
    private String token;
    private UUID userId;
    private LocalDateTime expiryDate;
    private boolean revoked;
}
```

**2. Modify Token Generation:**
```java
public AuthResponseDTO login(LoginRequestDTO request) {
    // ... validate credentials ...
    
    // Short-lived access token (15 min)
    String accessToken = jwtUtil.generateToken(user.getId(), user.getEmail(), 15 * 60 * 1000);
    
    // Long-lived refresh token (7 days)
    String refreshToken = jwtUtil.generateToken(user.getId(), user.getEmail(), 7 * 24 * 60 * 60 * 1000);
    
    // Store refresh token in database
    refreshTokenService.saveRefreshToken(user.getId(), refreshToken);
    
    AuthResponseDTO response = new AuthResponseDTO();
    response.setAccessToken(accessToken);
    response.setRefreshToken(refreshToken);
    return response;
}
```

**3. Create Refresh Endpoint:**
```java
@PostMapping("/auth/refresh")
public ResponseEntity<AuthResponseDTO> refreshToken(@RequestBody RefreshTokenRequest request) {
    String refreshToken = request.getRefreshToken();
    
    // Validate refresh token
    if (!jwtUtil.validateToken(refreshToken)) {
        throw new ValidationException("Invalid refresh token");
    }
    
    // Check if token exists in database and not revoked
    RefreshToken storedToken = refreshTokenService.findByToken(refreshToken);
    if (storedToken == null || storedToken.isRevoked()) {
        throw new ValidationException("Refresh token revoked");
    }
    
    // Generate new access token
    UUID userId = jwtUtil.getUserIdFromToken(refreshToken);
    User user = userRepo.findById(userId).orElseThrow();
    
    String newAccessToken = jwtUtil.generateToken(user.getId(), user.getEmail(), 15 * 60 * 1000);
    
    AuthResponseDTO response = new AuthResponseDTO();
    response.setAccessToken(newAccessToken);
    response.setRefreshToken(refreshToken);  // Same refresh token
    return ResponseEntity.ok(response);
}
```

**4. Logout Endpoint:**
```java
@PostMapping("/auth/logout")
public ResponseEntity<?> logout(@RequestBody RefreshTokenRequest request) {
    refreshTokenService.revokeToken(request.getRefreshToken());
    return ResponseEntity.ok().build();
}
```

**Benefits:**
- Better security (shorter access token lifetime)
- Better UX (users don't need to re-login frequently)
- Can revoke refresh tokens

---

### **Q20: How do you handle concurrent requests with the same JWT token?**

**Answer:**

**Current Implementation:**
- Each request is **stateless** and **independent**
- Same token can be used in multiple concurrent requests
- Each request validates token independently
- No locking or synchronization needed

**How it works:**
1. Request 1 arrives with token → Filter validates → Sets SecurityContext → Processes
2. Request 2 arrives with same token → Filter validates → Sets SecurityContext → Processes
3. Both run concurrently without interference

**SecurityContext is Thread-Local:**
- Each thread has its own SecurityContext
- No interference between concurrent requests
- Thread-safe by design

**Potential Issues:**
- **Token Expiration**: If token expires during processing, subsequent requests fail
- **Role Changes**: If user's role changes in database, old token still has old role until expiration

**Solutions:**
1. **Token Versioning**: Add version to User, include in token, check on validation
2. **Short Token Lifetime**: Reduces window for stale permissions
3. **Refresh Tokens**: Force re-authentication periodically

---

## **SECTION 6: TESTING & TROUBLESHOOTING**

### **Q21: How would you test JWT authentication?**

**Answer:**

**1. Unit Tests:**

```java
@Test
public void testTokenGeneration() {
    UUID userId = UUID.randomUUID();
    String email = "test@example.com";
    String token = jwtUtil.generateToken(userId, email);
    
    assertNotNull(token);
    assertEquals(email, jwtUtil.getEmailFromToken(token));
    assertEquals(userId, jwtUtil.getUserIdFromToken(token));
}

@Test
public void testTokenValidation() {
    String token = jwtUtil.generateToken(userId, email);
    assertTrue(jwtUtil.validateToken(token));
}

@Test
public void testExpiredToken() {
    // Generate token with 1ms expiration
    String token = generateTokenWithExpiration(1);
    Thread.sleep(2);
    assertFalse(jwtUtil.validateToken(token));
}
```

**2. Integration Tests:**

```java
@Test
public void testLoginReturnsToken() {
    // Create user
    UserCreateDTO userDTO = new UserCreateDTO();
    userDTO.setEmail("test@example.com");
    userDTO.setPassword("Password123");
    userService.signup(userDTO);
    
    // Login
    LoginRequestDTO loginDTO = new LoginRequestDTO();
    loginDTO.setEmail("test@example.com");
    loginDTO.setPassword("Password123");
    
    AuthResponseDTO response = userService.login(loginDTO);
    
    assertNotNull(response.getToken());
    assertTrue(jwtUtil.validateToken(response.getToken()));
}

@Test
public void testProtectedEndpointRequiresToken() {
    // Request without token
    mockMvc.perform(get("/wallets"))
        .andExpect(status().isUnauthorized());
    
    // Request with invalid token
    mockMvc.perform(get("/wallets")
        .header("Authorization", "Bearer invalid-token"))
        .andExpect(status().isUnauthorized());
    
    // Request with valid token
    String token = getValidToken();
    mockMvc.perform(get("/wallets")
        .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());
}
```

**3. Security Tests:**

```java
@Test
public void testUserCannotAccessOtherUserWallet() {
    // Create two users
    User user1 = createUser("user1@example.com");
    User user2 = createUser("user2@example.com");
    
    // Create wallet for user2
    Wallet wallet = createWallet(user2);
    
    // Login as user1
    String token1 = login(user1.getEmail(), "password");
    
    // Try to access user2's wallet
    mockMvc.perform(get("/wallets/" + wallet.getId())
        .header("Authorization", "Bearer " + token1))
        .andExpect(status().isForbidden());
}

@Test
public void testAdminCanAccessAllWallets() {
    User admin = createAdminUser();
    String adminToken = login(admin.getEmail(), "password");
    
    mockMvc.perform(get("/wallets")
        .header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(greaterThan(1)));  // Multiple wallets
}
```

---

### **Q22: What would you do if a user reports "Token expired" errors frequently?**

**Answer:**

**Diagnosis Steps:**

1. **Check Token Expiration Time:**
```java
// Current: 30 minutes
private static final long EXPIRATION_TIME = 30 * 60 * 1000;
```
- If too short, increase it
- If too long, implement refresh tokens

2. **Check System Clock:**
- JWT expiration uses server time
- If server clock is wrong, tokens expire incorrectly
- Ensure NTP synchronization

3. **Check Token Generation:**
```java
// Verify expiration is set correctly
Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);
```

4. **Implement Refresh Tokens:**
- Short-lived access tokens (15 min)
- Long-lived refresh tokens (7 days)
- Automatic token refresh before expiration

5. **Add Token Refresh Logic:**
```javascript
// Frontend: Refresh token before expiration
if (tokenExpiresIn < 5 minutes) {
    refreshAccessToken();
}
```

6. **Add Logging:**
```java
catch (ExpiredJwtException e) {
    logger.warn("Token expired for user: " + email);
    // Return specific error message
    throw new TokenExpiredException("Token expired. Please login again.");
}
```

7. **Better Error Messages:**
```java
catch (ExpiredJwtException e) {
    return ResponseEntity.status(401)
        .body(new ErrorResponse("Token expired", "TOKEN_EXPIRED", "/auth/refresh"));
}
```

---

## **SECTION 7: BEST PRACTICES**

### **Q23: What are the best practices you followed in JWT implementation?**

**Answer:**

**1. Stateless Authentication**
- No server-side sessions
- Scalable across multiple servers
- Each request is independent

**2. Secure Token Storage**
- Tokens stored in memory (not localStorage)
- Transmitted via HTTPS only
- Short expiration time (30 minutes)

**3. Password Security**
- BCrypt hashing (not plain text)
- Password strength validation
- Never log passwords

**4. Role-Based Access Control**
- Centralized in SecurityUtil
- Explicit checks in service layer
- Defense in depth

**5. Error Handling**
- Clear error messages for debugging
- Generic messages for production
- Proper HTTP status codes

**6. Token Validation**
- Signature verification
- Expiration checking
- Format validation

**7. Code Organization**
- Separation of concerns (Filter, Util, Service)
- Reusable SecurityUtil
- Clean service layer

**Improvements Needed:**
- Refresh tokens
- Token blacklisting
- Rate limiting
- Security headers
- Environment-based configuration

---

### **Q24: How would you scale this authentication system for millions of users?**

**Answer:**

**1. Database Optimization:**
```java
// Add index on email for fast lookups
@Column(unique = true)
@Index(name = "idx_user_email")
private String email;

// Cache frequently accessed users
@Cacheable(value = "users", key = "#email")
public Optional<User> findByEmail(String email) { ... }
```

**2. Token Validation Caching:**
```java
// Cache validated tokens (Redis)
@Cacheable(value = "validatedTokens", key = "#token")
public boolean validateToken(String token) { ... }
```

**3. Load Balancing:**
- Stateless design works with load balancers
- No sticky sessions needed
- Any server can validate any token

**4. Database Connection Pooling:**
```properties
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
```

**5. Redis for Session Management:**
- Store refresh tokens in Redis
- Fast lookups
- Distributed across servers

**6. CDN for Static Resources:**
- Offload authentication server
- Faster response times

**7. Microservices Architecture:**
- Separate authentication service
- Other services validate tokens independently
- No shared session storage needed

**8. Rate Limiting:**
```java
@RateLimiter(name = "login", fallbackMethod = "loginFallback")
public AuthResponseDTO login(LoginRequestDTO request) { ... }
```

**9. Monitoring:**
- Track authentication failures
- Monitor token generation/validation times
- Alert on suspicious patterns

---

### **Q25: Explain how you would implement OAuth2 with JWT in this project.**

**Answer:**

**OAuth2 Flow with JWT:**

**1. Add OAuth2 Dependencies:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>
```

**2. Configure OAuth2 Providers:**
```java
@Configuration
public class OAuth2Config {
    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        return new InMemoryClientRegistrationRepository(
            googleClientRegistration(),
            githubClientRegistration()
        );
    }
    
    private ClientRegistration googleClientRegistration() {
        return ClientRegistration.withRegistrationId("google")
            .clientId("google-client-id")
            .clientSecret("google-client-secret")
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
            .scope("openid", "profile", "email")
            .authorizationUri("https://accounts.google.com/o/oauth2/auth")
            .tokenUri("https://www.googleapis.com/oauth2/v3/token")
            .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
            .userNameAttributeName(IdTokenClaimNames.SUB)
            .clientName("Google")
            .build();
    }
}
```

**3. OAuth2 Success Handler:**
```java
@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UserRepo userRepo;
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                       HttpServletResponse response,
                                       Authentication authentication) {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String email = oauth2User.getAttribute("email");
        
        // Find or create user
        User user = userRepo.findByEmail(email)
            .orElseGet(() -> createUserFromOAuth2(oauth2User));
        
        // Generate JWT token
        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        
        // Redirect with token
        response.sendRedirect("/dashboard?token=" + token);
    }
}
```

**4. Security Configuration:**
```java
http.oauth2Login(oauth2 -> oauth2
    .successHandler(oAuth2SuccessHandler)
    .userInfoEndpoint(userInfo -> userInfo
        .userService(customOAuth2UserService)
    )
);
```

**Benefits:**
- Users can login with Google/GitHub/etc.
- No password management
- Still use JWT for API authentication
- Seamless integration

---

## **SUMMARY: KEY POINTS TO REMEMBER**

1. **JWT Structure**: Header.Payload.Signature
2. **Authentication**: Verifies identity (who you are)
3. **Authorization**: Verifies permissions (what you can do)
4. **Stateless**: No server-side sessions
5. **Security**: HTTPS, short expiration, secure storage
6. **Roles**: ADMIN vs USER with different access levels
7. **Filter**: Validates token before request processing
8. **SecurityContext**: Thread-local storage for authenticated user
9. **BCrypt**: One-way password hashing
10. **Defense in Depth**: Multiple security layers

---

**Good luck with your interviews!** 🚀

