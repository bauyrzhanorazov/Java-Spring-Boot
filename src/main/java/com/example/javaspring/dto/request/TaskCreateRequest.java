package com.example.javaspring.dto.request;

import com.example.javaspring.enums.TaskPriority;
import com.example.javaspring.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskCreateRequest {

    @NotBlank(message = "Task title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    private TaskStatus status;

    private TaskPriority priority;

    @NotNull(message = "Project ID is required")
    private UUID projectId;

    private UUID assigneeId;

    private LocalDateTime dueDate;
}