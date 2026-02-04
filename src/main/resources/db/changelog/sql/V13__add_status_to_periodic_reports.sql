-- Add status column to periodic_reports table
-- This migration adds status field for managing periodic report state (ACTIVE/INACTIVE)
ALTER TABLE periodic_reports ADD COLUMN status VARCHAR(20) DEFAULT 'ACTIVE' NOT NULL;

-- Create index on status for better query performance
CREATE INDEX IF NOT EXISTS idx_periodic_reports_status ON periodic_reports(status);

-- Update existing records to have ACTIVE status
UPDATE periodic_reports SET status = 'ACTIVE' WHERE status IS NULL;

-- Add comment to column
COMMENT ON COLUMN periodic_reports.status IS 'Status of periodic report: ACTIVE or INACTIVE';
