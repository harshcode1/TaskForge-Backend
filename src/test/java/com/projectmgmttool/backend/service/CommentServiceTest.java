package com.projectmgmttool.backend.service;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class CommentServiceTest {

    @Test
    void testAddComment() {
        // Arrange
        CommentService commentService = mock(CommentService.class);
        doNothing().when(commentService).addComment(any());

        // Act
        commentService.addComment(null);

        // Assert
        verify(commentService, times(1)).addComment(null);
    }

    @Test
    void testDeleteComment() {
        // Arrange
        CommentService commentService = mock(CommentService.class);
        doNothing().when(commentService).deleteComment(anyLong());

        // Act
        commentService.deleteComment(1L);

        // Assert
        verify(commentService, times(1)).deleteComment(1L);
    }
}
