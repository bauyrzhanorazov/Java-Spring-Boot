package com.example.javaspring.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentUpdateRequest {

    @NotBlank(message = "Comment content is required")
    @Size(max = 1000, message = "Comment must not exceed 1000 characters")
    private String content;
}