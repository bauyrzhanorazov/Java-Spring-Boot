package com.example.javaspring.service;

import com.example.javaspring.dto.request.UserCreateRequest;
import com.example.javaspring.dto.request.UserUpdateRequest;
import com.example.javaspring.dto.response.PageResponse;
import com.example.javaspring.dto.response.UserResponse;
import com.example.javaspring.dto.response.UserSummaryResponse;
import com.example.javaspring.enums.Role;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface UserService {

    UserResponse createUser(UserCreateRequest request);

    UserResponse getUserById(UUID id);

    UserResponse getUserByUsername(String username);

    UserResponse getUserByEmail(String email);

    PageResponse<UserResponse> getAllUsers(Pageable pageable);

    UserResponse updateUser(UUID id, UserUpdateRequest request);

    void deleteUser(UUID id);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean isUserEnabled(UUID id);

    UserResponse addRole(UUID userId, Role role);

    UserResponse removeRole(UUID userId, Role role);

    List<UserResponse> getUsersByRole(Role role);

    List<UserResponse> getUsersByRoles(Set<Role> roles);

    List<UserSummaryResponse> getUsersByProjectId(UUID projectId);

    List<UserSummaryResponse> getAssigneesByProjectId(UUID projectId);

    List<UserResponse> searchUsers(String search);

    List<UserResponse> getEnabledUsers();

    List<UserResponse> getDisabledUsers();

    long getTotalUsersCount();

    long getUsersCreatedAfterCount(java.time.LocalDateTime date);

    long getUsersWithActiveTasksCount();

    long getUsersByRoleCount(Role role);
}