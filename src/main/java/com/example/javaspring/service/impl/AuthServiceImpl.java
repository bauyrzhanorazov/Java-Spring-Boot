package com.example.javaspring.service.impl;

import com.example.javaspring.dto.request.LoginRequest;
import com.example.javaspring.dto.request.RefreshTokenRequest;
import com.example.javaspring.dto.request.UserCreateRequest;
import com.example.javaspring.dto.response.AuthResponse;
import com.example.javaspring.entity.User;
import com.example.javaspring.enums.Role;
import com.example.javaspring.exception.AuthenticationException;
import com.example.javaspring.exception.ResourceNotFoundException;
import com.example.javaspring.exception.TokenException;
import com.example.javaspring.mapper.AuthMapper;
import com.example.javaspring.repository.UserRepository;
import com.example.javaspring.service.AuthService;
import com.example.javaspring.service.UserService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final AuthMapper authMapper;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;

    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();
    private final Map<String, Integer> failedAttempts = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> lockedAccounts = new ConcurrentHashMap<>();

    @Value("${app.jwt.secret:taskflow-secret-key-very-long-for-security}")
    private String jwtSecret;

    @Value("${app.jwt.expiration:86400000}") // 24 hours
    private Long jwtExpirationMs;

    @Value("${app.jwt.refresh-expiration:604800000}") // 7 days
    private Long refreshExpirationMs;

    private static final String BLACKLIST_PREFIX = "blacklisted_token:";
    private static final String FAILED_ATTEMPTS_PREFIX = "failed_attempts:";
    private static final String LOCKED_ACCOUNT_PREFIX = "locked_account:";
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCK_DURATION_MINUTES = 30;

    // ========== AUTHENTICATION ==========

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.debug("Attempting login for username: {}", request.getUsername());

        // Check if account is locked
        if (isAccountLocked(request.getUsername())) {
            throw new AuthenticationException("Account is temporarily locked due to multiple failed attempts");
        }

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    incrementFailedAttempts(request.getUsername());
                    return new AuthenticationException("Invalid username or password");
                });

        if (!user.getEnabled()) {
            throw new AuthenticationException("Account is disabled");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            incrementFailedAttempts(request.getUsername());
            throw new AuthenticationException("Invalid username or password");
        }

        // Reset failed attempts on successful login
        resetFailedAttempts(request.getUsername());

        String accessToken = generateAccessToken(user);
        String refreshToken = generateRefreshToken(user);

        log.info("User {} logged in successfully", user.getUsername());

        return authMapper.toAuthResponse(accessToken, refreshToken, jwtExpirationMs, user);
    }

    @Override
    @Transactional
    public AuthResponse register(UserCreateRequest request) {
        log.debug("Registering new user: {}", request.getUsername());

        // Set default role if not provided
        if (request.getRoles() == null || request.getRoles().isEmpty()) {
            request.setRoles(Set.of(Role.USER));
        }

        // Create user through UserService
        var userResponse = userService.createUser(request);

        // Get the created user for token generation
        User user = userRepository.findById(userResponse.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found after creation"));

        String accessToken = generateAccessToken(user);
        String refreshToken = generateRefreshToken(user);

        log.info("User {} registered and logged in successfully", user.getUsername());

        return authMapper.toAuthResponse(accessToken, refreshToken, jwtExpirationMs, user);
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        log.debug("Attempting to refresh token");

        String refreshToken = request.getRefreshToken();

        if (!validateToken(refreshToken)) {
            throw new TokenException("Invalid refresh token");
        }

        String username = extractUsernameFromToken(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        if (!user.getEnabled()) {
            throw new AuthenticationException("Account is disabled");
        }

        String newAccessToken = generateAccessToken(user);
        String newRefreshToken = generateRefreshToken(user);

        // Invalidate old refresh token
        invalidateToken(refreshToken);

        log.info("Token refreshed successfully for user: {}", username);

        return authMapper.toAuthResponse(newAccessToken, newRefreshToken, jwtExpirationMs, user);
    }

    @Override
    public void logout(String token) {
        log.debug("Logging out user");

        if (token != null && !token.isEmpty()) {
            invalidateToken(token);
            log.info("User logged out successfully");
        }
    }

    // ========== TOKEN MANAGEMENT ==========

    @Override
    public boolean validateToken(String token) {
        try {
            // Check if token is blacklisted
            if (isTokenBlacklisted(token)) {
                return false;
            }

            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);

            return true;
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }

        return false;
    }

    @Override
    public String extractUsernameFromToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getSubject();
        } catch (Exception e) {
            throw new TokenException("Failed to extract username from token", e);
        }
    }

    @Override
    public boolean isTokenExpired(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            return true;
        }
    }

    @Override
    public boolean invalidateToken(String token) {
        return blacklistedTokens.contains(token);
    }

    @Override
    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        log.debug("Changing password for user: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new AuthenticationException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("Password changed successfully for user: {}", username);
    }

    @Override
    @Transactional
    public void resetPassword(String email) {
        log.debug("Resetting password for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        // Generate temporary password (in real app, send email with reset link)
        String tempPassword = generateTemporaryPassword();
        user.setPassword(passwordEncoder.encode(tempPassword));
        userRepository.save(user);

        // In real application, send email with temporary password or reset link
        log.info("Password reset for user: {} (temp password: {})", user.getUsername(), tempPassword);
    }

    @Override
    public boolean verifyPassword(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        return passwordEncoder.matches(password, user.getPassword());
    }

    // ========== ACCOUNT MANAGEMENT ==========

    @Override
    @Transactional
    public void enableAccount(String username) {
        log.debug("Enabling account: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        user.setEnabled(true);
        userRepository.save(user);

        log.info("Account enabled: {}", username);
    }

    @Override
    @Transactional
    public void disableAccount(String username) {
        log.debug("Disabling account: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        user.setEnabled(false);
        userRepository.save(user);

        log.info("Account disabled: {}", username);
    }

    @Override
    public boolean isAccountEnabled(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        return user.getEnabled();
    }

    // ========== SECURITY ==========

    @Override
    public void lockAccount(String username) {
        log.debug("Locking account: {}", username);

        redisTemplate.opsForValue().set(
                LOCKED_ACCOUNT_PREFIX + username,
                LocalDateTime.now().toString(),
                LOCK_DURATION_MINUTES,
                TimeUnit.MINUTES
        );

        log.info("Account locked for {} minutes: {}", LOCK_DURATION_MINUTES, username);
    }

    @Override
    public void unlockAccount(String username) {
        log.debug("Unlocking account: {}", username);

        redisTemplate.delete(LOCKED_ACCOUNT_PREFIX + username);
        resetFailedAttempts(username);

        log.info("Account unlocked: {}", username);
    }

    @Override
    public boolean isAccountLocked(String username) {
        return redisTemplate.hasKey(LOCKED_ACCOUNT_PREFIX + username);
    }

    @Override
    public int getFailedAttempts(String username) {
        Object attempts = redisTemplate.opsForValue().get(FAILED_ATTEMPTS_PREFIX + username);
        return attempts != null ? Integer.parseInt(attempts.toString()) : 0;
    }

    @Override
    public void resetFailedAttempts(String username) {
        redisTemplate.delete(FAILED_ATTEMPTS_PREFIX + username);
    }

    // ========== PRIVATE HELPER METHODS ==========

    private String generateAccessToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

        return Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .claim("userId", user.getId().toString())
                .claim("email", user.getEmail())
                .claim("roles", user.getRoles())
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    private String generateRefreshToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshExpirationMs);

        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

        return Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .claim("type", "refresh")
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    private boolean isTokenBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }

    private long getTokenExpiration(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Date expiration = claims.getExpiration();
            return expiration.getTime() - System.currentTimeMillis();
        } catch (Exception e) {
            return 0;
        }
    }

    private void incrementFailedAttempts(String username) {
        String key = FAILED_ATTEMPTS_PREFIX + username;
        int attempts = getFailedAttempts(username) + 1;

        redisTemplate.opsForValue().set(key, attempts, 24, TimeUnit.HOURS);

        if (attempts >= MAX_FAILED_ATTEMPTS) {
            lockAccount(username);
        }

        log.warn("Failed login attempt {} for user: {}", attempts, username);
    }

    private String generateTemporaryPassword() {
        // Simple temporary password generator
        return "TempPass" + System.currentTimeMillis() % 10000;
    }
}