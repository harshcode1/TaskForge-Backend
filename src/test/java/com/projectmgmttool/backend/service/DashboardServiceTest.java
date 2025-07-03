package com.projectmgmttool.backend.service;

import com.projectmgmttool.backend.dto.DashboardResponse;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @InjectMocks
    private DashboardService dashboardService;

    private User user;
    private Project project1;
    private Project project2;
    private Task todoTask;
    private Task inProgressTask;
    private Task doneTask;
    private ProjectMember projectMember;
    private UUID userId;
    private UUID project1Id;
    private UUID project2Id;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        project1Id = UUID.randomUUID();
        project2Id = UUID.randomUUID();

        // Setup User
        user = new User();
        user.setId(userId);
        user.setName("John Doe");
        user.setEmail("john@example.com");
        user.setRole(Role.MEMBER);

        // Setup Projects
        project1 = new Project();
        project1.setId(project1Id);
        project1.setName("Project 1");
        project1.setOwner(user);
        project1.setCreatedAt(LocalDateTime.now());

        project2 = new Project();
        project2.setId(project2Id);
        project2.setName("Project 2");
        project2.setCreatedAt(LocalDateTime.now());

        // Setup ProjectMember
        projectMember = new ProjectMember();
        projectMember.setProject(project2);
        projectMember.setUser(user);
        projectMember.setRole(Role.MEMBER);

        // Setup Tasks
        todoTask = new Task();
        todoTask.setTitle("Todo Task");
        todoTask.setStatus(TaskStatus.TODO);
        todoTask.setPriority(Priority.HIGH);
        todoTask.setDueDate(LocalDate.now().plusDays(3));
        todoTask.setProject(project1);

        inProgressTask = new Task();
        inProgressTask.setTitle("In Progress Task");
        inProgressTask.setStatus(TaskStatus.IN_PROGRESS);
        inProgressTask.setPriority(Priority.MEDIUM);
        inProgressTask.setDueDate(LocalDate.now().plusDays(1));
        inProgressTask.setProject(project1);

        doneTask = new Task();
        doneTask.setTitle("Done Task");
        doneTask.setStatus(TaskStatus.DONE);
        doneTask.setPriority(Priority.LOW);
        doneTask.setDueDate(LocalDate.now().minusDays(1));
        doneTask.setProject(project2);
        doneTask.setAssignee(user);
    }

    @Test
    void getDashboardData_Success_ReturnsDashboardResponse() {
        // Arrange
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        // Owned projects
        List<Project> ownedProjects = Collections.singletonList(project1);
        when(projectRepository.findByOwnerId(userId)).thenReturn(ownedProjects);

        // Projects as member
        List<ProjectMember> memberships = Collections.singletonList(projectMember);
        when(projectMemberRepository.findByUserEmail("john@example.com")).thenReturn(memberships);

        // Tasks in projects (both owned and member)
        List<Task> assignedTasks = Collections.singletonList(doneTask);
        when(taskRepository.findByAssigneeEmail("john@example.com")).thenReturn(assignedTasks);

        List<Task> project1Tasks = Arrays.asList(todoTask, inProgressTask);
        when(taskRepository.findByProjectId(project1Id)).thenReturn(project1Tasks);

        List<Task> project2Tasks = Collections.singletonList(doneTask);
        when(taskRepository.findByProjectId(project2Id)).thenReturn(project2Tasks);

        List<Task> upcomingTasks = Arrays.asList(todoTask, inProgressTask);
        when(taskRepository.findByDueDateBetweenAndAssigneeEmail(any(LocalDate.class), any(LocalDate.class), eq("john@example.com")))
            .thenReturn(upcomingTasks);

        // Act
        DashboardResponse response = dashboardService.getDashboardData("john@example.com");

        // Assert
        assertNotNull(response);
        assertEquals(2, response.getTotalProjects());
        assertEquals(3, response.getTotalTasks());
        assertEquals(1, response.getCompletedTasks());
        assertEquals(1, response.getAssignedTasks());
        assertEquals(2, response.getUpcomingTasks());

        verify(userRepository).findByEmail("john@example.com");
        verify(projectRepository).findByOwnerId(userId);
        verify(projectMemberRepository).findByUserEmail("john@example.com");
        verify(taskRepository).findByAssigneeEmail("john@example.com");
        verify(taskRepository, times(2)).findByProjectId(any(UUID.class));
        verify(taskRepository).findByDueDateBetweenAndAssigneeEmail(any(LocalDate.class), any(LocalDate.class), eq("john@example.com"));
    }

    @Test
    void getDashboardData_UserNotFound_ThrowsCustomApiException() {
        // Arrange
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        CustomApiException exception = assertThrows(CustomApiException.class,
            () -> dashboardService.getDashboardData("john@example.com"));

        assertEquals("User not found", exception.getMessage());

        verify(userRepository).findByEmail("john@example.com");
        verify(projectRepository, never()).findByOwnerId(any(UUID.class));
        verify(projectMemberRepository, never()).findByUserEmail(anyString());
        verify(taskRepository, never()).findByAssigneeEmail(anyString());
    }

    @Test
    void getDashboardData_NoProjects_ReturnsEmptyDashboard() {
        // Arrange
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(projectRepository.findByOwnerId(userId)).thenReturn(Collections.emptyList());
        when(projectMemberRepository.findByUserEmail("john@example.com")).thenReturn(Collections.emptyList());
        when(taskRepository.findByAssigneeEmail("john@example.com")).thenReturn(Collections.emptyList());
        when(taskRepository.findByDueDateBetweenAndAssigneeEmail(any(LocalDate.class), any(LocalDate.class), eq("john@example.com")))
            .thenReturn(Collections.emptyList());

        // Act
        DashboardResponse response = dashboardService.getDashboardData("john@example.com");

        // Assert
        assertNotNull(response);
        assertEquals(0, response.getTotalProjects());
        assertEquals(0, response.getTotalTasks());
        assertEquals(0, response.getCompletedTasks());
        assertEquals(0, response.getAssignedTasks());
        assertEquals(0, response.getUpcomingTasks());

        verify(userRepository).findByEmail("john@example.com");
        verify(projectRepository).findByOwnerId(userId);
        verify(projectMemberRepository).findByUserEmail("john@example.com");
        verify(taskRepository).findByAssigneeEmail("john@example.com");
        verify(taskRepository, never()).findByProjectId(any(UUID.class));
    }

    @Test
    void getDashboardData_EmptyEmail_ThrowsException() {
        // Act & Assert
        assertThrows(Exception.class, () -> dashboardService.getDashboardData(""));
    }

    @Test
    void getDashboardData_NullEmail_ThrowsException() {
        // Act & Assert
        assertThrows(Exception.class, () -> dashboardService.getDashboardData(null));
    }
}
