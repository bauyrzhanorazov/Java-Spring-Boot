package com.example.javaspring.service.impl;

import com.example.javaspring.dto.request.UserCreateRequest;
import com.example.javaspring.dto.request.UserUpdateRequest;
import com.example.javaspring.dto.response.PageResponse;
import com.example.javaspring.dto.response.UserResponse;
import com.example.javaspring.dto.response.UserSummaryResponse;
import com.example.javaspring.entity.User;
import com.example.javaspring.enums.Role;
import com.example.javaspring.exception.DuplicateResourceException;
import com.example.javaspring.exception.ResourceNotFoundException;
import com.example.javaspring.mapper.UserMapper;
import com.example.javaspring.repository.UserRepository;
import com.example.javaspring.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        log.debug("Creating user with username: {}", request.getUsername());

        validateUserCreation(request);

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(request.getRoles())
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());

        return userMapper.toResponse(savedUser);
    }

    @Override
    public UserResponse getUserById(UUID id) {
        log.debug("Fetching user by ID: {}", id);

        User user = findUserById(id);
        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse getUserByUsername(String username) {
        log.debug("Fetching user by username: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        log.debug("Fetching user by email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        return userMapper.toResponse(user);
    }

    @Override
    public PageResponse<UserResponse> getAllUsers(Pageable pageable) {
        log.debug("Fetching all users with pagination: {}", pageable);

        Page<User> userPage = userRepository.findAll(pageable);
        List<UserResponse> userResponses = userMapper.toResponseList(userPage.getContent());

        return PageResponse.<UserResponse>builder()
                .content(userResponses)
                .page(userPage.getNumber())
                .size(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .first(userPage.isFirst())
                .last(userPage.isLast())
                .hasNext(userPage.hasNext())
                .hasPrevious(userPage.hasPrevious())
                .build();
    }

    @Override
    @Transactional
    public UserResponse updateUser(UUID id, UserUpdateRequest request) {
        log.debug("Updating user with ID: {}", id);

        User user = findUserById(id);

        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            validateUsernameUniqueness(request.getUsername());
            user.setUsername(request.getUsername());
        }

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            validateEmailUniqueness(request.getEmail());
            user.setEmail(request.getEmail());
        }

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }

        if (request.getRoles() != null) {
            user.setRoles(request.getRoles());
        }

        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }

        User updatedUser = userRepository.save(user);
        log.info("User updated successfully with ID: {}", updatedUser.getId());

        return userMapper.toResponse(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(UUID id) {
        log.debug("Deleting user with ID: {}", id);

        User user = findUserById(id);
        userRepository.delete(user);

        log.info("User deleted successfully with ID: {}", id);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean isUserEnabled(UUID id) {
        User user = findUserById(id);
        return user.getEnabled();
    }

    @Override
    @Transactional
    public UserResponse addRole(UUID userId, Role role) {
        log.debug("Adding role {} to user {}", role, userId);

        User user = findUserById(userId);
        user.getRoles().add(role);

        User updatedUser = userRepository.save(user);
        log.info("Role {} added to user {}", role, userId);

        return userMapper.toResponse(updatedUser);
    }

    @Override
    @Transactional
    public UserResponse removeRole(UUID userId, Role role) {
        log.debug("Removing role {} from user {}", role, userId);

        User user = findUserById(userId);
        user.getRoles().remove(role);

        User updatedUser = userRepository.save(user);
        log.info("Role {} removed from user {}", role, userId);

        return userMapper.toResponse(updatedUser);
    }

    @Override
    public List<UserResponse> getUsersByRole(Role role) {
        log.debug("Fetching users by role: {}", role);

        List<User> users = userRepository.findByRole(role);
        return userMapper.toResponseList(users);
    }

    @Override
    public List<UserResponse> getUsersByRoles(Set<Role> roles) {
        log.debug("Fetching users by roles: {}", roles);

        List<User> users = userRepository.findByRolesIn(roles);
        return userMapper.toResponseList(users);
    }

    @Override
    public List<UserSummaryResponse> getUsersByProjectId(UUID projectId) {
        log.debug("Fetching users by project ID: {}", projectId);

        List<User> users = userRepository.findByProjectId(projectId);
        return userMapper.toSummaryResponseList(users);
    }

    @Override
    public List<UserSummaryResponse> getAssigneesByProjectId(UUID projectId) {
        log.debug("Fetching assignees by project ID: {}", projectId);

        List<User> users = userRepository.findAssigneesByProjectId(projectId);
        return userMapper.toSummaryResponseList(users);
    }

    @Override
    public List<UserResponse> searchUsers(String search) {
        log.debug("Searching users with query: {}", search);

        List<User> users = userRepository.searchUsers(search);
        return userMapper.toResponseList(users);
    }

    @Override
    public List<UserResponse> getEnabledUsers() {
        log.debug("Fetching enabled users");

        List<User> users = userRepository.findByEnabledTrue();
        return userMapper.toResponseList(users);
    }

    @Override
    public List<UserResponse> getDisabledUsers() {
        log.debug("Fetching disabled users");

        List<User> users = userRepository.findByEnabledFalse();
        return userMapper.toResponseList(users);
    }

    @Override
    public long getTotalUsersCount() {
        return userRepository.count();
    }

    @Override
    public long getUsersCreatedAfterCount(LocalDateTime date) {
        return userRepository.countUsersCreatedAfter(date);
    }

    @Override
    public long getUsersWithActiveTasksCount() {
        return userRepository.countUsersWithActiveTasks();
    }

    @Override
    public long getUsersByRoleCount(Role role) {
        return userRepository.countByRole(role);
    }

    private User findUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
    }

    private void validateUserCreation(UserCreateRequest request) {
        validateUsernameUniqueness(request.getUsername());
        validateEmailUniqueness(request.getEmail());
    }

    private void validateUsernameUniqueness(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("Username already exists: " + username);
        }
    }

    private void validateEmailUniqueness(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email already exists: " + email);
        }
    }
}