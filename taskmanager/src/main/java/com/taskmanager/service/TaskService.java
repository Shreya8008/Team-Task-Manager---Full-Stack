package com.taskmanager.service;

import com.taskmanager.dto.AuthDto;
import com.taskmanager.dto.TaskDto;
import com.taskmanager.entity.Project;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final ProjectService projectService;

    @Transactional
    public TaskDto.TaskResponse createTask(TaskDto.CreateRequest request) {
        User currentUser = authService.getCurrentUser();
        Project project = projectService.findProjectAndCheckAccess(request.getProjectId());

        User assignedTo = null;
        if (request.getAssignedToId() != null) {
            assignedTo = userRepository.findById(request.getAssignedToId())
                .orElseThrow(() -> new RuntimeException("Assigned user not found"));
        }

        Task task = Task.builder()
            .title(request.getTitle())
            .description(request.getDescription())
            .project(project)
            .createdBy(currentUser)
            .assignedTo(assignedTo)
            .priority(request.getPriority() != null ? request.getPriority() : Task.Priority.MEDIUM)
            .dueDate(request.getDueDate())
            .build();

        taskRepository.save(task);
        return toResponse(task);
    }

    public List<TaskDto.TaskResponse> getTasksByProject(Long projectId) {
        projectService.findProjectAndCheckAccess(projectId);
        return taskRepository.findByProjectId(projectId).stream()
            .map(this::toResponse).collect(Collectors.toList());
    }

    public List<TaskDto.TaskResponse> getMyTasks() {
        User currentUser = authService.getCurrentUser();
        List<Task> tasks = currentUser.getGlobalRole() == User.Role.ADMIN
            ? taskRepository.findAll()
            : taskRepository.findByAssignedTo(currentUser);
        return tasks.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public TaskDto.TaskResponse getTaskById(Long id) {
        Task task = findTaskAndCheckAccess(id);
        return toResponse(task);
    }

    @Transactional
    public TaskDto.TaskResponse updateTask(Long id, TaskDto.UpdateRequest request) {
        User currentUser = authService.getCurrentUser();
        Task task = findTaskAndCheckAccess(id);

        boolean isOwnerOrAdmin = currentUser.getGlobalRole() == User.Role.ADMIN
            || task.getProject().getOwner().getId().equals(currentUser.getId());

        // Members can only update status
        if (!isOwnerOrAdmin) {
            if (request.getStatus() != null) task.setStatus(request.getStatus());
        } else {
            if (request.getTitle() != null) task.setTitle(request.getTitle());
            if (request.getDescription() != null) task.setDescription(request.getDescription());
            if (request.getStatus() != null) task.setStatus(request.getStatus());
            if (request.getPriority() != null) task.setPriority(request.getPriority());
            if (request.getDueDate() != null) task.setDueDate(request.getDueDate());
            if (request.getAssignedToId() != null) {
                User assignedTo = userRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new RuntimeException("Assigned user not found"));
                task.setAssignedTo(assignedTo);
            }
        }

        taskRepository.save(task);
        return toResponse(task);
    }

    @Transactional
    public void deleteTask(Long id) {
        User currentUser = authService.getCurrentUser();
        Task task = findTaskAndCheckAccess(id);
        boolean isOwnerOrAdmin = currentUser.getGlobalRole() == User.Role.ADMIN
            || task.getProject().getOwner().getId().equals(currentUser.getId());
        if (!isOwnerOrAdmin) throw new RuntimeException("Not authorized to delete this task");
        taskRepository.delete(task);
    }

    private Task findTaskAndCheckAccess(Long taskId) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new RuntimeException("Task not found"));
        projectService.findProjectAndCheckAccess(task.getProject().getId());
        return task;
    }

    public TaskDto.TaskResponse toResponse(Task task) {
        AuthDto.UserResponse createdByDto = null;
        if (task.getCreatedBy() != null) {
            createdByDto = new AuthDto.UserResponse(
                task.getCreatedBy().getId(), task.getCreatedBy().getName(),
                task.getCreatedBy().getEmail(), task.getCreatedBy().getGlobalRole()
            );
        }

        AuthDto.UserResponse assignedToDto = null;
        if (task.getAssignedTo() != null) {
            assignedToDto = new AuthDto.UserResponse(
                task.getAssignedTo().getId(), task.getAssignedTo().getName(),
                task.getAssignedTo().getEmail(), task.getAssignedTo().getGlobalRole()
            );
        }

        boolean overdue = task.getDueDate() != null
            && task.getDueDate().isBefore(LocalDate.now())
            && task.getStatus() != Task.Status.DONE;

        return TaskDto.TaskResponse.builder()
            .id(task.getId())
            .title(task.getTitle())
            .description(task.getDescription())
            .status(task.getStatus())
            .priority(task.getPriority())
            .dueDate(task.getDueDate())
            .overdue(overdue)
            .projectId(task.getProject().getId())
            .projectName(task.getProject().getName())
            .createdBy(createdByDto)
            .assignedTo(assignedToDto)
            .createdAt(task.getCreatedAt())
            .updatedAt(task.getUpdatedAt())
            .build();
    }
}
