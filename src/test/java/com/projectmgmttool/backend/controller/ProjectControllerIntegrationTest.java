package com.projectmgmttool.backend.controller;

import com.projectmgmttool.backend.dto.ProjectRequest;
import com.projectmgmttool.backend.entity.Project;
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
class ProjectControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testCreateProject() {
        ProjectRequest request = new ProjectRequest("Test Project", "Description of Test Project");
        ResponseEntity<Project> response = restTemplate.postForEntity("/api/projects", request, Project.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test Project", response.getBody().getName());
    }

    @Test
    void testGetMyProjects() {
        ResponseEntity<Project[]> response = restTemplate.getForEntity("/api/projects", Project[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testUpdateProject() {
        UUID projectId = UUID.randomUUID();
        ProjectRequest request = new ProjectRequest("Updated Project", "Updated Description");
        ResponseEntity<Project> response = restTemplate.exchange("/api/projects/" + projectId, HttpMethod.PUT, new HttpEntity<>(request), Project.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Updated Project", response.getBody().getName());
    }
}
