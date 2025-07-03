package com.projectmgmttool.backend.service;

import com.projectmgmttool.backend.dto.ProjectMemberRequest;
import com.projectmgmttool.backend.entity.Project;
import com.projectmgmttool.backend.entity.ProjectMember;
import com.projectmgmttool.backend.entity.User;
import com.projectmgmttool.backend.entity.enums.Role;
import com.projectmgmttool.backend.exception.CustomApiException;
import com.projectmgmttool.backend.repository.ProjectMemberRepository;
import com.projectmgmttool.backend.repository.ProjectRepository;
import com.projectmgmttool.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectMemberServiceTest {

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProjectMemberService projectMemberService;

    private ProjectMemberRequest projectMemberRequest;
    private ProjectMember projectMember;
    private Project project;
    private User owner;
    private User member;
    private UUID projectId;
    private UUID ownerId;
    private UUID memberId;
    private Long projectMemberId;

    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();
        ownerId = UUID.randomUUID();
        memberId = UUID.randomUUID();
        projectMemberId = 1L;

        // Setup Owner
        owner = new User();
        owner.setId(ownerId);
        owner.setName("Project Owner");
        owner.setEmail("owner@example.com");
        owner.setRole(Role.MANAGER);

        // Setup Member
        member = new User();
        member.setId(memberId);
        member.setName("Team Member");
        member.setEmail("member@example.com");
        member.setRole(Role.MEMBER);

        // Setup Project
        project = new Project();
        project.setId(projectId);
        project.setName("Test Project");
        project.setOwner(owner);

        // Setup ProjectMemberRequest
        projectMemberRequest = new ProjectMemberRequest();
        projectMemberRequest.setProjectId(projectId);
        projectMemberRequest.setMemberEmail("member@example.com");
        projectMemberRequest.setRole(Role.MEMBER.name());

        // Setup ProjectMember
        projectMember = new ProjectMember();
        projectMember.setId(projectMemberId);
        projectMember.setUser(member);
        projectMember.setProject(project);
        projectMember.setRole(Role.MEMBER);
    }

    @Test
    void addProjectMember_Success_ReturnsProjectMember() {
        // Arrange
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(userRepository.findByEmail("member@example.com")).thenReturn(Optional.of(member));
        when(projectMemberRepository.existsByProjectIdAndUserEmail(projectId, "member@example.com")).thenReturn(false);
        when(projectMemberRepository.save(any(ProjectMember.class))).thenReturn(projectMember);

        // Act
        ProjectMember result = projectMemberService.addProjectMember(projectMemberRequest, "owner@example.com");

        // Assert
        assertNotNull(result);
        assertEquals(member, result.getUser());
        assertEquals(project, result.getProject());
        assertEquals(Role.MEMBER, result.getRole());

        verify(projectRepository).findById(projectId);
        verify(userRepository).findByEmail("member@example.com");
        verify(projectMemberRepository).existsByProjectIdAndUserEmail(projectId, "member@example.com");
        verify(projectMemberRepository).save(any(ProjectMember.class));
    }

    @Test
    void addProjectMember_ProjectNotFound_ThrowsCustomApiException() {
        // Arrange
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        // Act & Assert
        CustomApiException exception = assertThrows(CustomApiException.class,
            () -> projectMemberService.addProjectMember(projectMemberRequest, "owner@example.com"));

        assertEquals("Project not found", exception.getMessage());

        verify(projectRepository).findById(projectId);
        verify(userRepository, never()).findByEmail(anyString());
        verify(projectMemberRepository, never()).save(any(ProjectMember.class));
    }

    @Test
    void addProjectMember_NotOwner_ThrowsCustomApiException() {
        // Arrange
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        // Act & Assert
        CustomApiException exception = assertThrows(CustomApiException.class,
            () -> projectMemberService.addProjectMember(projectMemberRequest, "other@example.com"));

        assertEquals("Only the project owner can add members", exception.getMessage());

        verify(projectRepository).findById(projectId);
        verify(userRepository, never()).findByEmail(anyString());
        verify(projectMemberRepository, never()).save(any(ProjectMember.class));
    }

    @Test
    void addProjectMember_UserNotFound_ThrowsCustomApiException() {
        // Arrange
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(userRepository.findByEmail("member@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        CustomApiException exception = assertThrows(CustomApiException.class,
            () -> projectMemberService.addProjectMember(projectMemberRequest, "owner@example.com"));

        assertEquals("User not found", exception.getMessage());

        verify(projectRepository).findById(projectId);
        verify(userRepository).findByEmail("member@example.com");
        verify(projectMemberRepository, never()).save(any(ProjectMember.class));
    }

    @Test
    void addProjectMember_AlreadyMember_ThrowsCustomApiException() {
        // Arrange
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(userRepository.findByEmail("member@example.com")).thenReturn(Optional.of(member));
        when(projectMemberRepository.existsByProjectIdAndUserEmail(projectId, "member@example.com")).thenReturn(true);

        // Act & Assert
        CustomApiException exception = assertThrows(CustomApiException.class,
            () -> projectMemberService.addProjectMember(projectMemberRequest, "owner@example.com"));

        assertEquals("User is already a member of this project", exception.getMessage());

        verify(projectRepository).findById(projectId);
        verify(userRepository).findByEmail("member@example.com");
        verify(projectMemberRepository).existsByProjectIdAndUserEmail(projectId, "member@example.com");
        verify(projectMemberRepository, never()).save(any(ProjectMember.class));
    }

    @Test
    void addProjectMember_InvalidRole_ThrowsIllegalArgumentException() {
        // Arrange
        projectMemberRequest.setRole("INVALID_ROLE");
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(userRepository.findByEmail("member@example.com")).thenReturn(Optional.of(member));
        when(projectMemberRepository.existsByProjectIdAndUserEmail(projectId, "member@example.com")).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> projectMemberService.addProjectMember(projectMemberRequest, "owner@example.com"));

        assertTrue(exception.getMessage().contains("No enum constant"));
    }

    @Test
    void getProjectMembers_Success_ReturnsProjectMemberList() {
        // Arrange
        List<ProjectMember> members = Arrays.asList(projectMember);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectMemberRepository.findByProjectId(projectId)).thenReturn(members);

        // Act
        List<ProjectMember> result = projectMemberService.getProjectMembers(projectId, "owner@example.com");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(projectMember, result.get(0));

        verify(projectRepository).findById(projectId);
        verify(projectMemberRepository).findByProjectId(projectId);
    }

    @Test
    void getProjectMembers_ProjectNotFound_ThrowsCustomApiException() {
        // Arrange
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        // Act & Assert
        CustomApiException exception = assertThrows(CustomApiException.class,
            () -> projectMemberService.getProjectMembers(projectId, "owner@example.com"));

        assertEquals("Project not found", exception.getMessage());

        verify(projectRepository).findById(projectId);
        verify(projectMemberRepository, never()).findByProjectId(any(UUID.class));
    }

    @Test
    void getProjectMembers_NotOwnerOrMember_ThrowsCustomApiException() {
        // Arrange
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProjectIdAndUserEmail(projectId, "other@example.com")).thenReturn(false);

        // Act & Assert
        CustomApiException exception = assertThrows(CustomApiException.class,
            () -> projectMemberService.getProjectMembers(projectId, "other@example.com"));

        assertEquals("You are not authorized to view this project's members", exception.getMessage());

        verify(projectRepository).findById(projectId);
        verify(projectMemberRepository).existsByProjectIdAndUserEmail(projectId, "other@example.com");
        verify(projectMemberRepository, never()).findByProjectId(any(UUID.class));
    }

    @Test
    void getProjectMembers_AsMember_ReturnsProjectMemberList() {
        // Arrange
        List<ProjectMember> members = Arrays.asList(projectMember);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectMemberRepository.existsByProjectIdAndUserEmail(projectId, "member@example.com")).thenReturn(true);
        when(projectMemberRepository.findByProjectId(projectId)).thenReturn(members);

        // Act
        List<ProjectMember> result = projectMemberService.getProjectMembers(projectId, "member@example.com");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(projectMember, result.get(0));

        verify(projectRepository).findById(projectId);
        verify(projectMemberRepository).existsByProjectIdAndUserEmail(projectId, "member@example.com");
        verify(projectMemberRepository).findByProjectId(projectId);
    }

    @Test
    void removeProjectMember_Success_RemovesMember() {
        // Arrange
        when(projectMemberRepository.findById(projectMemberId)).thenReturn(Optional.of(projectMember));
        doNothing().when(projectMemberRepository).delete(projectMember);

        // Act
        assertDoesNotThrow(() -> projectMemberService.removeProjectMember(projectMemberId, "owner@example.com"));

        // Assert
        verify(projectMemberRepository).findById(projectMemberId);
        verify(projectMemberRepository).delete(projectMember);
    }

    @Test
    void removeProjectMember_MemberNotFound_ThrowsCustomApiException() {
        // Arrange
        when(projectMemberRepository.findById(projectMemberId)).thenReturn(Optional.empty());

        // Act & Assert
        CustomApiException exception = assertThrows(CustomApiException.class,
            () -> projectMemberService.removeProjectMember(projectMemberId, "owner@example.com"));

        assertEquals("Project member not found", exception.getMessage());

        verify(projectMemberRepository).findById(projectMemberId);
        verify(projectMemberRepository, never()).delete(any(ProjectMember.class));
    }

    @Test
    void removeProjectMember_NotOwner_ThrowsCustomApiException() {
        // Arrange
        when(projectMemberRepository.findById(projectMemberId)).thenReturn(Optional.of(projectMember));

        // Act & Assert
        CustomApiException exception = assertThrows(CustomApiException.class,
            () -> projectMemberService.removeProjectMember(projectMemberId, "other@example.com"));

        assertEquals("Only the project owner can remove members", exception.getMessage());

        verify(projectMemberRepository).findById(projectMemberId);
        verify(projectMemberRepository, never()).delete(any(ProjectMember.class));
    }

    @Test
    void removeProjectMember_RemovingSelf_ThrowsCustomApiException() {
        // Arrange
        ProjectMember ownerMember = new ProjectMember();
        ownerMember.setId(2L);
        ownerMember.setUser(owner);
        ownerMember.setProject(project);
        ownerMember.setRole(Role.MANAGER);

        when(projectMemberRepository.findById(2L)).thenReturn(Optional.of(ownerMember));

        // Act & Assert
        CustomApiException exception = assertThrows(CustomApiException.class,
            () -> projectMemberService.removeProjectMember(2L, "owner@example.com"));

        assertEquals("Project owner cannot be removed", exception.getMessage());

        verify(projectMemberRepository).findById(2L);
        verify(projectMemberRepository, never()).delete(any(ProjectMember.class));
    }
}
