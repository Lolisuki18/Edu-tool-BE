-- Add email_verification_token column to users table
ALTER TABLE users ADD COLUMN email_verification_token VARCHAR(500);
