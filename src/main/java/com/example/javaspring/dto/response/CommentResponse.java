package com.example.javaspring.dto.response;

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
public class CommentResponse {

    private UUID id;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserSummaryResponse author;
}