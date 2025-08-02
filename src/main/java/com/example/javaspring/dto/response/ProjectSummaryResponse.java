package com.example.javaspring.dto.response;

import com.example.javaspring.enums.ProjectStatus;
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
public class ProjectSummaryResponse {

    private UUID id;
    private String name;
    private ProjectStatus status;
    private LocalDateTime deadline;
    private UserSummaryResponse owner;
    private Integer tasksCount;
}