package com.projectmgmttool.backend.service;

import com.projectmgmttool.backend.dto.AuthRequest;
import com.projectmgmttool.backend.dto.AuthResponse;
import com.projectmgmttool.backend.dto.RegisterRequest;
import com.projectmgmttool.backend.entity.User;
import com.projectmgmttool.backend.entity.enums.Role;
import com.projectmgmttool.backend.repository.UserRepository;
import com.projectmgmttool.backend.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private AuthRequest authRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setName("John Doe");
        registerRequest.setEmail("john@example.com");
        registerRequest.setPassword("Password123");
        registerRequest.setRole(Role.MEMBER);

        authRequest = new AuthRequest();
        authRequest.setEmail("john@example.com");
        authRequest.setPassword("Password123");

        user = new User();
        user.setId(UUID.randomUUID());
        user.setName("John Doe");
        user.setEmail("john@example.com");
        user.setPassword("encodedPassword");
        user.setRole(Role.MEMBER);
    }

    @Test
    void register_Success_ReturnsAuthResponse() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtUtil.generateToken(any(User.class))).thenReturn("jwt-token");

        // Act
        AuthResponse response = authService.register(registerRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals(user.getId(), response.getUserId());
        assertEquals("John Doe", response.getName());
        assertEquals("john@example.com", response.getEmail());
        assertEquals(Role.MEMBER, response.getRole());

        verify(userRepository).existsByEmail("john@example.com");
        verify(passwordEncoder).encode("Password123");
        verify(userRepository).save(any(User.class));
        verify(jwtUtil).generateToken(any(User.class));
    }

    @Test
    void register_EmailAlreadyExists_ThrowsRuntimeException() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> authService.register(registerRequest));

        assertEquals("Email already registered", exception.getMessage());

        verify(userRepository).existsByEmail("john@example.com");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_Success_ReturnsAuthResponse() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(null);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(any(User.class))).thenReturn("jwt-token");

        // Act
        AuthResponse response = authService.login(authRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals(user.getId(), response.getUserId());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail("john@example.com");
        verify(jwtUtil).generateToken(user);
    }

    @Test
    void login_UserNotFound_ThrowsRuntimeException() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(null);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> authService.login(authRequest));

        assertEquals("User not found", exception.getMessage());

        verify(userRepository).findByEmail("john@example.com");
        verify(jwtUtil, never()).generateToken(any(User.class));
    }

    @Test
    void testAuthenticateUser() {
        // Arrange
        AuthRequest authRequest = new AuthRequest("testuser", "password");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(jwtUtil.generateToken(anyString())).thenReturn("mockToken");

        // Act
        AuthResponse response = authService.authenticateUser(authRequest);

        // Assert
        assertNotNull(response);
        assertEquals("mockToken", response.getToken());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil).generateToken(anyString());
    }
}
