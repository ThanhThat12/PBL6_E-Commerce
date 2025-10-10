-- Sport Commerce Database Schema
-- PostgreSQL Database Setup Script

-- Create database (run this as postgres superuser)
-- CREATE DATABASE sportcommerce;
-- \c sportcommerce;

-- Create ENUM types (if you prefer using PostgreSQL enums instead of strings)
-- Note: Spring Boot will handle these as strings by default

-- Users table will be auto-created by Hibernate
-- OTP Verifications table will be auto-created by Hibernate

-- Optional: Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_otp_email_type ON otp_verifications(email, otp_type);
CREATE INDEX IF NOT EXISTS idx_otp_expires_at ON otp_verifications(expires_at);

-- Optional: Create a function to clean up expired OTPs
CREATE OR REPLACE FUNCTION cleanup_expired_otps()
RETURNS void AS $$
BEGIN
    DELETE FROM otp_verifications 
    WHERE expires_at < NOW() AND verified = false;
END;
$$ LANGUAGE plpgsql;

-- Optional: Create a scheduled job to run cleanup (requires pg_cron extension)
-- SELECT cron.schedule('cleanup-expired-otps', '0 * * * *', 'SELECT cleanup_expired_otps();');

-- View to check OTP statistics
CREATE OR REPLACE VIEW otp_statistics AS
SELECT 
    email,
    otp_type,
    COUNT(*) as total_requests,
    SUM(CASE WHEN verified THEN 1 ELSE 0 END) as verified_count,
    MAX(created_at) as last_request_at
FROM otp_verifications
GROUP BY email, otp_type;

-- View to check user registration statistics
CREATE OR REPLACE VIEW user_statistics AS
SELECT 
    role,
    provider,
    email_verification_status,
    status,
    COUNT(*) as user_count,
    DATE(created_at) as registration_date
FROM users
GROUP BY role, provider, email_verification_status, status, DATE(created_at)
ORDER BY registration_date DESC;

-- Grant permissions (adjust username as needed)
-- GRANT ALL PRIVILEGES ON DATABASE sportcommerce TO your_username;
-- GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO your_username;
-- GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO your_username;
