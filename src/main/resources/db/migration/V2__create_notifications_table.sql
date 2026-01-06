-- Migration để tạo bảng notifications với các cải tiến từ góp ý frontend
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    applicant_id BIGINT,
    notification_type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT,
    status VARCHAR(50),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    deleted_at DATETIME,
    job_title VARCHAR(255),
    company_name VARCHAR(255),
    
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_notification_applicant FOREIGN KEY (applicant_id) REFERENCES applicants(id) ON DELETE CASCADE,
    
    INDEX idx_user_created (user_id, created_at),
    INDEX idx_user_read (user_id, is_read),
    INDEX idx_deleted_at (deleted_at)
);
