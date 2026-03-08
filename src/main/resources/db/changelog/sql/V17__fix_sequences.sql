-- Fix sequences for ALL tables that had explicit ID inserts during initial data load

SELECT setval(pg_get_serial_sequence('users', 'user_id'),               COALESCE((SELECT MAX(user_id)          FROM users),               0) + 1, false);
SELECT setval(pg_get_serial_sequence('lecturers', 'lecturer_id'),        COALESCE((SELECT MAX(lecturer_id)       FROM lecturers),           0) + 1, false);
SELECT setval(pg_get_serial_sequence('students', 'student_id'),          COALESCE((SELECT MAX(student_id)        FROM students),            0) + 1, false);
SELECT setval(pg_get_serial_sequence('semesters', 'semester_id'),        COALESCE((SELECT MAX(semester_id)       FROM semesters),           0) + 1, false);
SELECT setval(pg_get_serial_sequence('courses', 'course_id'),            COALESCE((SELECT MAX(course_id)         FROM courses),             0) + 1, false);
SELECT setval(pg_get_serial_sequence('projects', 'project_id'),          COALESCE((SELECT MAX(project_id)        FROM projects),            0) + 1, false);
SELECT setval(pg_get_serial_sequence('course_enrollments', 'enrollment_id'), COALESCE((SELECT MAX(enrollment_id) FROM course_enrollments),  0) + 1, false);
SELECT setval(pg_get_serial_sequence('periodic_reports', 'report_id'),   COALESCE((SELECT MAX(report_id)         FROM periodic_reports),    0) + 1, false);
SELECT setval(pg_get_serial_sequence('report_details', 'report_detail_id'), COALESCE((SELECT MAX(report_detail_id) FROM report_details),   0) + 1, false);
SELECT setval(pg_get_serial_sequence('github_repositories', 'repo_id'), COALESCE((SELECT MAX(repo_id)            FROM github_repositories), 0) + 1, false);
SELECT setval(pg_get_serial_sequence('jira_projects', 'jira_project_id'), COALESCE((SELECT MAX(jira_project_id)  FROM jira_projects),       0) + 1, false);
SELECT setval(pg_get_serial_sequence('srs_documents', 'srs_id'),         COALESCE((SELECT MAX(srs_id)            FROM srs_documents),       0) + 1, false);
SELECT setval(pg_get_serial_sequence('commit_contributions', 'contribution_id'), COALESCE((SELECT MAX(contribution_id) FROM commit_contributions), 0) + 1, false);
SELECT setval(pg_get_serial_sequence('commit_details', 'commit_id'),     COALESCE((SELECT MAX(commit_id)         FROM commit_details),      0) + 1, false);
SELECT setval(pg_get_serial_sequence('refresh_tokens', 'id'),            COALESCE((SELECT MAX(id)                FROM refresh_tokens),      0) + 1, false);
