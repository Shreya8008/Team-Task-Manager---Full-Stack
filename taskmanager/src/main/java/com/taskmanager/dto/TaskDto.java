package com.taskmanager.dto;

import com.taskmanager.entity.Task;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TaskDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank(message = "Task title is required")
        private String title;

        private String description;

        @NotNull(message = "Project ID is required")
        private Long projectId;

        private Long assignedToId;
        private Task.Priority priority;
        private LocalDate dueDate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        private String title;
        private String description;
        private Task.Status status;
        private Task.Priority priority;
        private Long assignedToId;
        private LocalDate dueDate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TaskResponse {
        private Long id;
        private String title;
        private String description;
        private Task.Status status;
        private Task.Priority priority;
        private LocalDate dueDate;
        private boolean overdue;
        private Long projectId;
        private String projectName;
        private AuthDto.UserResponse createdBy;
        private AuthDto.UserResponse assignedTo;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
