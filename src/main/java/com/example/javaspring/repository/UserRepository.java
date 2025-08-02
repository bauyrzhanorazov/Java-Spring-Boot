package com.example.javaspring.repository;

import com.example.javaspring.entity.User;
import com.example.javaspring.enums.Role;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface UserRepository extends BaseRepository<User> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<User> findByEnabledTrue();

    List<User> findByEnabledFalse();

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r IN :roles")
    List<User> findByRolesIn(@Param("roles") Set<Role> roles);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r = :role")
    List<User> findByRole(@Param("role") Role role);

    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r = :role")
    long countByRole(@Param("role") Role role);

    @Query("SELECT DISTINCT u FROM User u JOIN u.memberProjects p WHERE p.id = :projectId")
    List<User> findByProjectId(@Param("projectId") UUID projectId);

    @Query("SELECT u FROM User u WHERE u.id IN " +
            "(SELECT DISTINCT t.assignee.id FROM Task t WHERE t.project.id = :projectId)")
    List<User> findAssigneesByProjectId(@Param("projectId") UUID projectId);

    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<User> searchUsers(@Param("search") String search);

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :date")
    long countUsersCreatedAfter(@Param("date") LocalDateTime date);

    @Query("SELECT COUNT(DISTINCT u) FROM User u JOIN u.assignedTasks t WHERE t.status = 'IN_PROGRESS'")
    long countUsersWithActiveTasks();

    @Query("SELECT u.id, u.username, u.email, u.firstName, u.lastName FROM User u WHERE u.enabled = true")
    List<Object[]> findUserSummaries();
}