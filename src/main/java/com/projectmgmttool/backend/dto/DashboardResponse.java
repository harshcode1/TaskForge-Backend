package com.projectmgmttool.backend.dto;

import java.util.Map;

public class DashboardResponse {
    private Map<String, Long> taskStatusCounts; // e.g., { "TODO": 3, "IN_PROGRESS": 2, "DONE": 5 }
    private Map<String, Long> tasksPerUser;     // e.g., { "alice@example.com": 4, "bob@example.com": 2 }

    public DashboardResponse() { }
    public DashboardResponse(Map<String, Long> taskStatusCounts, Map<String, Long> tasksPerUser) {
        this.taskStatusCounts = taskStatusCounts;
        this.tasksPerUser = tasksPerUser;
    }

    public Map<String, Long> getTaskStatusCounts() {
        return taskStatusCounts;
    }

    public Map<String, Long> getTasksPerUser() {
        return tasksPerUser;
    }
}
