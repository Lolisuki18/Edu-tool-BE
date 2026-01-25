-- =========================
-- CREATE TABLES
-- =========================

-- Users table
CREATE TABLE users (
    user_id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(300) NOT NULL,
    role VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Lecturers table
CREATE TABLE lecturers (
    lecturer_id SERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL,
    staff_code VARCHAR(255) UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- Students table
CREATE TABLE students (
    student_id SERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL,
    student_code VARCHAR(255) UNIQUE,
    github_username VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- Semesters table
CREATE TABLE semesters (
    semester_id SERIAL PRIMARY KEY,
    name VARCHAR(255),
    start_date DATE,
    end_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Courses table
CREATE TABLE courses (
    course_id SERIAL PRIMARY KEY,
    course_code VARCHAR(255),
    course_name VARCHAR(255),
    semester_id INTEGER,
    lecturer_id INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (semester_id) REFERENCES semesters(semester_id),
    FOREIGN KEY (lecturer_id) REFERENCES lecturers(lecturer_id)
);

-- Projects table
CREATE TABLE projects (
    project_id SERIAL PRIMARY KEY,
    project_code VARCHAR(255),
    project_name VARCHAR(255),
    course_id INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (course_id) REFERENCES courses(course_id)
);

-- Course Enrollments table
CREATE TABLE course_enrollments (
    enrollment_id SERIAL PRIMARY KEY,
    student_id INTEGER,
    course_id INTEGER,
    project_id INTEGER,
    role_in_project VARCHAR(255),
    enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(student_id),
    FOREIGN KEY (course_id) REFERENCES courses(course_id),
    FOREIGN KEY (project_id) REFERENCES projects(project_id)
);

-- Periodic Reports table
CREATE TABLE periodic_reports (
    report_id SERIAL PRIMARY KEY,
    course_id INTEGER,
    week_number INTEGER,
    year INTEGER,
    content TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (course_id) REFERENCES courses(course_id)
);

-- GitHub Repositories table
CREATE TABLE github_repositories (
    repo_id SERIAL PRIMARY KEY,
    repo_url VARCHAR(255),
    repo_name VARCHAR(255),
    owner VARCHAR(255),
    is_selected BOOLEAN,
    project_id INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(project_id)
);

-- Jira Projects table
CREATE TABLE jira_projects (
    jira_project_id SERIAL PRIMARY KEY,
    jira_key VARCHAR(255),
    project_name VARCHAR(255),
    jira_url VARCHAR(255),
    project_id INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(project_id)
);

-- SRS Documents table
CREATE TABLE srs_documents (
    srs_id SERIAL PRIMARY KEY,
    jira_project_id INTEGER,
    version VARCHAR(255),
    document_link VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (jira_project_id) REFERENCES jira_projects(jira_project_id)
);

-- Commit Contributions table
CREATE TABLE commit_contributions (
    contribution_id SERIAL PRIMARY KEY,
    student_id INTEGER,
    repo_id INTEGER,
    github_author VARCHAR(255),
    author_email VARCHAR(255),
    week_number INTEGER,
    year INTEGER,
    total_commits INTEGER,
    additions INTEGER,
    deletions INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(student_id),
    FOREIGN KEY (repo_id) REFERENCES github_repositories(repo_id)
);

-- Commit Details table
CREATE TABLE commit_details (
    commit_id SERIAL PRIMARY KEY,
    contribution_id INTEGER,
    commit_hash VARCHAR(255),
    commit_message TEXT,
    files_changed INTEGER,
    additions INTEGER,
    deletions INTEGER,
    committed_at TIMESTAMP,
    commit_url VARCHAR(255),
    FOREIGN KEY (contribution_id) REFERENCES commit_contributions(contribution_id)
);

-- Refresh Tokens table
CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP,
    revoked BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- =========================
-- CREATE INDEXES
-- =========================

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_students_user_id ON students(user_id);
CREATE INDEX idx_lecturers_user_id ON lecturers(user_id);
CREATE INDEX idx_courses_semester_id ON courses(semester_id);
CREATE INDEX idx_courses_lecturer_id ON courses(lecturer_id);
CREATE INDEX idx_projects_course_id ON projects(course_id);
CREATE INDEX idx_course_enrollments_student_id ON course_enrollments(student_id);
CREATE INDEX idx_course_enrollments_course_id ON course_enrollments(course_id);
CREATE INDEX idx_course_enrollments_project_id ON course_enrollments(project_id);
CREATE INDEX idx_github_repositories_project_id ON github_repositories(project_id);
CREATE INDEX idx_jira_projects_project_id ON jira_projects(project_id);
CREATE INDEX idx_commit_contributions_student_id ON commit_contributions(student_id);
CREATE INDEX idx_commit_contributions_repo_id ON commit_contributions(repo_id);
CREATE INDEX idx_commit_details_contribution_id ON commit_details(contribution_id);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
