-- Fix refresh_tokens sequence
-- Reset the sequence to start after the last inserted ID
SELECT setval(pg_get_serial_sequence('refresh_tokens', 'id'), (SELECT MAX(id) FROM refresh_tokens) + 1);
