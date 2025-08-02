package com.example.javaspring.repository;

import com.example.javaspring.entity.Project;
import com.example.javaspring.enums.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectRepository extends BaseRepository<Project> {

    List<Project> findByStatus(ProjectStatus status);

    List<Project> findByOwnerId(UUID ownerId);

    Page<Project> findByOwnerId(UUID ownerId, Pageable pageable);

    Page<Project> findByStatus(ProjectStatus status, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Project p JOIN p.members m WHERE m.id = :userId")
    List<Project> findByMemberId(@Param("userId") UUID userId);

    @Query("SELECT p FROM Project p WHERE p.owner.id = :userId OR p.id IN " +
            "(SELECT DISTINCT pm.id FROM Project pm JOIN pm.members m WHERE m.id = :userId)")
    List<Project> findByUserInvolved(@Param("userId") UUID userId);

    List<Project> findByDeadlineBefore(LocalDateTime deadline);

    List<Project> findByDeadlineBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT p FROM Project p WHERE p.deadline IS NOT NULL AND p.deadline <= :date AND p.status = 'ACTIVE'")
    List<Project> findOverdueProjects(@Param("date") LocalDateTime date);

    @Query("SELECT p FROM Project p WHERE p.deadline IS NOT NULL AND p.deadline BETWEEN :start AND :end AND p.status = 'ACTIVE'")
    List<Project> findProjectsDueSoon(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT p FROM Project p WHERE " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Project> searchProjects(@Param("search") String search);

    @Query("SELECT COUNT(p) FROM Project p WHERE p.status = :status")
    long countByStatus(@Param("status") ProjectStatus status);

    @Query("SELECT p.status, COUNT(p) FROM Project p GROUP BY p.status")
    List<Object[]> getProjectStatusStatistics();

    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.id = :projectId")
    long countTasksByProjectId(@Param("projectId") UUID projectId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.id = :projectId AND t.status = 'DONE'")
    long countCompletedTasksByProjectId(@Param("projectId") UUID projectId);

    @Query("SELECT p FROM Project p WHERE SIZE(p.tasks) > :taskCount")
    List<Project> findProjectsWithMoreTasks(@Param("taskCount") int taskCount);

    @Query("SELECT COUNT(m) FROM Project p JOIN p.members m WHERE p.id = :projectId")
    long countMembersByProjectId(@Param("projectId") UUID projectId);

    @Query("SELECT p FROM Project p WHERE SIZE(p.members) >= :memberCount")
    List<Project> findProjectsWithMinMembers(@Param("memberCount") int memberCount);

    @Query("SELECT p FROM Project p WHERE p.owner.id = :userId ORDER BY p.updatedAt DESC")
    List<Project> findRecentProjectsByOwner(@Param("userId") UUID userId);

    @Query("SELECT p FROM Project p JOIN p.members m WHERE m.id = :userId AND p.status = 'ACTIVE' ORDER BY p.deadline ASC")
    List<Project> findActiveProjectsByMember(@Param("userId") UUID userId);
}