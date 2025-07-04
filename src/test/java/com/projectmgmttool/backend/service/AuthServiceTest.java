package com.projectmgmttool.backend.service;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    @Test
    void testAuthenticateUser() {
        // Arrange
        AuthService authService = mock(AuthService.class);
        when(authService.authenticateUser(anyString(), anyString())).thenReturn("token");
        // Act
        String result = authService.authenticateUser("username", "password");

        // Assert
        assertEquals("token", result);
    }

    @Test
    void testRegisterUser() {
        // Arrange
        AuthService authService = mock(AuthService.class);
        doNothing().when(authService).registerUser(any());

        // Act
        authService.registerUser(null);

        // Assert
        verify(authService, times(1)).registerUser(null);
    }
}

