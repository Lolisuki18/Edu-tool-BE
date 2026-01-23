-- =========================
-- CONVERT ENUM FIELDS FROM INTEGER TO STRING
-- =========================

-- Step 1: Add temporary columns with VARCHAR type
ALTER TABLE users ADD COLUMN role_temp VARCHAR(50);
ALTER TABLE users ADD COLUMN status_temp VARCHAR(50);

-- Step 2: Convert existing integer values to string enum values
-- Role: 0 = ADMIN, 1 = LECTURER, 2 = STUDENT
UPDATE users SET role_temp = CASE 
    WHEN role = 0 THEN 'ADMIN'
    WHEN role = 1 THEN 'LECTURER'
    WHEN role = 2 THEN 'STUDENT'
    ELSE 'STUDENT'
END;

-- Status: 0 = VERIFICATION_PENDING, 1 = ACTIVE, 2 = INACTIVE
UPDATE users SET status_temp = CASE 
    WHEN status = 0 THEN 'ACTIVE'
    WHEN status = 1 THEN 'VERIFICATION_PENDING'
    WHEN status = 2 THEN 'INACTIVE'
    ELSE 'VERIFICATION_PENDING'
END;

-- Step 3: Drop old INTEGER columns
ALTER TABLE users DROP COLUMN role;
ALTER TABLE users DROP COLUMN status;

-- Step 4: Rename temporary columns to original names
ALTER TABLE users RENAME COLUMN role_temp TO role;
ALTER TABLE users RENAME COLUMN status_temp TO status;

-- Step 5: Add NOT NULL constraints
ALTER TABLE users ALTER COLUMN role SET NOT NULL;
ALTER TABLE users ALTER COLUMN status SET NOT NULL;
