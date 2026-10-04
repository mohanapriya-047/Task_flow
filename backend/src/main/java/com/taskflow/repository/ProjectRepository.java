package com.taskflow.repository;

import com.taskflow.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByManagerId(Long managerId);

    @org.springframework.data.jpa.repository.Query(
        "select distinct p from Project p join Task t on t.project = p where t.assignedUser.id = :userId")
    List<Project> findProjectsAssignedToUser(Long userId);
}
