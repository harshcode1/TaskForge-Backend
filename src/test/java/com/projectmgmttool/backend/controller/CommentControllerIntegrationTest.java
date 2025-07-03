package com.projectmgmttool.backend.controller;

import com.projectmgmttool.backend.dto.CommentRequest;
import com.projectmgmttool.backend.entity.Comment;
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
class CommentControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testAddComment() {
        CommentRequest request = new CommentRequest("Test comment", UUID.randomUUID());
        ResponseEntity<Comment> response = restTemplate.postForEntity("/api/comments/add", request, Comment.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test comment", response.getBody().getContent());
    }

    @Test
    void testGetCommentsForTask() {
        UUID taskId = UUID.randomUUID();
        ResponseEntity<Comment[]> response = restTemplate.getForEntity("/api/comments/task/" + taskId, Comment[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
