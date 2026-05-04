package com.taskmanager.service;

import com.taskmanager.dto.AuthDto;
import com.taskmanager.dto.ProjectDto;
import com.taskmanager.entity.Project;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import com.taskmanager.repository.ProjectRepository;
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
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final AuthService authService;

    @Transactional
    public ProjectDto.ProjectResponse createProject(ProjectDto.CreateRequest request) {
        User currentUser = authService.getCurrentUser();
        Project project = Project.builder()
            .name(request.getName())
            .description(request.getDescription())
            .owner(currentUser)
            .build();
        project.getMembers().add(currentUser);
        projectRepository.save(project);
        return toResponse(project);
    }

    public List<ProjectDto.ProjectResponse> getMyProjects() {
        User currentUser = authService.getCurrentUser();
        List<Project> projects = currentUser.getGlobalRole() == User.Role.ADMIN
            ? projectRepository.findAll()
            : projectRepository.findAllProjectsForUser(currentUser);
        return projects.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public ProjectDto.ProjectResponse getProjectById(Long id) {
        Project project = findProjectAndCheckAccess(id);
        return toResponse(project);
    }

    @Transactional
    public ProjectDto.ProjectResponse updateProject(Long id, ProjectDto.UpdateRequest request) {
        Project project = findProjectAndCheckOwnerOrAdmin(id);
        if (request.getName() != null) project.setName(request.getName());
        if (request.getDescription() != null) project.setDescription(request.getDescription());
        if (request.getStatus() != null) project.setStatus(request.getStatus());
        projectRepository.save(project);
        return toResponse(project);
    }

    @Transactional
    public void deleteProject(Long id) {
        Project project = findProjectAndCheckOwnerOrAdmin(id);
        projectRepository.delete(project);
    }

    @Transactional
    public ProjectDto.ProjectResponse addMember(Long projectId, Long userId) {
        Project project = findProjectAndCheckOwnerOrAdmin(projectId);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        project.getMembers().add(user);
        projectRepository.save(project);
        return toResponse(project);
    }

    @Transactional
    public ProjectDto.ProjectResponse removeMember(Long projectId, Long userId) {
        Project project = findProjectAndCheckOwnerOrAdmin(projectId);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        if (project.getOwner().getId().equals(userId)) {
            throw new RuntimeException("Cannot remove project owner");
        }
        project.getMembers().remove(user);
        projectRepository.save(project);
        return toResponse(project);
    }

    public Project findProjectAndCheckAccess(Long projectId) {
        User currentUser = authService.getCurrentUser();
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found"));
        if (currentUser.getGlobalRole() == User.Role.ADMIN) return project;
        boolean isMember = project.getOwner().getId().equals(currentUser.getId())
            || project.getMembers().stream().anyMatch(m -> m.getId().equals(currentUser.getId()));
        if (!isMember) throw new RuntimeException("Access denied");
        return project;
    }

    private Project findProjectAndCheckOwnerOrAdmin(Long projectId) {
        User currentUser = authService.getCurrentUser();
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found"));
        if (currentUser.getGlobalRole() == User.Role.ADMIN) return project;
        if (!project.getOwner().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Only project owner or admin can perform this action");
        }
        return project;
    }

    public ProjectDto.ProjectResponse toResponse(Project project) {
        long total = taskRepository.countByProjectId(project.getId());
        long completed = taskRepository.countByProjectIdAndStatus(project.getId(), Task.Status.DONE);
        long overdue = project.getTasks().stream()
            .filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(LocalDate.now()) && t.getStatus() != Task.Status.DONE)
            .count();

        List<AuthDto.UserResponse> members = project.getMembers().stream()
            .map(m -> new AuthDto.UserResponse(m.getId(), m.getName(), m.getEmail(), m.getGlobalRole()))
            .collect(Collectors.toList());

        AuthDto.UserResponse ownerDto = new AuthDto.UserResponse(
            project.getOwner().getId(), project.getOwner().getName(),
            project.getOwner().getEmail(), project.getOwner().getGlobalRole()
        );

        return ProjectDto.ProjectResponse.builder()
            .id(project.getId())
            .name(project.getName())
            .description(project.getDescription())
            .status(project.getStatus())
            .owner(ownerDto)
            .members(members)
            .totalTasks(total)
            .completedTasks(completed)
            .overdueTasks(overdue)
            .createdAt(project.getCreatedAt())
            .build();
    }
}
