-- ========================================
-- Add indexes for all tables to improve query performance
-- ========================================

-- ReportDetail table indexes
CREATE INDEX IF NOT EXISTS idx_report_id ON report_details(report_id);
CREATE INDEX IF NOT EXISTS idx_student_id ON report_details(student_id);
CREATE INDEX IF NOT EXISTS idx_project_id ON report_details(project_id);
CREATE INDEX IF NOT EXISTS idx_status ON report_details(status);
CREATE INDEX IF NOT EXISTS idx_report_student ON report_details(report_id, student_id);

-- PeriodicReport table indexes
CREATE INDEX IF NOT EXISTS idx_course_id ON periodic_reports(course_id);
CREATE INDEX IF NOT EXISTS idx_report_dates ON periodic_reports(reportFromDate, reportToDate);
CREATE INDEX IF NOT EXISTS idx_submit_dates ON periodic_reports(submitStartAt, submitEndAt);

-- Course table indexes
CREATE UNIQUE INDEX IF NOT EXISTS idx_course_code ON courses(courseCode);
CREATE INDEX IF NOT EXISTS idx_semester_id ON courses(semester_id);
CREATE INDEX IF NOT EXISTS idx_lecturer_id ON courses(lecturer_id);

-- Student table indexes
CREATE UNIQUE INDEX IF NOT EXISTS idx_student_code ON students(studentCode);
CREATE UNIQUE INDEX IF NOT EXISTS idx_user_id ON students(user_id);
CREATE INDEX IF NOT EXISTS idx_github_username ON students(githubUsername);

-- Project table indexes
CREATE UNIQUE INDEX IF NOT EXISTS idx_project_code ON projects(projectCode);
CREATE INDEX IF NOT EXISTS idx_project_course_id ON projects(course_id);

-- User table indexes
CREATE UNIQUE INDEX IF NOT EXISTS idx_username ON users(username);
CREATE UNIQUE INDEX IF NOT EXISTS idx_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_user_status ON users(status);

-- Lecturer table indexes
CREATE UNIQUE INDEX IF NOT EXISTS idx_staff_code ON lecturers(staffCode);
CREATE UNIQUE INDEX IF NOT EXISTS idx_lecturer_user_id ON lecturers(user_id);

-- CourseEnrollment table indexes
CREATE INDEX IF NOT EXISTS idx_enrollment_student_id ON course_enrollments(student_id);
CREATE INDEX IF NOT EXISTS idx_enrollment_course_id ON course_enrollments(course_id);
CREATE INDEX IF NOT EXISTS idx_enrollment_project_id ON course_enrollments(project_id);
CREATE INDEX IF NOT EXISTS idx_student_course ON course_enrollments(student_id, course_id);

-- Semester table indexes
CREATE INDEX IF NOT EXISTS idx_semester_name ON semesters(semesterName);
CREATE INDEX IF NOT EXISTS idx_semester_dates ON semesters(startDate, endDate);

-- RefreshToken table indexes
CREATE UNIQUE INDEX IF NOT EXISTS idx_token ON refresh_tokens(token);
CREATE INDEX IF NOT EXISTS idx_refresh_user_id ON refresh_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_expiry_date ON refresh_tokens(expiryDate);

-- GithubRepository table indexes
CREATE INDEX IF NOT EXISTS idx_github_project_id ON github_repositories(project_id);
CREATE INDEX IF NOT EXISTS idx_repo_url ON github_repositories(repoUrl);
CREATE INDEX IF NOT EXISTS idx_repo_owner ON github_repositories(owner);
CREATE INDEX IF NOT EXISTS idx_is_selected ON github_repositories(isSelected);

-- CommitContribution table indexes
CREATE INDEX IF NOT EXISTS idx_contribution_student_id ON commit_contributions(student_id);
CREATE INDEX IF NOT EXISTS idx_contribution_repo_id ON commit_contributions(repo_id);
CREATE INDEX IF NOT EXISTS idx_commit_date ON commit_contributions(commitDate);
CREATE INDEX IF NOT EXISTS idx_student_repo ON commit_contributions(student_id, repo_id);

-- CommitDetail table indexes
CREATE INDEX IF NOT EXISTS idx_contribution_id ON commit_details(contribution_id);
CREATE INDEX IF NOT EXISTS idx_commit_hash ON commit_details(commitHash);
CREATE INDEX IF NOT EXISTS idx_committed_at ON commit_details(committedAt);

-- SrsDocument table indexes
CREATE INDEX IF NOT EXISTS idx_jira_project_id ON srs_documents(jira_project_id);
CREATE INDEX IF NOT EXISTS idx_srs_version ON srs_documents(version);

-- JiraProject table indexes
CREATE UNIQUE INDEX IF NOT EXISTS idx_jira_key ON jira_projects(jiraKey);
CREATE INDEX IF NOT EXISTS idx_jira_project_id ON jira_projects(project_id);
