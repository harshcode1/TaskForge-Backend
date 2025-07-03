package com.projectmgmttool.backend.service;

import com.projectmgmttool.backend.entity.Project;
import com.projectmgmttool.backend.entity.User;
import com.projectmgmttool.backend.entity.enums.Role;
import com.projectmgmttool.backend.exception.CustomApiException;
import com.projectmgmttool.backend.repository.ProjectRepository;
import com.projectmgmttool.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProjectService projectService;

    private Project project;
    private User user;
    private UUID projectId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();
        userId = UUID.randomUUID();

        user = new User();
        user.setId(userId);
        user.setName("John Doe");
        user.setEmail("john@example.com");
        user.setRole(Role.MANAGER);

        project = new Project();
        project.setId(projectId);
        project.setName("Test Project");
        project.setDescription("Test Description");
        project.setOwner(user);
    }

    @Test
    void createProject_Success_ReturnsProject() {
        // Arrange
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        // Act
        Project result = projectService.createProject("Test Project", "Test Description", "john@example.com");

        // Assert
        assertNotNull(result);
        assertEquals("Test Project", result.getName());
        assertEquals("Test Description", result.getDescription());
        assertEquals(user, result.getOwner());

        verify(userRepository).findByEmail("john@example.com");
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    void createProject_UserNotFound_ThrowsCustomApiException() {
        // Arrange
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        CustomApiException exception = assertThrows(CustomApiException.class,
            () -> projectService.createProject("Test Project", "Test Description", "john@example.com"));

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findByEmail("john@example.com");
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void getProjectsForUser_Success_ReturnsProjectList() {
        // Arrange
        List<Project> projects = Arrays.asList(project);
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(projectRepository.findByOwnerId(userId)).thenReturn(projects);

        // Act
        List<Project> result = projectService.getProjectsForUser("john@example.com");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(project, result.get(0));

        verify(userRepository).findByEmail("john@example.com");
        verify(projectRepository).findByOwnerId(userId);
    }

    @Test
    void deleteProject_Success_DeletesProject() {
        // Arrange
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        doNothing().when(projectRepository).delete(project);

        // Act
        assertDoesNotThrow(() -> projectService.deleteProject(projectId, "john@example.com"));

        // Assert
        verify(projectRepository).findById(projectId);
        verify(projectRepository).delete(project);
    }

    @Test
    void deleteProject_NotOwner_ThrowsCustomApiException() {
        // Arrange
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        // Act & Assert
        CustomApiException exception = assertThrows(CustomApiException.class,
            () -> projectService.deleteProject(projectId, "other@example.com"));

        assertEquals("Only the owner can delete the project", exception.getMessage());
        verify(projectRepository).findById(projectId);
        verify(projectRepository, never()).delete(any(Project.class));
    }
}
