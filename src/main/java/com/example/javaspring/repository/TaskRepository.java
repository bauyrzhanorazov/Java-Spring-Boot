package com.example.javaspring.repository;

import com.example.javaspring.entity.Task;
import com.example.javaspring.enums.TaskPriority;
import com.example.javaspring.enums.TaskStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends BaseRepository<Task> {

    // ========== BASIC QUERIES ==========

    List<Task> findByProjectId(UUID projectId);

    List<Task> findByAssigneeId(UUID assigneeId);

    List<Task> findByReporterId(UUID reporterId);

    List<Task> findByStatus(TaskStatus status);

    List<Task> findByPriority(TaskPriority priority);

    // ========== COMBINED QUERIES ==========

    List<Task> findByProjectIdAndStatus(UUID projectId, TaskStatus status);

    List<Task> findByAssigneeIdAndStatus(UUID assigneeId, TaskStatus status);

    List<Task> findByProjectIdAndAssigneeId(UUID projectId, UUID assigneeId);

    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND t.status = :status AND t.priority = :priority")
    List<Task> findByProjectIdAndStatusAndPriority(
            @Param("projectId") UUID projectId,
            @Param("status") TaskStatus status,
            @Param("priority") TaskPriority priority
    );

    // ========== DUE DATE QUERIES ==========

    List<Task> findByDueDateBefore(LocalDateTime dueDate);

    List<Task> findByDueDateBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT t FROM Task t WHERE t.dueDate IS NOT NULL AND t.dueDate <= :date AND t.status NOT IN ('DONE', 'CANCELLED')")
    List<Task> findOverdueTasks(@Param("date") LocalDateTime date);

    @Query("SELECT t FROM Task t WHERE t.dueDate IS NOT NULL AND t.dueDate BETWEEN :start AND :end AND t.status NOT IN ('DONE', 'CANCELLED')")
    List<Task> findTasksDueSoon(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT t FROM Task t WHERE t.assignee.id = :assigneeId AND t.dueDate IS NOT NULL AND t.dueDate <= :date AND t.status NOT IN ('DONE', 'CANCELLED')")
    List<Task> findOverdueTasksByAssignee(@Param("assigneeId") UUID assigneeId, @Param("date") LocalDateTime date);

    // ========== SEARCH QUERIES ==========

    @Query("SELECT t FROM Task t WHERE " +
            "LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Task> searchTasks(@Param("search") String search);

    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND (" +
            "LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Task> searchTasksInProject(@Param("projectId") UUID projectId, @Param("search") String search);

    // ========== STATISTICS ==========

    @Query("SELECT COUNT(t) FROM Task t WHERE t.status = :status")
    long countByStatus(@Param("status") TaskStatus status);

    @Query("SELECT t.status, COUNT(t) FROM Task t WHERE t.project.id = :projectId GROUP BY t.status")
    List<Object[]> getTaskStatusStatisticsByProject(@Param("projectId") UUID projectId);

    @Query("SELECT t.priority, COUNT(t) FROM Task t WHERE t.project.id = :projectId GROUP BY t.priority")
    List<Object[]> getTaskPriorityStatisticsByProject(@Param("projectId") UUID projectId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignee.id = :assigneeId AND t.status = :status")
    long countByAssigneeAndStatus(@Param("assigneeId") UUID assigneeId, @Param("status") TaskStatus status);

    // ========== ASSIGNEE WORKLOAD ==========

    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignee.id = :assigneeId AND t.status IN ('TODO', 'IN_PROGRESS', 'IN_REVIEW')")
    long countActiveTasksByAssignee(@Param("assigneeId") UUID assigneeId);

    @Query("SELECT t.assignee.id, COUNT(t) FROM Task t WHERE t.project.id = :projectId AND t.status IN ('TODO', 'IN_PROGRESS', 'IN_REVIEW') GROUP BY t.assignee.id")
    List<Object[]> getAssigneeWorkloadByProject(@Param("projectId") UUID projectId);

    @Query("SELECT t FROM Task t WHERE t.assignee.id = :assigneeId AND t.status IN ('TODO', 'IN_PROGRESS') ORDER BY t.priority DESC, t.dueDate ASC")
    List<Task> findActiveTasksByAssigneeOrderedByPriority(@Param("assigneeId") UUID assigneeId);

    // ========== DASHBOARD QUERIES ==========

    @Query("SELECT t FROM Task t WHERE t.assignee.id = :assigneeId ORDER BY t.updatedAt DESC")
    List<Task> findRecentTasksByAssignee(@Param("assigneeId") UUID assigneeId);

    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId ORDER BY t.createdAt DESC")
    List<Task> findRecentTasksByProject(@Param("projectId") UUID projectId);

    @Query("SELECT t FROM Task t WHERE t.status = 'IN_PROGRESS' AND t.assignee.id = :assigneeId ORDER BY t.priority DESC")
    List<Task> findInProgressTasksByAssignee(@Param("assigneeId") UUID assigneeId);

    // ========== COMMENTS ==========

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.task.id = :taskId")
    long countCommentsByTaskId(@Param("taskId") UUID taskId);
}