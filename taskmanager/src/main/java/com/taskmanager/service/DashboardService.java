package com.taskmanager.service;

import com.taskmanager.dto.DashboardDto;
import com.taskmanager.dto.ProjectDto;
import com.taskmanager.dto.TaskDto;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import com.taskmanager.repository.ProjectRepository;
import com.taskmanager.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final AuthService authService;
    private final TaskService taskService;
    private final ProjectService projectService;

    public DashboardDto getDashboard() {
        User currentUser = authService.getCurrentUser();

        List<Task> allTasks = currentUser.getGlobalRole() == User.Role.ADMIN
            ? taskRepository.findAll()
            : taskRepository.findAllTasksForUser(currentUser);

        long totalProjects = currentUser.getGlobalRole() == User.Role.ADMIN
            ? projectRepository.count()
            : projectRepository.findAllProjectsForUser(currentUser).size();

        long completedTasks = allTasks.stream().filter(t -> t.getStatus() == Task.Status.DONE).count();
        long inProgressTasks = allTasks.stream().filter(t -> t.getStatus() == Task.Status.IN_PROGRESS).count();
        long todoTasks = allTasks.stream().filter(t -> t.getStatus() == Task.Status.TODO).count();

        List<Task> overdueTasks = allTasks.stream()
            .filter(t -> t.getDueDate() != null
                && t.getDueDate().isBefore(LocalDate.now())
                && t.getStatus() != Task.Status.DONE)
            .collect(Collectors.toList());

        Map<String, Long> tasksByStatus = allTasks.stream()
            .collect(Collectors.groupingBy(t -> t.getStatus().name(), Collectors.counting()));

        Map<String, Long> tasksByPriority = allTasks.stream()
            .collect(Collectors.groupingBy(t -> t.getPriority().name(), Collectors.counting()));

        List<TaskDto.TaskResponse> recentTasks = allTasks.stream()
            .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
            .limit(5)
            .map(taskService::toResponse)
            .collect(Collectors.toList());

        List<TaskDto.TaskResponse> overdueTaskDtos = overdueTasks.stream()
            .map(taskService::toResponse)
            .collect(Collectors.toList());

        List<ProjectDto.ProjectResponse> recentProjects = (
            currentUser.getGlobalRole() == User.Role.ADMIN
                ? projectRepository.findAll()
                : projectRepository.findAllProjectsForUserOrderByDate(currentUser)
        ).stream()
            .limit(5)
            .map(projectService::toResponse)
            .collect(Collectors.toList());

        return DashboardDto.builder()
            .totalProjects(totalProjects)
            .totalTasks(allTasks.size())
            .completedTasks(completedTasks)
            .inProgressTasks(inProgressTasks)
            .todoTasks(todoTasks)
            .overdueTasks(overdueTasks.size())
            .tasksByStatus(tasksByStatus)
            .tasksByPriority(tasksByPriority)
            .recentTasks(recentTasks)
            .overdueTaskList(overdueTaskDtos)
            .recentProjects(recentProjects)
            .build();
    }
}
