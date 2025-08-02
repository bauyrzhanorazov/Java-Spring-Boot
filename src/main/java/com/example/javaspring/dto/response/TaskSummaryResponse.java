package com.example.javaspring.dto.response;

import com.example.javaspring.enums.TaskPriority;
import com.example.javaspring.enums.TaskStatus;
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
public class TaskSummaryResponse {

    private UUID id;
    private String title;
    private TaskStatus status;
    private TaskPriority priority;
    private LocalDateTime dueDate;
    private UserSummaryResponse assignee;
    private Integer commentsCount;
}