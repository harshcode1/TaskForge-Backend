package com.projectmgmttool.backend.controller;

import com.projectmgmttool.backend.dto.ProjectMemberRequest;
import com.projectmgmttool.backend.entity.ProjectMember;
import com.projectmgmttool.backend.entity.Role;
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
class ProjectMemberControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testInviteUser() {
        ProjectMemberRequest request = new ProjectMemberRequest(UUID.randomUUID(), "testuser@example.com", Role.MEMBER);
        ResponseEntity<ProjectMember> response = restTemplate.postForEntity("/api/project-members/invite", request, ProjectMember.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("testuser@example.com", response.getBody().getEmail());
    }

    @Test
    void testGetProjectMembers() {
        UUID projectId = UUID.randomUUID();
        ResponseEntity<ProjectMember[]> response = restTemplate.getForEntity("/api/project-members/" + projectId, ProjectMember[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
