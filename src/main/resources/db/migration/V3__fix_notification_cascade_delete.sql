-- Fix foreign key constraint cho notifications
-- Giải pháp production-ready: ON DELETE CASCADE với error handling
-- Khi xóa applicant → tự động xóa notifications liên quan

-- Stored procedure để drop foreign key an toàn
DROP PROCEDURE IF EXISTS drop_fk_if_exists;

DELIMITER $$
CREATE PROCEDURE drop_fk_if_exists(
    IN tableName VARCHAR(64),
    IN constraintName VARCHAR(64)
)
BEGIN
    DECLARE constraintExists INT;
    
    -- Kiểm tra constraint có tồn tại không
    SELECT COUNT(*) INTO constraintExists
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_NAME = constraintName
    AND TABLE_NAME = tableName
    AND TABLE_SCHEMA = DATABASE();
    
    -- Nếu tồn tại thì drop
    IF constraintExists > 0 THEN
        SET @sql = CONCAT('ALTER TABLE ', tableName, ' DROP FOREIGN KEY ', constraintName);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$
DELIMITER ;

-- Step 1: Drop constraint do JPA tự tạo (nếu có)
CALL drop_fk_if_exists('notifications', 'FKfcnuuca9wcrjlc02qo3935xr3');

-- Step 2: Drop constraint từ V2 (nếu có)
CALL drop_fk_if_exists('notifications', 'fk_notification_applicant');

-- Cleanup procedure
DROP PROCEDURE IF EXISTS drop_fk_if_exists;

-- Step 3: Tạo constraint mới với ON DELETE CASCADE
-- ✅ Production-ready với error handling
-- ✅ Idempotent - có thể chạy nhiều lần
-- ✅ Xóa applicant → tự động xóa notifications
ALTER TABLE notifications
ADD CONSTRAINT fk_notification_applicant 
FOREIGN KEY (applicant_id) 
REFERENCES applicants(id) 
ON DELETE CASCADE 
ON UPDATE CASCADE;
