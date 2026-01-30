-- Add status column to courses table
ALTER TABLE courses ADD COLUMN status BOOLEAN DEFAULT TRUE;
