package com.projectmgmttool.backend.service;

import com.projectmgmttool.backend.entity.Project;
import com.projectmgmttool.backend.entity.User;
import com.projectmgmttool.backend.exception.CustomApiException;
import com.projectmgmttool.backend.repository.ProjectRepository;
import com.projectmgmttool.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    public Project createProject(String name, String description, String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new CustomApiException("User not found"));

        Project project = new Project();
        project.setName(name);
        project.setDescription(description);
        project.setOwner(owner);
        project.setCreatedAt(LocalDateTime.now());

        return projectRepository.save(project);
    }

    public List<Project> getProjectsForUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomApiException("User not found"));

        return projectRepository.findByOwnerId(user.getId());
    }

    public Project updateProject(UUID projectId, String name, String description, String requesterEmail) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new CustomApiException("Project not found"));

        if (!project.getOwner().getEmail().equals(requesterEmail)) {
            throw new CustomApiException("Only the owner can update the project");
        }

        project.setName(name);
        project.setDescription(description);
        return projectRepository.save(project);
    }

    public void deleteProject(UUID projectId, String requesterEmail) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new CustomApiException("Project not found"));

        if (!project.getOwner().getEmail().equals(requesterEmail)) {
            throw new CustomApiException("Only the owner can delete the project");
        }

        projectRepository.delete(project);
    }
}
