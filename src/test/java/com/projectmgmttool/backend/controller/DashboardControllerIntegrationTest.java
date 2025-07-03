package com.projectmgmttool.backend.controller;

import com.projectmgmttool.backend.dto.DashboardResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DashboardControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testGetProjectDashboard() {
        UUID projectId = UUID.randomUUID();
        ResponseEntity<DashboardResponse> response = restTemplate.getForEntity("/api/dashboard/" + projectId, DashboardResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testGetPersonalDashboard() {
        ResponseEntity<DashboardResponse> response = restTemplate.getForEntity("/api/dashboard/my-dashboard", DashboardResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
