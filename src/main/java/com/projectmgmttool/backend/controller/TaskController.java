package com.projectmgmttool.backend.controller;

import com.projectmgmttool.backend.entity.Task;
import com.projectmgmttool.backend.dto.TaskRequest;
import com.projectmgmttool.backend.dto.TaskDTO;
import com.projectmgmttool.backend.service.TaskService;
import com.projectmgmttool.backend.repository.TaskRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.Optional;
import java.util.UUID;

@Tag(name = "Tasks", description = "Endpoints for task management")
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @Operation(summary = "Create a new task", description = "Creates a new task within a project.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Task created successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Task.class))),
        @ApiResponse(responseCode = "404", description = "Project not found",
            content = @Content(mediaType = "application/json"))
    })
    @PostMapping
    public ResponseEntity<TaskDTO> createTask(
            @Valid @RequestBody TaskRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Task created = taskService.createTask(request, userDetails.getUsername());

        TaskDTO response = new TaskDTO(
                created.getId(),
                created.getTitle(),
                created.getDescription(),
                created.getPriority(),
                created.getStatus(),
                created.getCreatedAt(),
                created.getProject().getId()
        );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get tasks for a project", description = "Retrieves tasks for a specific project, optionally filtered by status.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Task.class))),
        @ApiResponse(responseCode = "404", description = "Project not found",
            content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<Task>> getTasks(
            @PathVariable UUID projectId,
            @RequestParam(required = false) String status) {
        List<Task> tasks = taskService.getTasksForProject(projectId, Optional.ofNullable(status));
        return ResponseEntity.ok(tasks);
    }

    @Operation(summary = "Update a task", description = "Updates a task by its ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Task updated successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Task.class))),
        @ApiResponse(responseCode = "404", description = "Task not found",
            content = @Content(mediaType = "application/json"))
    })
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(
            @PathVariable UUID id,
            @Valid @RequestBody TaskRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Task updated = taskService.updateTask(id, request, userDetails.getUsername());
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Delete a task", description = "Deletes a task by its ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Task deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Task not found",
            content = @Content(mediaType = "application/json"))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        taskService.deleteTask(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<Task>> getTasksForUser(@AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        List<Task> tasks = taskRepository.findTasksAssignedToUser(userId);
        return ResponseEntity.ok(tasks);
    }
}