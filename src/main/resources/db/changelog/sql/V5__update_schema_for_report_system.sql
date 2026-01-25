-- =========================
-- UPDATE SCHEMA FOR PERIODIC REPORT AND RELATED TABLES
-- =========================

-- =========================
-- Step 1: Add new columns to existing tables
-- =========================

-- Add description and technologies to projects table
ALTER TABLE projects ADD COLUMN description TEXT;
ALTER TABLE projects ADD COLUMN technologies TEXT;

-- Add group_number to course_enrollments table
ALTER TABLE course_enrollments ADD COLUMN group_number INTEGER;

-- Rename semester name column
ALTER TABLE semesters RENAME COLUMN name TO semester_name;

-- Rename jira_projects projectName to jiraName
ALTER TABLE jira_projects RENAME COLUMN project_name TO jira_name;

-- =========================
-- Step 2: Update periodic_reports table structure
-- =========================

-- Add new columns for periodic reports
ALTER TABLE periodic_reports ADD COLUMN report_from_date TIMESTAMP;
ALTER TABLE periodic_reports ADD COLUMN report_to_date TIMESTAMP;
ALTER TABLE periodic_reports ADD COLUMN submit_start_at TIMESTAMP;
ALTER TABLE periodic_reports ADD COLUMN submit_end_at TIMESTAMP;
ALTER TABLE periodic_reports ADD COLUMN description TEXT;

-- Drop old columns from periodic_reports
ALTER TABLE periodic_reports DROP COLUMN IF EXISTS week_number;
ALTER TABLE periodic_reports DROP COLUMN IF EXISTS year;
ALTER TABLE periodic_reports DROP COLUMN IF EXISTS content;

-- =========================
-- Step 3: Create report_details table
-- =========================

CREATE TABLE report_details (
    report_detail_id SERIAL PRIMARY KEY,
    report_id INTEGER NOT NULL,
    project_id INTEGER NOT NULL,
    student_id INTEGER NOT NULL,
    title VARCHAR(255),
    content TEXT,
    attachment_url VARCHAR(500),
    score DECIMAL(5, 2),
    feedback TEXT,
    submitted_at TIMESTAMP,
    last_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50),
    FOREIGN KEY (report_id) REFERENCES periodic_reports(report_id) ON DELETE CASCADE,
    FOREIGN KEY (project_id) REFERENCES projects(project_id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE
);

-- =========================
-- Step 4: Create indexes for better performance
-- =========================

CREATE INDEX idx_report_details_report_id ON report_details(report_id);
CREATE INDEX idx_report_details_project_id ON report_details(project_id);
CREATE INDEX idx_report_details_student_id ON report_details(student_id);
CREATE INDEX idx_report_details_status ON report_details(status);
CREATE INDEX idx_course_enrollments_group_number ON course_enrollments(group_number);

-- =========================
-- Notes:
-- - This migration updates the periodic report system
-- - Adds report_details table for managing student submissions
-- - Enhances projects table with description and technologies fields
-- - Adds group tracking to course enrollments
-- - Renames fields to match database schema conventions
-- =========================
