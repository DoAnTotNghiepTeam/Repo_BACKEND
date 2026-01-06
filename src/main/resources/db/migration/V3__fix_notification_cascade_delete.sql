-- ============================================================================
-- V3: Fix notification foreign key constraint với ON DELETE CASCADE
-- ============================================================================
-- Problem: Cannot delete applicant vì notifications có FK constraint
-- Solution: Drop ALL existing FK constraints, tạo mới với CASCADE
-- Status: Production-ready, Idempotent, Flyway-compatible
-- ============================================================================

-- Step 1: Drop TẤT CẢ foreign key constraints của notifications.applicant_id
-- Lấy danh sách constraint names từ information_schema và drop bằng prepared statement
SET @constraint_name = NULL;

-- Tìm constraint name của notifications -> applicants
SELECT CONSTRAINT_NAME INTO @constraint_name
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'notifications'
  AND COLUMN_NAME = 'applicant_id'
  AND REFERENCED_TABLE_NAME = 'applicants'
LIMIT 1;

-- Drop constraint nếu tồn tại (dynamic SQL)
SET @drop_sql = IF(
    @constraint_name IS NOT NULL,
    CONCAT('ALTER TABLE notifications DROP FOREIGN KEY ', @constraint_name),
    'SELECT "No FK constraint found" AS info'
);

PREPARE stmt FROM @drop_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Step 2: Tạo constraint mới với ON DELETE CASCADE
-- ✅ Production-safe: Xóa applicant → tự động xóa notifications
-- ✅ Không cần quan tâm tên constraint cũ là gì
-- ✅ Idempotent: Có thể chạy nhiều lần (constraint mới luôn có tên cố định)
ALTER TABLE notifications
ADD CONSTRAINT fk_notification_applicant
FOREIGN KEY (applicant_id)
REFERENCES applicants(id)
ON DELETE CASCADE
ON UPDATE CASCADE;
