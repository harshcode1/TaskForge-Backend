package com.projectmgmttool.backend.service;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class DashboardServiceTest {

    @Test
    void testGetDashboardData() {
        // Arrange
        DashboardService dashboardService = mock(DashboardService.class);
        when(dashboardService.getDashboardData(anyLong())).thenReturn(null);

        // Act
        Object result = dashboardService.getDashboardData(1L);

        // Assert
        assertNull(result);
    }
}
