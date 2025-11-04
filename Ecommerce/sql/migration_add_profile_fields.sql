-- Migration script to add profile enhancement fields to users table
-- Date: 2024
-- Description: Add fullName, avatarUrl, createdAt, updatedAt columns to support enhanced profile management

-- Add fullName column
ALTER TABLE users
ADD COLUMN full_name VARCHAR(100) NULL COMMENT 'Full name of the user';

-- Add avatarUrl column  
ALTER TABLE users
ADD COLUMN avatar_url VARCHAR(500) NULL COMMENT 'URL to user avatar image';

-- Add createdAt column with default value for existing records
ALTER TABLE users
ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT 'Timestamp when user was created';

-- Add updatedAt column with auto-update on row modification
ALTER TABLE users
ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL COMMENT 'Timestamp when user was last updated';

-- Optional: Update existing records to set created_at to a reasonable default if needed
-- UPDATE users SET created_at = NOW() WHERE created_at IS NULL;

-- Verify the changes
-- SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_DEFAULT, COLUMN_COMMENT 
-- FROM INFORMATION_SCHEMA.COLUMNS 
-- WHERE TABLE_NAME = 'users' 
-- AND COLUMN_NAME IN ('full_name', 'avatar_url', 'created_at', 'updated_at');
