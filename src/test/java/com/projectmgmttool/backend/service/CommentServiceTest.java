package com.projectmgmttool.backend.service;

import com.projectmgmttool.backend.dto.CommentRequest;
import com.projectmgmttool.backend.entity.Comment;
import com.projectmgmttool.backend.entity.Task;
import com.projectmgmttool.backend.entity.User;
import com.projectmgmttool.backend.exception.CustomApiException;
import com.projectmgmttool.backend.repository.CommentRepository;
import com.projectmgmttool.backend.repository.TaskRepository;
import com.projectmgmttool.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommentService commentService;

    private CommentRequest commentRequest;
    private Comment comment;
    private Task task;
    private User user;
    private UUID commentId;
    private UUID taskId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        commentId = UUID.randomUUID();
        taskId = UUID.randomUUID();
        userId = UUID.randomUUID();

        // Setup User
        user = new User();
        user.setId(userId);
        user.setName("John Doe");
        user.setEmail("john@example.com");

        // Setup Task
        task = new Task();
        task.setId(taskId);
        task.setTitle("Test Task");

        // Setup CommentRequest
        commentRequest = new CommentRequest();
        commentRequest.setContent("Test Comment");
        commentRequest.setTaskId(taskId);

        // Setup Comment
        comment = new Comment();
        comment.setId(commentId);
        comment.setContent("Test Comment");
        comment.setAuthor(user);
        comment.setTask(task);
        comment.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createComment_Success_ReturnsComment() {
        // Arrange
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        // Act
        Comment result = commentService.createComment(commentRequest, "john@example.com");

        // Assert
        assertNotNull(result);
        assertEquals("Test Comment", result.getContent());
        assertEquals(user, result.getAuthor());
        assertEquals(task, result.getTask());
        assertNotNull(result.getCreatedAt());

        verify(taskRepository).findById(taskId);
        verify(userRepository).findByEmail("john@example.com");
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void createComment_TaskNotFound_ThrowsCustomApiException() {
        // Arrange
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        // Act & Assert
        CustomApiException exception = assertThrows(CustomApiException.class,
            () -> commentService.createComment(commentRequest, "john@example.com"));

        assertEquals("Task not found", exception.getMessage());

        verify(taskRepository).findById(taskId);
        verify(userRepository, never()).findByEmail(anyString());
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void createComment_UserNotFound_ThrowsCustomApiException() {
        // Arrange
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        CustomApiException exception = assertThrows(CustomApiException.class,
            () -> commentService.createComment(commentRequest, "john@example.com"));

        assertEquals("User not found", exception.getMessage());

        verify(taskRepository).findById(taskId);
        verify(userRepository).findByEmail("john@example.com");
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void createComment_NullContent_ThrowsException() {
        // Arrange
        commentRequest.setContent(null);

        // Act & Assert
        assertThrows(Exception.class,
            () -> commentService.createComment(commentRequest, "john@example.com"));
    }

    @Test
    void createComment_EmptyContent_ThrowsException() {
        // Arrange
        commentRequest.setContent("");

        // Act & Assert
        assertThrows(Exception.class,
            () -> commentService.createComment(commentRequest, "john@example.com"));
    }

    @Test
    void getCommentsForTask_Success_ReturnsCommentList() {
        // Arrange
        List<Comment> comments = Arrays.asList(comment);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(commentRepository.findByTaskIdOrderByCreatedAtDesc(taskId)).thenReturn(comments);

        // Act
        List<Comment> result = commentService.getCommentsForTask(taskId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(comment, result.get(0));

        verify(taskRepository).findById(taskId);
        verify(commentRepository).findByTaskIdOrderByCreatedAtDesc(taskId);
    }

    @Test
    void getCommentsForTask_TaskNotFound_ThrowsCustomApiException() {
        // Arrange
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        // Act & Assert
        CustomApiException exception = assertThrows(CustomApiException.class,
            () -> commentService.getCommentsForTask(taskId));

        assertEquals("Task not found", exception.getMessage());

        verify(taskRepository).findById(taskId);
        verify(commentRepository, never()).findByTaskIdOrderByCreatedAtDesc(any(UUID.class));
    }

    @Test
    void getCommentsForTask_EmptyList_ReturnsEmptyList() {
        // Arrange
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(commentRepository.findByTaskIdOrderByCreatedAtDesc(taskId)).thenReturn(Collections.emptyList());

        // Act
        List<Comment> result = commentService.getCommentsForTask(taskId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(taskRepository).findById(taskId);
        verify(commentRepository).findByTaskIdOrderByCreatedAtDesc(taskId);
    }

    @Test
    void deleteComment_Success_DeletesComment() {
        // Arrange
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        doNothing().when(commentRepository).delete(comment);

        // Act
        assertDoesNotThrow(() -> commentService.deleteComment(commentId, "john@example.com"));

        // Assert
        verify(commentRepository).findById(commentId);
        verify(commentRepository).delete(comment);
    }

    @Test
    void deleteComment_CommentNotFound_ThrowsCustomApiException() {
        // Arrange
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        // Act & Assert
        CustomApiException exception = assertThrows(CustomApiException.class,
            () -> commentService.deleteComment(commentId, "john@example.com"));

        assertEquals("Comment not found", exception.getMessage());

        verify(commentRepository).findById(commentId);
        verify(commentRepository, never()).delete(any(Comment.class));
    }

    @Test
    void deleteComment_NotAuthor_ThrowsCustomApiException() {
        // Arrange
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        // Act & Assert
        CustomApiException exception = assertThrows(CustomApiException.class,
            () -> commentService.deleteComment(commentId, "other@example.com"));

        assertEquals("Only the author can delete the comment", exception.getMessage());

        verify(commentRepository).findById(commentId);
        verify(commentRepository, never()).delete(any(Comment.class));
    }

    @Test
    void deleteComment_NullCommentId_ThrowsException() {
        // Act & Assert
        assertThrows(Exception.class,
            () -> commentService.deleteComment(null, "john@example.com"));
    }
}
