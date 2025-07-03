package com.projectmgmttool.backend.service;

import com.projectmgmttool.backend.dto.TaskRequest;
import com.projectmgmttool.backend.entity.Project;
import com.projectmgmttool.backend.entity.ProjectMember;
import com.projectmgmttool.backend.entity.Task;
import com.projectmgmttool.backend.entity.User;
import com.projectmgmttool.backend.entity.enums.Priority;
import com.projectmgmttool.backend.entity.enums.Role;
import com.projectmgmttool.backend.entity.enums.TaskStatus;
import com.projectmgmttool.backend.exception.CustomApiException;
import com.projectmgmttool.backend.repository.ProjectMemberRepository;
import com.projectmgmttool.backend.repository.ProjectRepository;
import com.projectmgmttool.backend.repository.TaskRepository;
import com.projectmgmttool.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    @Autowired
    private UserRepository userRepository;

    public Task createTask(TaskRequest request, String creatorEmail) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new CustomApiException("Project not found"));

        User assignedTo = null;
        if (request.getAssigneeEmail() != null) {
            assignedTo = userRepository.findByEmail(request.getAssigneeEmail())
                    .orElseThrow(() -> new CustomApiException("Assignee not found"));
        }

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(TaskStatus.valueOf(request.getStatus()));
        // Fix: Convert Date to LocalDate
        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        }
        task.setProject(project);
        task.setAssignee(assignedTo);
        task.setCreatedAt(LocalDateTime.now());

        return taskRepository.save(task);
    }

    public List<Task> getTasksForProject(UUID projectId, Optional<String> statusOpt) {
        String status = statusOpt.orElse(null);
        if (status != null && !status.isEmpty()) {
            return taskRepository.findByProjectIdAndStatus(projectId, status);
        } else {
            return taskRepository.findByProjectId(projectId);
        }
    }

    public Task updateTask(UUID taskId, TaskRequest request, String userEmail) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new CustomApiException("Task not found"));

        User requestingUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomApiException("User not found"));

        // Check if user is a member of the project
        Optional<ProjectMember> memberOpt = projectMemberRepository
                .findByProjectIdAndUserEmail(task.getProject().getId(), userEmail);

        // Only allow update if user is a project member with manager role or is the project owner
        if (memberOpt.isEmpty() &&
            !task.getProject().getOwner().getEmail().equals(userEmail)) {
            throw new CustomApiException("You are not authorized to update this task");
        }

        // If user is a member, check if they have manager role
        if (memberOpt.isPresent()) {
            ProjectMember member = memberOpt.get();
            Project project = task.getProject();

            boolean isManager = member.getRole() == Role.MANAGER;
            boolean isOwner = project.getOwner().getId().equals(requestingUser.getId());

            if (!isManager && !isOwner) {
                throw new CustomApiException("You do not have permission to update this task.");
            }
        }

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(TaskStatus.valueOf(request.getStatus()));

        // Convert Date to LocalDate if present
        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate());
        }

        return taskRepository.save(task);
    }

    public void deleteTask(UUID taskId, String userEmail) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new CustomApiException("Task not found"));

        User requestingUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomApiException("User not found"));

        // Only project owner or manager can delete tasks
        Project project = task.getProject();
        boolean isOwner = project.getOwner().getId().equals(requestingUser.getId());

        // Check if user is a member with manager role
        Optional<ProjectMember> memberOpt = projectMemberRepository
                .findByProjectIdAndUserEmail(project.getId(), userEmail);

        boolean isManager = memberOpt.isPresent() && memberOpt.get().getRole() == Role.MANAGER;

        if (!isOwner && !isManager) {
            throw new CustomApiException("You do not have permission to delete this task");
        }

        taskRepository.delete(task);
    }
}
