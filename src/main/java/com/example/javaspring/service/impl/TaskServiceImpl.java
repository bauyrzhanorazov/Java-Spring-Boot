package com.example.javaspring.service.impl;

import com.example.javaspring.dto.request.TaskCreateRequest;
import com.example.javaspring.dto.request.TaskUpdateRequest;
import com.example.javaspring.dto.response.PageResponse;
import com.example.javaspring.dto.response.TaskResponse;
import com.example.javaspring.dto.response.TaskSummaryResponse;
import com.example.javaspring.entity.Project;
import com.example.javaspring.entity.Task;
import com.example.javaspring.entity.User;
import com.example.javaspring.enums.TaskPriority;
import com.example.javaspring.enums.TaskStatus;
import com.example.javaspring.exception.BusinessLogicException;
import com.example.javaspring.exception.ResourceNotFoundException;
import com.example.javaspring.mapper.TaskMapper;
import com.example.javaspring.repository.ProjectRepository;
import com.example.javaspring.repository.TaskRepository;
import com.example.javaspring.repository.UserRepository;
import com.example.javaspring.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;

    @Override
    @Transactional
    public TaskResponse createTask(TaskCreateRequest request, UUID reporterId) {
        log.debug("Creating task: {} for project: {} by reporter: {}",
                request.getTitle(), request.getProjectId(), reporterId);

        Project project = findProjectById(request.getProjectId());
        User reporter = findUserById(reporterId);

        // Validate reporter can create tasks in this project
        validateUserCanAccessProject(reporter, project);

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : TaskStatus.TODO)
                .priority(request.getPriority() != null ? request.getPriority() : TaskPriority.MEDIUM)
                .dueDate(request.getDueDate())
                .project(project)
                .reporter(reporter)
                .build();

        // Assign task if assignee is provided
        if (request.getAssigneeId() != null) {
            User assignee = findUserById(request.getAssigneeId());
            validateUserCanAccessProject(assignee, project);
            task.setAssignee(assignee);
        }

        Task savedTask = taskRepository.save(task);
        log.info("Task created successfully with ID: {}", savedTask.getId());

        return taskMapper.toResponse(savedTask);
    }

    @Override
    public TaskResponse getTaskById(UUID id) {
        log.debug("Fetching task by ID: {}", id);

        Task task = findTaskById(id);
        return taskMapper.toResponse(task);
    }

    @Override
    public PageResponse<TaskResponse> getAllTasks(Pageable pageable) {
        log.debug("Fetching all tasks with pagination: {}", pageable);

        Page<Task> taskPage = taskRepository.findAll(pageable);
        List<TaskResponse> taskResponses = taskMapper.toResponseList(taskPage.getContent());

        return buildPageResponse(taskPage, taskResponses);
    }

    @Override
    @Transactional
    public TaskResponse updateTask(UUID id, TaskUpdateRequest request) {
        log.debug("Updating task with ID: {}", id);

        Task task = findTaskById(id);

        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }

        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }

        if (request.getStatus() != null) {
            validateStatusTransition(task.getStatus(), request.getStatus());
            task.setStatus(request.getStatus());
        }

        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }

        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }

        if (request.getAssigneeId() != null) {
            User assignee = findUserById(request.getAssigneeId());
            validateUserCanAccessProject(assignee, task.getProject());
            task.setAssignee(assignee);
        }

        Task updatedTask = taskRepository.save(task);
        log.info("Task updated successfully with ID: {}", updatedTask.getId());

        return taskMapper.toResponse(updatedTask);
    }

    @Override
    @Transactional
    public void deleteTask(UUID id) {
        log.debug("Deleting task with ID: {}", id);

        Task task = findTaskById(id);

        // Validate task can be deleted
        if (task.getStatus() == TaskStatus.IN_PROGRESS) {
            throw new BusinessLogicException("delete task", "task is currently in progress");
        }

        taskRepository.delete(task);
        log.info("Task deleted successfully with ID: {}", id);
    }

    @Override
    public List<TaskResponse> getTasksByProject(UUID projectId) {
        log.debug("Fetching tasks by project ID: {}", projectId);

        List<Task> tasks = taskRepository.findByProjectId(projectId);
        return taskMapper.toResponseList(tasks);
    }

    @Override
    public List<TaskResponse> getTasksByProjectAndStatus(UUID projectId, TaskStatus status) {
        log.debug("Fetching tasks by project {} and status {}", projectId, status);

        List<Task> tasks = taskRepository.findByProjectIdAndStatus(projectId, status);
        return taskMapper.toResponseList(tasks);
    }

    @Override
    public PageResponse<TaskResponse> getTasksByProject(UUID projectId, Pageable pageable) {
        log.debug("Fetching tasks by project ID {} with pagination: {}", projectId, pageable);

        Page<Task> taskPage = taskRepository.findByProjectId(projectId, pageable);
        List<TaskResponse> taskResponses = taskMapper.toResponseList(taskPage.getContent());

        return buildPageResponse(taskPage, taskResponses);
    }

    @Override
    public List<TaskResponse> getTasksByAssignee(UUID assigneeId) {
        log.debug("Fetching tasks by assignee ID: {}", assigneeId);

        List<Task> tasks = taskRepository.findByAssigneeId(assigneeId);
        return taskMapper.toResponseList(tasks);
    }

    @Override
    public List<TaskResponse> getTasksByAssigneeAndStatus(UUID assigneeId, TaskStatus status) {
        log.debug("Fetching tasks by assignee {} and status {}", assigneeId, status);

        List<Task> tasks = taskRepository.findByAssigneeIdAndStatus(assigneeId, status);
        return taskMapper.toResponseList(tasks);
    }

    @Override
    public List<TaskResponse> getActiveTasksByAssignee(UUID assigneeId) {
        log.debug("Fetching active tasks by assignee: {}", assigneeId);

        List<Task> tasks = taskRepository.findActiveTasksByAssigneeOrderedByPriority(assigneeId);
        return taskMapper.toResponseList(tasks);
    }

    @Override
    @Transactional
    public TaskResponse assignTask(UUID taskId, UUID assigneeId) {
        log.debug("Assigning task {} to user {}", taskId, assigneeId);

        Task task = findTaskById(taskId);
        User assignee = findUserById(assigneeId);

        validateUserCanAccessProject(assignee, task.getProject());

        task.setAssignee(assignee);
        Task updatedTask = taskRepository.save(task);

        log.info("Task {} assigned to user {}", taskId, assigneeId);
        return taskMapper.toResponse(updatedTask);
    }

    @Override
    @Transactional
    public TaskResponse unassignTask(UUID taskId) {
        log.debug("Unassigning task {}", taskId);

        Task task = findTaskById(taskId);
        task.setAssignee(null);

        Task updatedTask = taskRepository.save(task);
        log.info("Task {} unassigned", taskId);

        return taskMapper.toResponse(updatedTask);
    }

    @Override
    public List<TaskResponse> getTasksByStatus(TaskStatus status) {
        log.debug("Fetching tasks by status: {}", status);

        List<Task> tasks = taskRepository.findByStatus(status);
        return taskMapper.toResponseList(tasks);
    }

    @Override
    @Transactional
    public TaskResponse updateTaskStatus(UUID id, TaskStatus status) {
        log.debug("Updating task {} status to {}", id, status);

        Task task = findTaskById(id);
        validateStatusTransition(task.getStatus(), status);

        task.setStatus(status);
        Task updatedTask = taskRepository.save(task);

        log.info("Task {} status updated to {}", id, status);
        return taskMapper.toResponse(updatedTask);
    }

    @Override
    public List<TaskResponse> getInProgressTasksByAssignee(UUID assigneeId) {
        log.debug("Fetching in-progress tasks by assignee: {}", assigneeId);

        List<Task> tasks = taskRepository.findInProgressTasksByAssignee(assigneeId);
        return taskMapper.toResponseList(tasks);
    }

    @Override
    public List<TaskResponse> getTasksByPriority(TaskPriority priority) {
        log.debug("Fetching tasks by priority: {}", priority);

        List<Task> tasks = taskRepository.findByPriority(priority);
        return taskMapper.toResponseList(tasks);
    }

    @Override
    @Transactional
    public TaskResponse updateTaskPriority(UUID id, TaskPriority priority) {
        log.debug("Updating task {} priority to {}", id, priority);

        Task task = findTaskById(id);
        task.setPriority(priority);

        Task updatedTask = taskRepository.save(task);
        log.info("Task {} priority updated to {}", id, priority);

        return taskMapper.toResponse(updatedTask);
    }

    @Override
    public List<TaskResponse> getOverdueTasks() {
        log.debug("Fetching overdue tasks");

        List<Task> tasks = taskRepository.findOverdueTasks(LocalDateTime.now());
        return taskMapper.toResponseList(tasks);
    }

    @Override
    public List<TaskResponse> getTasksDueSoon(LocalDateTime start, LocalDateTime end) {
        log.debug("Fetching tasks due between {} and {}", start, end);

        List<Task> tasks = taskRepository.findTasksDueSoon(start, end);
        return taskMapper.toResponseList(tasks);
    }

    @Override
    public List<TaskResponse> getOverdueTasksByAssignee(UUID assigneeId) {
        log.debug("Fetching overdue tasks by assignee: {}", assigneeId);

        List<Task> tasks = taskRepository.findOverdueTasksByAssignee(assigneeId, LocalDateTime.now());
        return taskMapper.toResponseList(tasks);
    }

    @Override
    public List<TaskResponse> getTasksWithDueDateBefore(LocalDateTime dueDate) {
        log.debug("Fetching tasks with due date before: {}", dueDate);

        List<Task> tasks = taskRepository.findByDueDateBefore(dueDate);
        return taskMapper.toResponseList(tasks);
    }

    @Override
    public List<TaskResponse> getTasksWithDueDateBetween(LocalDateTime start, LocalDateTime end) {
        log.debug("Fetching tasks with due date between {} and {}", start, end);

        List<Task> tasks = taskRepository.findByDueDateBetween(start, end);
        return taskMapper.toResponseList(tasks);
    }

    @Override
    public List<TaskResponse> searchTasks(String search) {
        log.debug("Searching tasks with query: {}", search);

        List<Task> tasks = taskRepository.searchTasks(search);
        return taskMapper.toResponseList(tasks);
    }

    @Override
    public List<TaskResponse> searchTasksInProject(UUID projectId, String search) {
        log.debug("Searching tasks in project {} with query: {}", projectId, search);

        List<Task> tasks = taskRepository.searchTasksInProject(projectId, search);
        return taskMapper.toResponseList(tasks);
    }

    @Override
    public List<TaskResponse> getTasksByProjectAndAssignee(UUID projectId, UUID assigneeId) {
        log.debug("Fetching tasks by project {} and assignee {}", projectId, assigneeId);

        List<Task> tasks = taskRepository.findByProjectIdAndAssigneeId(projectId, assigneeId);
        return taskMapper.toResponseList(tasks);
    }

    @Override
    public List<TaskResponse> getTasksByProjectStatusAndPriority(UUID projectId, TaskStatus status, TaskPriority priority) {
        log.debug("Fetching tasks by project {}, status {} and priority {}", projectId, status, priority);

        List<Task> tasks = taskRepository.findByProjectIdAndStatusAndPriority(projectId, status, priority);
        return taskMapper.toResponseList(tasks);
    }

    @Override
    public List<TaskSummaryResponse> getRecentTasksByAssignee(UUID assigneeId) {
        log.debug("Fetching recent tasks by assignee: {}", assigneeId);

        List<Task> tasks = taskRepository.findRecentTasksByAssignee(assigneeId);
        return taskMapper.toSummaryResponseList(tasks);
    }

    @Override
    public List<TaskSummaryResponse> getRecentTasksByProject(UUID projectId) {
        log.debug("Fetching recent tasks by project: {}", projectId);

        List<Task> tasks = taskRepository.findRecentTasksByProject(projectId);
        return taskMapper.toSummaryResponseList(tasks);
    }

    @Override
    public List<TaskResponse> getActiveTasksByAssigneeOrderedByPriority(UUID assigneeId) {
        log.debug("Fetching active tasks by assignee ordered by priority: {}", assigneeId);

        List<Task> tasks = taskRepository.findActiveTasksByAssigneeOrderedByPriority(assigneeId);
        return taskMapper.toResponseList(tasks);
    }

    @Override
    public long getTotalTasksCount() {
        return taskRepository.count();
    }

    @Override
    public long getTasksByStatusCount(TaskStatus status) {
        return taskRepository.countByStatus(status);
    }

    @Override
    public long getTasksByAssigneeAndStatusCount(UUID assigneeId, TaskStatus status) {
        return taskRepository.countByAssigneeAndStatus(assigneeId, status);
    }

    @Override
    public long getActiveTasksByAssigneeCount(UUID assigneeId) {
        return taskRepository.countActiveTasksByAssignee(assigneeId);
    }

    @Override
    public List<Object[]> getTaskStatusStatisticsByProject(UUID projectId) {
        return taskRepository.getTaskStatusStatisticsByProject(projectId);
    }

    @Override
    public List<Object[]> getTaskPriorityStatisticsByProject(UUID projectId) {
        return taskRepository.getTaskPriorityStatisticsByProject(projectId);
    }

    @Override
    public List<Object[]> getAssigneeWorkloadByProject(UUID projectId) {
        return taskRepository.getAssigneeWorkloadByProject(projectId);
    }

    @Override
    public long getCommentsCountByTask(UUID taskId) {
        return taskRepository.countCommentsByTaskId(taskId);
    }

    private Task findTaskById(UUID id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + id));
    }

    private Project findProjectById(UUID id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + id));
    }

    private User findUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
    }

    private void validateUserCanAccessProject(User user, Project project) {
        boolean canAccess = project.getOwner().getId().equals(user.getId()) ||
                project.getMembers().contains(user);

        if (!canAccess) {
            throw new BusinessLogicException("access project", "user is not a member or owner of the project");
        }
    }

    private void validateStatusTransition(TaskStatus currentStatus, TaskStatus newStatus) {
        // Define valid status transitions
        boolean isValidTransition = switch (currentStatus) {
            case TODO -> newStatus == TaskStatus.IN_PROGRESS || newStatus == TaskStatus.CANCELLED;
            case IN_PROGRESS -> newStatus == TaskStatus.IN_REVIEW ||
                    newStatus == TaskStatus.TODO ||
                    newStatus == TaskStatus.CANCELLED;
            case IN_REVIEW -> newStatus == TaskStatus.DONE ||
                    newStatus == TaskStatus.IN_PROGRESS;
            case DONE -> newStatus == TaskStatus.IN_REVIEW; // Allow reopening
            case CANCELLED -> newStatus == TaskStatus.TODO; // Allow reactivation
        };

        if (!isValidTransition) {
            throw new BusinessLogicException("change task status",
                    String.format("invalid transition from %s to %s", currentStatus, newStatus));
        }
    }

    private PageResponse<TaskResponse> buildPageResponse(Page<Task> taskPage, List<TaskResponse> taskResponses) {
        return PageResponse.<TaskResponse>builder()
                .content(taskResponses)
                .page(taskPage.getNumber())
                .size(taskPage.getSize())
                .totalElements(taskPage.getTotalElements())
                .totalPages(taskPage.getTotalPages())
                .first(taskPage.isFirst())
                .last(taskPage.isLast())
                .hasNext(taskPage.hasNext())
                .hasPrevious(taskPage.hasPrevious())
                .build();
    }
}