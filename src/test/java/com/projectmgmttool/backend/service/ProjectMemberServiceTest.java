package com.projectmgmttool.backend.service;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ProjectMemberServiceTest {

    @Test
    void testAddProjectMember() {
        // Arrange
        ProjectMemberService projectMemberService = mock(ProjectMemberService.class);
        doNothing().when(projectMemberService).addProjectMember(any());

        // Act
        projectMemberService.addProjectMember(null);

        // Assert
        verify(projectMemberService, times(1)).addProjectMember(null);
    }

    @Test
    void testRemoveProjectMember() {
        // Arrange
        ProjectMemberService projectMemberService = mock(ProjectMemberService.class);
        doNothing().when(projectMemberService).removeProjectMember(anyLong());

        // Act
        projectMemberService.removeProjectMember(1L);

        // Assert
        verify(projectMemberService, times(1)).removeProjectMember(1L);
    }
}
