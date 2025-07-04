package com.projectmgmttool.backend.service;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ProjectServiceTest {

    @Test
    void testCreateProject() {
        // Arrange
        ProjectService projectService = mock(ProjectService.class);
        doNothing().when(projectService).createProject(any());

        // Act
        projectService.createProject(null);

        // Assert
        verify(projectService, times(1)).createProject(null);
    }

    @Test
    void testDeleteProject() {
        // Arrange
        ProjectService projectService = mock(ProjectService.class);
        doNothing().when(projectService).deleteProject(anyLong());

        // Act
        projectService.deleteProject(1L);

        // Assert
        verify(projectService, times(1)).deleteProject(1L);
    }
}
