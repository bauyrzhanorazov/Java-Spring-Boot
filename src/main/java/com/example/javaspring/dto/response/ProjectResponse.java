package com.example.javaspring.dto.response;

import com.example.javaspring.enums.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectResponse {

    private UUID id;
    private String name;
    private String description;
    private ProjectStatus status;
    private LocalDateTime deadline;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserSummaryResponse owner;
    private Set<UserSummaryResponse> members;
    private Integer tasksCount;
}