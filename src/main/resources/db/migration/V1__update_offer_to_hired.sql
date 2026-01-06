-- Migration: Xóa trạng thái OFFER và chuyển sang HIRED
-- Chạy trước khi alter enum

UPDATE applicants 
SET application_status = 'HIRED' 
WHERE application_status = 'OFFER';

UPDATE applicant_history 
SET status = 'HIRED' 
WHERE status = 'OFFER';
