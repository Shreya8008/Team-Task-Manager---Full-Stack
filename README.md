⚡ TaskFlow
Team Task Manager — Full Stack Web Application
Built with Spring Boot 3 • JWT Auth • PostgreSQL • Role-Based Access Control

1. Project Overview
TaskFlow is a full-stack team task management web application that allows teams to create projects, assign tasks, and track progress with role-based access control. The application features a modern dark-themed UI, JWT-based authentication, and a RESTful API backend.

Tech Stack
Layer	Technology	Purpose
Backend	Spring Boot 3.2 (Java 17)	REST API, Business Logic
Security	Spring Security + JWT	Authentication & Authorization
Database (Dev)	H2 In-Memory	Local development & testing
Database (Prod)	PostgreSQL (Railway)	Production deployment
ORM	Spring Data JPA / Hibernate	Database operations
Frontend	HTML5 + CSS3 + Vanilla JS	Single Page Application
Build Tool	Maven	Dependency management
Deployment	Railway.app	Cloud hosting


2. Key Features
Authentication & Authorization
•	JWT-based stateless authentication (signup & login)
•	Password hashing with BCrypt
•	First registered user automatically becomes ADMIN
•	All subsequent users are MEMBER by default
•	Protected routes — all API endpoints require valid JWT token

Role-Based Access Control (RBAC)
Feature	ADMIN	MEMBER
View all projects	✅ Yes	✅ Own/member projects only
Create projects	✅ Yes	✅ Yes
Delete any project	✅ Yes	❌ Owner only
Add/remove members	✅ Yes	✅ Owner only
Create tasks	✅ Yes	✅ Yes (own projects)
Edit all task fields	✅ Yes	⚠️ Status only
Delete tasks	✅ Yes	❌ No
View all users	✅ Yes	✅ Yes
Team Members page	✅ Yes	❌ Hidden

Project Management
•	Create, update, delete projects
•	Add and remove team members per project
•	Project status tracking: ACTIVE, COMPLETED, ARCHIVED
•	Progress bar showing completed vs total tasks

Task Management
•	Full task lifecycle: TODO → IN_PROGRESS → IN_REVIEW → DONE
•	Priority levels: LOW, MEDIUM, HIGH, CRITICAL
•	Due date tracking with automatic overdue detection
•	Assign tasks to team members
•	Kanban board view per project

Dashboard
•	Stats: Total Projects, Total Tasks, Completed, Overdue
•	Recent tasks list with status and priority
•	Overdue tasks highlighted in red
•	Recent projects with progress

