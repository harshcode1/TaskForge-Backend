package com.projectmgmttool.backend.service;

import com.projectmgmttool.backend.dto.TaskRequest;
import com.projectmgmttool.backend.entity.Project;
import com.projectmgmttool.backend.entity.Task;
import com.projectmgmttool.backend.entity.User;
import com.projectmgmttool.backend.entity.enums.TaskStatus;
import com.projectmgmttool.backend.exception.CustomApiException;
import com.projectmgmttool.backend.repository.ProjectRepository;
import com.projectmgmttool.backend.repository.TaskRepository;
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
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    private TaskRequest taskRequest;
    private Task task;
    private Project project;
    private User user;
    private UUID taskId;
    private UUID projectId;

    @BeforeEach
    void setUp() {
        taskId = UUID.randomUUID();
        projectId = UUID.randomUUID();

        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("john@example.com");

        project = new Project();
        project.setId(projectId);
        project.setName("Test Project");
        project.setOwner(user);

        taskRequest = new TaskRequest();
        taskRequest.setTitle("Test Task");
        taskRequest.setDescription("Test Description");
        taskRequest.setProjectId(projectId);
        taskRequest.setStatus("TODO");

        task = new Task();
        task.setId(taskId);
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        task.setStatus(TaskStatus.TODO);
        task.setProject(project);
    }

    @Test
    void createTask_Success_ReturnsTask() {
        // Arrange
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // Act
        Task result = taskService.createTask(taskRequest, "john@example.com");

        // Assert
        assertNotNull(result);
        assertEquals("Test Task", result.getTitle());
        assertEquals("Test Description", result.getDescription());
        assertEquals(TaskStatus.TODO, result.getStatus());

        verify(projectRepository).findById(projectId);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void createTask_ProjectNotFound_ThrowsCustomApiException() {
        // Arrange
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        // Act & Assert
        CustomApiException exception = assertThrows(CustomApiException.class,
            () -> taskService.createTask(taskRequest, "john@example.com"));

        assertEquals("Project not found", exception.getMessage());
        verify(projectRepository).findById(projectId);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void getTasksForProject_WithoutStatus_ReturnsAllTasks() {
        // Arrange
        List<Task> tasks = Arrays.asList(task);
        when(taskRepository.findByProjectId(projectId)).thenReturn(tasks);

        // Act
        List<Task> result = taskService.getTasksForProject(projectId, Optional.empty());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(task, result.get(0));

        verify(taskRepository).findByProjectId(projectId);
    }

    @Test
    void deleteTask_Success_DeletesTask() {
        // Arrange
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        doNothing().when(taskRepository).delete(task);

        // Act
        assertDoesNotThrow(() -> taskService.deleteTask(taskId, "john@example.com"));

        // Assert
        verify(taskRepository).findById(taskId);
        verify(taskRepository).delete(task);
    }

    @Test
    void deleteTask_TaskNotFound_ThrowsCustomApiException() {
        // Arrange
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        // Act & Assert
        CustomApiException exception = assertThrows(CustomApiException.class,
            () -> taskService.deleteTask(taskId, "john@example.com"));

        assertEquals("Task not found", exception.getMessage());
        verify(taskRepository).findById(taskId);
        verify(taskRepository, never()).delete(any(Task.class));
    }

    @Test
    void testCreateTask() {
        when(projectRepository.findById(any(UUID.class))).thenReturn(Optional.of(new Project()));
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(new User()));
        when(taskRepository.save(any(Task.class))).thenReturn(new Task());

        TaskRequest taskRequest = new TaskRequest("Task Title", "Task Description", UUID.randomUUID(), UUID.randomUUID(), TaskStatus.TODO);
        Task task = taskService.createTask(taskRequest);

        assertNotNull(task);
        assertEquals("Task Title", task.getTitle());
        verify(taskRepository).save(any(Task.class));
    }
}
