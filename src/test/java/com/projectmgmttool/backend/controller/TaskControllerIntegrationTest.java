package com.projectmgmttool.backend.controller;

import com.projectmgmttool.backend.dto.TaskRequest;
import com.projectmgmttool.backend.entity.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TaskControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testCreateTask() {
        TaskRequest request = new TaskRequest("Test Task", "Description of Test Task", UUID.randomUUID(), UUID.randomUUID(), "TODO");
        ResponseEntity<Task> response = restTemplate.postForEntity("/api/tasks", request, Task.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test Task", response.getBody().getTitle());
    }

    @Test
    void testGetTasksForProject() {
        UUID projectId = UUID.randomUUID();
        ResponseEntity<Task[]> response = restTemplate.getForEntity("/api/tasks/project/" + projectId, Task[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testUpdateTask() {
        UUID taskId = UUID.randomUUID();
        TaskRequest request = new TaskRequest("Updated Task", "Updated Description", UUID.randomUUID(), UUID.randomUUID(), "IN_PROGRESS");
        ResponseEntity<Task> response = restTemplate.exchange("/api/tasks/" + taskId, HttpMethod.PUT, new HttpEntity<>(request), Task.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Updated Task", response.getBody().getTitle());
    }

    @Test
    void testDeleteTask() {
        UUID taskId = UUID.randomUUID();
        ResponseEntity<Void> response = restTemplate.exchange("/api/tasks/" + taskId, HttpMethod.DELETE, null, Void.class);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }
}
