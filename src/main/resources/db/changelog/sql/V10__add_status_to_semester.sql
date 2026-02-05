-- Add status column to semester table
ALTER TABLE semesters ADD COLUMN status BOOLEAN DEFAULT TRUE;
