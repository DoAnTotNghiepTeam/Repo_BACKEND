-- Add job_id column to notifications table
ALTER TABLE notifications ADD COLUMN job_id BIGINT;

-- Add index for better query performance
CREATE INDEX idx_notification_job_id ON notifications(job_id);

-- Optionally add foreign key constraint to job_postings table (if needed)
-- ALTER TABLE notifications ADD CONSTRAINT fk_notification_job_id FOREIGN KEY (job_id) REFERENCES job_postings(id);
