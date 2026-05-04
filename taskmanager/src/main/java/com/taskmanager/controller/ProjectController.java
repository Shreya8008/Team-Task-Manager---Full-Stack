package com.taskmanager.controller;

import com.taskmanager.dto.ProjectDto;
import com.taskmanager.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ProjectDto.ProjectResponse> create(@Valid @RequestBody ProjectDto.CreateRequest request) {
        return ResponseEntity.ok(projectService.createProject(request));
    }

    @GetMapping
    public ResponseEntity<List<ProjectDto.ProjectResponse>> getAll() {
        return ResponseEntity.ok(projectService.getMyProjects());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectDto.ProjectResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectDto.ProjectResponse> update(@PathVariable Long id,
                                                              @RequestBody ProjectDto.UpdateRequest request) {
        return ResponseEntity.ok(projectService.updateProject(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{projectId}/members")
    public ResponseEntity<ProjectDto.ProjectResponse> addMember(@PathVariable Long projectId,
                                                                 @RequestBody ProjectDto.AddMemberRequest request) {
        return ResponseEntity.ok(projectService.addMember(projectId, request.getUserId()));
    }

    @DeleteMapping("/{projectId}/members/{userId}")
    public ResponseEntity<ProjectDto.ProjectResponse> removeMember(@PathVariable Long projectId,
                                                                    @PathVariable Long userId) {
        return ResponseEntity.ok(projectService.removeMember(projectId, userId));
    }
}
