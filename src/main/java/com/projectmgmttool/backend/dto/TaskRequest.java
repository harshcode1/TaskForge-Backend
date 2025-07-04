package com.projectmgmttool.backend.dto;

import com.projectmgmttool.backend.entity.enums.Priority;
import jakarta.validation.constraints.*;

import java.util.Date;
import java.util.UUID;

public class TaskRequest {

    @NotBlank(message = "Task title is required")
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    private String title;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotNull(message = "Project ID is required")
    private UUID projectId;

    @Email(message = "Please provide a valid email address for assignee")
    private String assigneeEmail;

    @NotBlank(message = "Task status is required")
    @Pattern(regexp = "^(TODO|IN_PROGRESS|DONE)$", message = "Status must be TODO, IN_PROGRESS, or DONE")
    private String status;

    @NotNull(message = "Priority is required")
    private Priority priority;

    private Date dueDate;

    public TaskRequest(String title, String description, UUID projectId, String assigneeEmail, String status, Priority priority) {
        this.title = title;
        this.description = description;
        this.projectId = projectId;
        this.assigneeEmail = assigneeEmail;
        this.status = status;
        this.priority = priority;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }

    public void setAssigneeEmail(String assigneeEmail) {
        this.assigneeEmail = assigneeEmail;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public String getTitle() {
        return title;
    }
    public String getDescription() {
        return description;
    }
    public UUID getProjectId() {
        return projectId;
    }
    public String getAssigneeEmail() {
        return assigneeEmail;
    }
    public String getStatus() {
        return status;
    }
    public Priority getPriority() {
        return priority;
    }
    public Date getDueDate() {
        return dueDate;
    }
}
