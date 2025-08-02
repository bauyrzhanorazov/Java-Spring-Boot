package com.example.javaspring.service;

import com.example.javaspring.dto.request.TaskCreateRequest;
import com.example.javaspring.dto.request.TaskUpdateRequest;
import com.example.javaspring.dto.response.PageResponse;
import com.example.javaspring.dto.response.TaskResponse;
import com.example.javaspring.dto.response.TaskSummaryResponse;
import com.example.javaspring.enums.TaskPriority;
import com.example.javaspring.enums.TaskStatus;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TaskService {

    TaskResponse createTask(TaskCreateRequest request, UUID reporterId);

    TaskResponse getTaskById(UUID id);

    PageResponse<TaskResponse> getAllTasks(Pageable pageable);

    TaskResponse updateTask(UUID id, TaskUpdateRequest request);

    void deleteTask(UUID id);

    List<TaskResponse> getTasksByProject(UUID projectId);

    List<TaskResponse> getTasksByProjectAndStatus(UUID projectId, TaskStatus status);

    PageResponse<TaskResponse> getTasksByProject(UUID projectId, Pageable pageable);

    List<TaskResponse> getTasksByAssignee(UUID assigneeId);

    List<TaskResponse> getTasksByAssigneeAndStatus(UUID assigneeId, TaskStatus status);

    List<TaskResponse> getActiveTasksByAssignee(UUID assigneeId);

    TaskResponse assignTask(UUID taskId, UUID assigneeId);

    TaskResponse unassignTask(UUID taskId);

    List<TaskResponse> getTasksByStatus(TaskStatus status);

    TaskResponse updateTaskStatus(UUID id, TaskStatus status);

    List<TaskResponse> getInProgressTasksByAssignee(UUID assigneeId);

    List<TaskResponse> getTasksByPriority(TaskPriority priority);

    TaskResponse updateTaskPriority(UUID id, TaskPriority priority);

    List<TaskResponse> getOverdueTasks();

    List<TaskResponse> getTasksDueSoon(LocalDateTime start, LocalDateTime end);

    List<TaskResponse> getOverdueTasksByAssignee(UUID assigneeId);

    List<TaskResponse> getTasksWithDueDateBefore(LocalDateTime dueDate);

    List<TaskResponse> getTasksWithDueDateBetween(LocalDateTime start, LocalDateTime end);

    List<TaskResponse> searchTasks(String search);

    List<TaskResponse> searchTasksInProject(UUID projectId, String search);

    List<TaskResponse> getTasksByProjectAndAssignee(UUID projectId, UUID assigneeId);

    List<TaskResponse> getTasksByProjectStatusAndPriority(UUID projectId, TaskStatus status, TaskPriority priority);

    List<TaskSummaryResponse> getRecentTasksByAssignee(UUID assigneeId);

    List<TaskSummaryResponse> getRecentTasksByProject(UUID projectId);

    List<TaskResponse> getActiveTasksByAssigneeOrderedByPriority(UUID assigneeId);

    long getTotalTasksCount();

    long getTasksByStatusCount(TaskStatus status);

    long getTasksByAssigneeAndStatusCount(UUID assigneeId, TaskStatus status);

    long getActiveTasksByAssigneeCount(UUID assigneeId);

    List<Object[]> getTaskStatusStatisticsByProject(UUID projectId);

    List<Object[]> getTaskPriorityStatisticsByProject(UUID projectId);

    List<Object[]> getAssigneeWorkloadByProject(UUID projectId);

    long getCommentsCountByTask(UUID taskId);
}