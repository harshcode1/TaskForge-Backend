package com.projectmgmttool.backend.repository;

import com.projectmgmttool.backend.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findByProjectId(UUID projectId);
    List<Task> findByProjectIdAndStatus(UUID projectId, String status);
    List<Task> findByAssigneeEmail(String email);
    List<Task> findByDueDateBetweenAndAssigneeEmail(LocalDate start, LocalDate end, String email);
}
