package com.projectmgmttool.backend.controller;

import com.projectmgmttool.backend.dto.AuthRequest;
import com.projectmgmttool.backend.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testRegisterUser() {
        RegisterRequest request = new RegisterRequest("John Doe", "john@example.com", "password123", "MEMBER");
        ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/register", request, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testLoginUser() {
        AuthRequest request = new AuthRequest("john@example.com", "password123");
        ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/login", request, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
