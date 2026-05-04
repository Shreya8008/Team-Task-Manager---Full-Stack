package com.taskmanager.repository;

import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByProjectId(Long projectId);

    List<Task> findByAssignedTo(User user);

    List<Task> findByAssignedToAndStatus(User user, Task.Status status);

    @Query("SELECT t FROM Task t WHERE t.assignedTo = :user AND t.dueDate < :today AND t.status != 'DONE'")
    List<Task> findOverdueTasksForUser(@Param("user") User user, @Param("today") LocalDate today);

    @Query("SELECT t FROM Task t WHERE t.project.id IN " +
           "(SELECT p.id FROM Project p WHERE p.owner = :user OR :user MEMBER OF p.members) " +
           "AND t.dueDate < :today AND t.status != 'DONE'")
    List<Task> findOverdueTasksInUserProjects(@Param("user") User user, @Param("today") LocalDate today);

    @Query("SELECT t FROM Task t WHERE t.project.id IN " +
           "(SELECT p.id FROM Project p WHERE p.owner = :user OR :user MEMBER OF p.members)")
    List<Task> findAllTasksForUser(@Param("user") User user);

    long countByProjectId(Long projectId);

    long countByProjectIdAndStatus(Long projectId, Task.Status status);
}