3. REST API Reference
Base URL: http://localhost:8080 (dev) or https://your-app.up.railway.app (prod)
All endpoints except /api/auth/** require: Authorization: Bearer <token>

Authentication Endpoints
Method	Endpoint	Body	Description
POST	/api/auth/signup	{name, email, password}	Register new user, returns JWT
POST	/api/auth/login	{email, password}	Login, returns JWT token
GET	/api/auth/me	—	Get current logged-in user
GET	/api/auth/users	—	Get all users in system

Project Endpoints
Method	Endpoint	Body	Description
GET	/api/projects	—	Get all accessible projects
POST	/api/projects	{name, description}	Create new project
GET	/api/projects/{id}	—	Get project by ID
PUT	/api/projects/{id}	{name, description, status}	Update project
DELETE	/api/projects/{id}	—	Delete project (owner/admin)
POST	/api/projects/{id}/members	{userId}	Add member to project
DELETE	/api/projects/{id}/members/{uid}	—	Remove member from project

Task Endpoints
Method	Endpoint	Body	Description
GET	/api/tasks	—	Get my assigned tasks
POST	/api/tasks	{title, projectId, ...}	Create new task
GET	/api/tasks/{id}	—	Get task by ID
GET	/api/tasks/project/{id}	—	Get all tasks in a project
PUT	/api/tasks/{id}	{title, status, priority, ...}	Update task
DELETE	/api/tasks/{id}	—	Delete task (owner/admin)

Dashboard Endpoint
Method	Endpoint	Body	Description
GET	/api/dashboard	—	Get full dashboard stats + recent data

4. Database Schema
Entities & Relationships
•	User — id, name, email, password (BCrypt), globalRole (ADMIN/MEMBER), createdAt
•	Project — id, name, description, status, owner (FK→User), members (M2M→User), createdAt
•	Task — id, title, description, status, priority, dueDate, project (FK), createdBy (FK→User), assignedTo (FK→User), createdAt, updatedAt
•	project_members — join table (project_id, user_id)

Status Enums
•	Task Status: TODO | IN_PROGRESS | IN_REVIEW | DONE
•	Task Priority: LOW | MEDIUM | HIGH | CRITICAL
•	Project Status: ACTIVE | COMPLETED | ARCHIVED
•	User Role: ADMIN | MEMBER

5. Local Setup & Running
Prerequisites
•	Java 17 (https://adoptium.net)
•	Maven 3.8+ (or use included mvnw wrapper)
•	IntelliJ IDEA (recommended)

Steps to Run Locally
1.	Extract the taskmanager-backend.zip
2.	Open the taskmanager/ folder in IntelliJ IDEA
3.	Wait for Maven to download dependencies
4.	Install Lombok plugin: Settings → Plugins → search Lombok
5.	Enable annotation processing: Settings → Build → Compiler → Annotation Processors
6.	Run TaskManagerApplication.java (green play button)
7.	Open http://localhost:8080 in browser

Demo Credentials (auto-seeded)
Admin:  admin@demo.com  /  admin123
Member: member@demo.com /  member123

6. Railway Deployment Guide
Step-by-Step
8.	Push the taskmanager/ folder to a GitHub repository
9.	Go to https://railway.app and sign up / log in
10.	Click New Project → Deploy from GitHub repo → select your repo
11.	In the Railway project, click + New → Database → Add PostgreSQL
12.	Go to your app service → Variables tab → Add these variables:

SPRING_PROFILES_ACTIVE = prod
JWT_SECRET = MySuperSecretLongKey2024TaskFlowApp
DATABASE_URL = (Railway fills this automatically from PostgreSQL plugin)

13.	Railway will auto-build and deploy your app
14.	Click the generated domain URL (e.g. https://taskflow-xxx.up.railway.app)
15.	Your app is now live and publicly accessible!

Note: The DATABASE_URL environment variable is automatically injected by Railway when you add a PostgreSQL plugin. You do not need to set it manually.

7. Testing the Application
Frontend Testing (Browser)
16.	Go to http://localhost:8080 (or your Railway URL)
17.	Sign up a new account (first user = ADMIN automatically)
18.	Or login with demo: admin@demo.com / admin123
19.	Create a project from the Projects page
20.	Add a task with due date, priority, and assignee
21.	Change task status via the Kanban board or Edit button
22.	Check the Dashboard for stats and overdue tasks

API Testing (Postman)
23.	POST /api/auth/login with {email, password} → copy the token
24.	Add header: Authorization: Bearer <token> to all requests
25.	POST /api/projects to create a project
26.	POST /api/tasks with projectId to create a task
27.	GET /api/dashboard to see all stats

H2 Database Console (Dev Only)
URL: http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:taskmanagerdb
Username: sa   |   Password: (leave empty)

8. Project File Structure
taskmanager/
├── pom.xml                          Maven build config
├── src/main/java/com/taskmanager/
│   ├── TaskManagerApplication.java  Entry point
│   ├── entity/                      JPA Entities (User, Project, Task)
│   ├── repository/                  Spring Data JPA Repositories
│   ├── service/                     Business Logic
│   ├── controller/                  REST Controllers
│   ├── dto/                         Request/Response DTOs
│   ├── security/                    JWT + Spring Security
│   └── config/                      Security, Exception Handler, Seeder
└── src/main/resources/
    ├── application.properties       Main config
    ├── application-dev.properties   H2 config (local)
    ├── application-prod.properties  PostgreSQL config (Railway)
    └── static/index.html            Frontend SPA

9. Submission Checklist
•	Live URL (Railway deployed app)
•	GitHub Repository link
•	README file (this document)
•	2-5 minute demo video

What to show in demo video
28.	Open the live URL — show it's publicly accessible
29.	Sign up as a new user (show it becomes ADMIN)
30.	Create a project and add a team member
31.	Create tasks with different priorities and due dates
32.	Move tasks through the Kanban board
33.	Show the Dashboard with stats and overdue tasks
34.	Login as member — show restricted access

TaskFlow — Built with using Spring Boot & Java 17
