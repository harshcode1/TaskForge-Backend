package com.projectmgmttool.backend.service;

import com.projectmgmttool.backend.dto.DashboardResponse;
import com.projectmgmttool.backend.entity.Project;
import com.projectmgmttool.backend.entity.Task;
import com.projectmgmttool.backend.entity.User;
import com.projectmgmttool.backend.exception.CustomApiException;
import com.projectmgmttool.backend.repository.ProjectMemberRepository;
import com.projectmgmttool.backend.repository.ProjectRepository;
import com.projectmgmttool.backend.repository.TaskRepository;
import com.projectmgmttool.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    public DashboardResponse getDashboardData(UUID projectId, String userEmail) {
        // Find the project
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new CustomApiException("Project not found", 404));

        // Check if user is authorized to view project dashboard
        boolean isOwner = project.getOwner().getEmail().equals(userEmail);
        boolean isMember = projectMemberRepository.existsByProjectIdAndUserEmail(projectId, userEmail);

        if (!isOwner && !isMember) {
            throw new CustomApiException("You are not authorized to view this project's dashboard", 403);
        }

        // Get all tasks for the project
        List<Task> tasks = taskRepository.findByProjectId(project.getId());

        // Calculate task stats by status
        Map<String, Long> taskStatusCounts = tasks.stream()
                .filter(task -> task.getStatus() != null)
                .collect(Collectors.groupingBy(
                    task -> task.getStatus().name(),
                    Collectors.counting()
                ));

        // Calculate tasks per user
        Map<String, Long> tasksPerUser = tasks.stream()
                .filter(task -> task.getAssignee() != null)
                .collect(Collectors.groupingBy(
                    task -> task.getAssignee().getEmail(),
                    Collectors.counting()
                ));

        return new DashboardResponse(taskStatusCounts, tasksPerUser);
    }

    public DashboardResponse getUserDashboardData(String userEmail) {
        // Find the user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomApiException("User not found", 404));

        // Get tasks assigned to the user
        List<Task> assignedTasks = taskRepository.findByAssigneeEmail(userEmail);

        // Calculate task stats by status
        Map<String, Long> taskStatusCounts = assignedTasks.stream()
                .filter(task -> task.getStatus() != null)
                .collect(Collectors.groupingBy(
                    task -> task.getStatus().name(),
                    Collectors.counting()
                ));

        // For personal dashboard, we'll use projects as the second dimension
        Map<String, Long> tasksPerProject = assignedTasks.stream()
                .filter(task -> task.getProject() != null)
                .collect(Collectors.groupingBy(
                    task -> task.getProject().getName(),
                    Collectors.counting()
                ));

        return new DashboardResponse(taskStatusCounts, tasksPerProject);
    }
}
