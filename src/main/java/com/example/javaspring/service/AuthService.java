package com.example.javaspring.service;

import com.example.javaspring.dto.request.LoginRequest;
import com.example.javaspring.dto.request.RefreshTokenRequest;
import com.example.javaspring.dto.request.UserCreateRequest;
import com.example.javaspring.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse login(LoginRequest request);

    AuthResponse register(UserCreateRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    void logout(String token);

    boolean validateToken(String token);

    String extractUsernameFromToken(String token);

    boolean isTokenExpired(String token);

    boolean invalidateToken(String token);

    void changePassword(String username, String oldPassword, String newPassword);

    void resetPassword(String email);

    boolean verifyPassword(String username, String password);

    void enableAccount(String username);

    void disableAccount(String username);

    boolean isAccountEnabled(String username);

    void lockAccount(String username);

    void unlockAccount(String username);

    boolean isAccountLocked(String username);

    int getFailedAttempts(String username);

    void resetFailedAttempts(String username);
}