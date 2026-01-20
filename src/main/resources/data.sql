-- =========================
-- USERS
-- password = 12345678
-- =========================
INSERT INTO users
( username, email, password_hash, full_name, role, status, created_at)
VALUES
    ('admin', 'admin@edutool.com',
     '$2a$10$69BykBSBDqQZuNRwGbAKL.ebf5FfrYZrGuDsnX3Ks4pBGbWETpuBu',
     'System Admin', 'ADMIN', 'ACTIVE', now()),

    ( 'lecturer', 'lecturer@edutool.com',
     '$2a$10$69BykBSBDqQZuNRwGbAKL.ebf5FfrYZrGuDsnX3Ks4pBGbWETpuBu',
     'Nguyen Van A', 'LECTURER', 'ACTIVE', now()),
    ( 'student1', 'student1@edutool.com',
     '$2a$10$69BykBSBDqQZuNRwGbAKL.ebf5FfrYZrGuDsnX3Ks4pBGbWETpuBu',
     'Tran Van B', 'STUDENT', 'ACTIVE', now()),

    ('student2', 'student2@edutool.com',
     '$2a$10$69BykBSBDqQZuNRwGbAKL.ebf5FfrYZrGuDsnX3Ks4pBGbWETpuBu',
     'Le Thi C', 'STUDENT', 'ACTIVE', now());
-- =========================
-- LECTURER
-- =========================
INSERT INTO lecturers (lecturer_id, user_id, staff_code, created_at)
VALUES
    (1, 2, 'GV001', now());

-- =========================
-- STUDENT
-- =========================
INSERT INTO students (student_id, user_id, student_code, github_username, created_at)
VALUES
    (1, 3, 'SE181731', 'student1git', now()),
    (2, 4, 'SE181732', 'student2git', now());

-- =========================
-- SEMESTER
-- =========================
INSERT INTO semesters (semester_id, name, start_date, end_date, created_at)
VALUES
    (1, 'Spring 2026', '2026-01-01', '2026-05-01', now());

-- =========================
-- COURSE
-- =========================
INSERT INTO courses (course_id, course_code, course_name, semester_id, lecturer_id, created_at)
VALUES
    (1, 'PRJ301', 'Java Web Development', 1, 1, now());

-- =========================
-- PROJECT
-- =========================
INSERT INTO projects (project_id, project_code, project_name, course_id, created_at)
VALUES
    (1, 'PRJ301-G1', 'EduTool Management System', 1, now());

-- =========================
-- COURSE ENROLLMENT
-- =========================
INSERT INTO course_enrollments
(enrollment_id, student_id, course_id, project_id, role_in_project, enrolled_at)
VALUES
    (1, 1, 1, 1, 'Leader', now()),
    (2, 2, 1, 1, 'Member', now());

-- =========================
-- PERIODIC REPORT
-- =========================
INSERT INTO periodic_reports
(report_id, course_id, week_number, year, content, created_at)
VALUES
    (1, 1, 1, 2026, 'Week 1: Project planning & requirement analysis', now()),
    (2, 1, 2, 2026, 'Week 2: Database design & architecture', now());

-- =========================
-- GITHUB REPOSITORY
-- =========================
INSERT INTO github_repositories
(repo_id, repo_url, repo_name, owner, is_selected, project_id, created_at)
VALUES
    (1, 'https://github.com/edutool/backend', 'edutool-backend', 'edutool', true, 1, now());

-- =========================
-- JIRA PROJECT
-- =========================
INSERT INTO jira_projects
(jira_project_id, jira_key, project_name, jira_url, project_id)
VALUES
    (1, 'EDU', 'EduTool Jira Project', 'https://jira.edutool.com/browse/EDU', 1);

-- =========================
-- SRS DOCUMENT
-- =========================
INSERT INTO srs_documents
(srs_id, jira_project_id, version, document_link, created_at)
VALUES
    (1, 1, 'v1.0', 'https://docs.edutool.com/srs_v1.pdf', now()),
    (2, 1, 'v1.1', 'https://docs.edutool.com/srs_v1_1.pdf', now());

-- =========================
-- COMMIT CONTRIBUTION
-- =========================
INSERT INTO commit_contributions
(contribution_id, student_id, repo_id, github_author, author_email,
 week_number, year, total_commits, additions, deletions, created_at)
VALUES
    (1, 1, 1, 'student1git', 'student1@github.com',
     1, 2026, 20, 900, 200, now()),

    (2, 2, 1, 'student2git', 'student2@github.com',
     1, 2026, 15, 600, 150, now());

-- =========================
-- COMMIT DETAIL
-- =========================
INSERT INTO commit_details
(commit_id, contribution_id, commit_hash, commit_message,
 files_changed, additions, deletions, committed_at, commit_url)
VALUES
    (1, 1, 'abc123', 'Initial project setup', 10, 300, 50, now(),
     'https://github.com/edutool/backend/commit/abc123'),

    (2, 1, 'def456', 'Add authentication module', 8, 400, 80, now(),
     'https://github.com/edutool/backend/commit/def456'),

    (3, 2, 'ghi789', 'Create database schema', 6, 200, 40, now(),
     'https://github.com/edutool/backend/commit/ghi789');

-- =========================
-- REFRESH TOKENS
-- =========================
-- INSERT INTO refresh_tokens
-- (id, user_id, token_hash, expires_at, revoked)
-- VALUES
--     (1, 3, 'hashed_token_student1', now() + interval '7 days', false),
--     (2, 4, 'hashed_token_student2', now() + interval '7 days', false);
