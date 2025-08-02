package com.example.javaspring.dto.response;

import com.example.javaspring.enums.TaskPriority;
import com.example.javaspring.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskResponse {

    private UUID id;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private ProjectSummaryResponse project;
    private UserSummaryResponse assignee;
    private UserSummaryResponse reporter;
    private List<CommentResponse> comments;
}