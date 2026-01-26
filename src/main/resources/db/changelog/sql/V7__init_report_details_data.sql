-- =========================
-- REPORT DETAILS - Sample Data
-- Note: This migration adds after V5 creates report_details table
-- and V5 updates periodic_reports structure
-- =========================

-- Update existing periodic_reports with new structure (V5 already changed schema)
UPDATE periodic_reports SET
    report_from_date = '2026-01-01',
    report_to_date = '2026-01-07',
    submit_start_at = '2026-01-06',
    submit_end_at = '2026-01-08',
    description = 'Week 1: Project planning & requirement analysis'
WHERE report_id = 1;

UPDATE periodic_reports SET
    report_from_date = '2026-01-08',
    report_to_date = '2026-01-14',
    submit_start_at = '2026-01-13',
    submit_end_at = '2026-01-15',
    description = 'Week 2: Database design & architecture'
WHERE report_id = 2;

-- Update existing projects with description and technologies (V5 already added columns)
UPDATE projects SET
    description = 'A comprehensive education management tool for tracking student projects, commits, and reports',
    technologies = 'Java Spring Boot, PostgreSQL, React, GitHub API'
WHERE project_id = 1;

-- Update course_enrollments with group_number (V5 already added column)
UPDATE course_enrollments SET group_number = 1 WHERE enrollment_id IN (1, 2);

-- Insert sample report details
INSERT INTO report_details
(report_detail_id, report_id, project_id, student_id, title, content, attachment_url, score, feedback, submitted_at, last_updated_at, status)
VALUES
    (1, 1, 1, 1, 'Week 1 Progress Report', 
     'Our team has completed the initial project planning phase. We defined the project scope, identified key stakeholders, and established the development timeline. Requirements analysis was conducted through stakeholder meetings.', 
     'https://drive.google.com/file/d/week1-report', 
     8.5, 
     'Good start! The planning is well-structured, but need more details on technical requirements.', 
     '2026-01-07 23:30:00', 
     '2026-01-07 23:30:00', 
     'SUBMITTED'),
     
    (2, 2, 1, 2, 'Week 2 Progress Report', 
     'Database schema design completed. We created ER diagrams and normalized the database structure. Architecture documentation includes system components, data flow, and API design.', 
     'https://drive.google.com/file/d/week2-report', 
     9.0, 
     'Excellent work! The database design is comprehensive and well-documented.', 
     '2026-01-14 22:15:00', 
     '2026-01-14 22:15:00', 
     'SUBMITTED');
