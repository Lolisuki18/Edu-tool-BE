-- =========================
-- ADD ON DELETE CASCADE TO ALL FOREIGN KEYS
-- =========================

-- Drop existing foreign key constraints and recreate with ON DELETE CASCADE

-- Lecturers table
ALTER TABLE lecturers DROP CONSTRAINT IF EXISTS lecturers_user_id_fkey;
ALTER TABLE lecturers 
    ADD CONSTRAINT lecturers_user_id_fkey 
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE;

-- Students table
ALTER TABLE students DROP CONSTRAINT IF EXISTS students_user_id_fkey;
ALTER TABLE students 
    ADD CONSTRAINT students_user_id_fkey 
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE;

-- Courses table
ALTER TABLE courses DROP CONSTRAINT IF EXISTS courses_semester_id_fkey;
ALTER TABLE courses 
    ADD CONSTRAINT courses_semester_id_fkey 
    FOREIGN KEY (semester_id) REFERENCES semesters(semester_id) ON DELETE CASCADE;

ALTER TABLE courses DROP CONSTRAINT IF EXISTS courses_lecturer_id_fkey;
ALTER TABLE courses 
    ADD CONSTRAINT courses_lecturer_id_fkey 
    FOREIGN KEY (lecturer_id) REFERENCES lecturers(lecturer_id) ON DELETE CASCADE;

-- Projects table
ALTER TABLE projects DROP CONSTRAINT IF EXISTS projects_course_id_fkey;
ALTER TABLE projects 
    ADD CONSTRAINT projects_course_id_fkey 
    FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE;

-- Course Enrollments table
ALTER TABLE course_enrollments DROP CONSTRAINT IF EXISTS course_enrollments_student_id_fkey;
ALTER TABLE course_enrollments 
    ADD CONSTRAINT course_enrollments_student_id_fkey 
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE;

ALTER TABLE course_enrollments DROP CONSTRAINT IF EXISTS course_enrollments_course_id_fkey;
ALTER TABLE course_enrollments 
    ADD CONSTRAINT course_enrollments_course_id_fkey 
    FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE;

ALTER TABLE course_enrollments DROP CONSTRAINT IF EXISTS course_enrollments_project_id_fkey;
ALTER TABLE course_enrollments 
    ADD CONSTRAINT course_enrollments_project_id_fkey 
    FOREIGN KEY (project_id) REFERENCES projects(project_id) ON DELETE CASCADE;

-- Periodic Reports table
ALTER TABLE periodic_reports DROP CONSTRAINT IF EXISTS periodic_reports_course_id_fkey;
ALTER TABLE periodic_reports 
    ADD CONSTRAINT periodic_reports_course_id_fkey 
    FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE;

-- GitHub Repositories table
ALTER TABLE github_repositories DROP CONSTRAINT IF EXISTS github_repositories_project_id_fkey;
ALTER TABLE github_repositories 
    ADD CONSTRAINT github_repositories_project_id_fkey 
    FOREIGN KEY (project_id) REFERENCES projects(project_id) ON DELETE CASCADE;

-- Jira Projects table
ALTER TABLE jira_projects DROP CONSTRAINT IF EXISTS jira_projects_project_id_fkey;
ALTER TABLE jira_projects 
    ADD CONSTRAINT jira_projects_project_id_fkey 
    FOREIGN KEY (project_id) REFERENCES projects(project_id) ON DELETE CASCADE;

-- SRS Documents table
ALTER TABLE srs_documents DROP CONSTRAINT IF EXISTS srs_documents_jira_project_id_fkey;
ALTER TABLE srs_documents 
    ADD CONSTRAINT srs_documents_jira_project_id_fkey 
    FOREIGN KEY (jira_project_id) REFERENCES jira_projects(jira_project_id) ON DELETE CASCADE;

-- Commit Contributions table
ALTER TABLE commit_contributions DROP CONSTRAINT IF EXISTS commit_contributions_student_id_fkey;
ALTER TABLE commit_contributions 
    ADD CONSTRAINT commit_contributions_student_id_fkey 
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE;

ALTER TABLE commit_contributions DROP CONSTRAINT IF EXISTS commit_contributions_repo_id_fkey;
ALTER TABLE commit_contributions 
    ADD CONSTRAINT commit_contributions_repo_id_fkey 
    FOREIGN KEY (repo_id) REFERENCES github_repositories(repo_id) ON DELETE CASCADE;

-- Commit Details table
ALTER TABLE commit_details DROP CONSTRAINT IF EXISTS commit_details_contribution_id_fkey;
ALTER TABLE commit_details 
    ADD CONSTRAINT commit_details_contribution_id_fkey 
    FOREIGN KEY (contribution_id) REFERENCES commit_contributions(contribution_id) ON DELETE CASCADE;

-- Refresh Tokens table
ALTER TABLE refresh_tokens DROP CONSTRAINT IF EXISTS refresh_tokens_user_id_fkey;
ALTER TABLE refresh_tokens 
    ADD CONSTRAINT refresh_tokens_user_id_fkey 
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE;
