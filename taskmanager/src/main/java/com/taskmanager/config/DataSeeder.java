package com.taskmanager.config;

import com.taskmanager.entity.Project;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import com.taskmanager.repository.ProjectRepository;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    @Bean
    @Profile("dev")
    CommandLineRunner seedData(UserRepository userRepo, ProjectRepository projectRepo,
                               TaskRepository taskRepo, PasswordEncoder encoder) {
        return args -> {
            if (userRepo.count() > 0) return;

            log.info("Seeding demo data...");

            // Create admin
            User admin = User.builder()
                .name("Admin User")
                .email("admin@demo.com")
                .password(encoder.encode("admin123"))
                .globalRole(User.Role.ADMIN)
                .build();
            userRepo.save(admin);

            // Create member
            User member = User.builder()
                .name("John Member")
                .email("member@demo.com")
                .password(encoder.encode("member123"))
                .globalRole(User.Role.MEMBER)
                .build();
            userRepo.save(member);

            // Create project
            Project project = Project.builder()
                .name("Demo Project")
                .description("A sample project to get you started")
                .owner(admin)
                .build();
            project.getMembers().add(admin);
            project.getMembers().add(member);
            projectRepo.save(project);

            // Create tasks
            Task t1 = Task.builder()
                .title("Set up project repository")
                .description("Initialize Git repository and configure CI/CD")
                .project(project).createdBy(admin).assignedTo(admin)
                .status(Task.Status.DONE).priority(Task.Priority.HIGH)
                .dueDate(LocalDate.now().minusDays(2)).build();

            Task t2 = Task.builder()
                .title("Design database schema")
                .description("Create ERD and define all relationships")
                .project(project).createdBy(admin).assignedTo(member)
                .status(Task.Status.IN_PROGRESS).priority(Task.Priority.HIGH)
                .dueDate(LocalDate.now().plusDays(3)).build();

            Task t3 = Task.builder()
                .title("Implement REST APIs")
                .description("Build all backend endpoints with validation")
                .project(project).createdBy(admin).assignedTo(member)
                .status(Task.Status.TODO).priority(Task.Priority.MEDIUM)
                .dueDate(LocalDate.now().plusDays(7)).build();

            Task t4 = Task.builder()
                .title("Write unit tests")
                .description("Achieve minimum 80% code coverage")
                .project(project).createdBy(admin).assignedTo(admin)
                .status(Task.Status.TODO).priority(Task.Priority.LOW)
                .dueDate(LocalDate.now().minusDays(1)).build(); // overdue

            taskRepo.save(t1);
            taskRepo.save(t2);
            taskRepo.save(t3);
            taskRepo.save(t4);

            log.info("✅ Demo data seeded!");
            log.info("Admin: admin@demo.com / admin123");
            log.info("Member: member@demo.com / member123");
        };
    }
}
