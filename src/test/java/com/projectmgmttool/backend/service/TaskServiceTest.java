package com.projectmgmttool.backend.service;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class TaskServiceTest {

    @Test
    void testCreateTask() {
        // Arrange
        TaskService taskService = mock(TaskService.class);
        doNothing().when(taskService).createTask(any());

        // Act
        taskService.createTask(null);

        // Assert
        verify(taskService, times(1)).createTask(null);
    }

    @Test
    void testDeleteTask() {
        // Arrange
        TaskService taskService = mock(TaskService.class);
        doNothing().when(taskService).deleteTask(anyLong());

        // Act
        taskService.deleteTask(1L);

        // Assert
        verify(taskService, times(1)).deleteTask(1L);
    }
}
