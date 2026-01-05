-- create table roles (id, name)
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY, -- nếu có bảng trong entity rồi thì kdl ở đây phải trùng theo để nó ko tạo mới còn chưa có thì nó tạo mới
    name VARCHAR(255) NOT NULL UNIQUE
);
-- create table users (id, username, password)
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY, -- nếu có bảng trong entity rồi thì kdl ở đây phải trùng theo để nó ko tạo mới còn chưa có thì nó tạo mới
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);
-- create table users_roles (user_id, role_id)
CREATE TABLE IF NOT EXISTS users_roles (
    user_id BIGINT NOT NULL, -- nếu có bảng trong entity rồi thì kdl ở đây phải trùng theo để nó ko tạo mới còn chưa có thì nó tạo mới
    role_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- I only insert if the role name does not exists, i use mysql database
INSERT INTO
    roles (name)
SELECT 'Administrators'
WHERE
    NOT EXISTS (
        SELECT 1
        FROM roles
        WHERE
            name = 'Administrators'
    );

INSERT INTO
    roles (name)
SELECT 'Employers'
WHERE
    NOT EXISTS (
        SELECT 1
        FROM roles
        WHERE
            name = 'Employers'
    );

INSERT INTO
    roles (name)
SELECT 'Users'
WHERE
    NOT EXISTS (
        SELECT 1
        FROM roles
        WHERE
            name = 'Users'
    );

INSERT INTO
    users (username, password)
SELECT 'hoangle191205@gmail.com', '123456789' -- chưa có thằng hoangle191205@gamil.com vs password : 123456789 thì nó tạo cho mik
WHERE
    NOT EXISTS (
        SELECT 1
        FROM users
        WHERE
            username = 'hoangle191205@gmail.com'
    );

INSERT INTO
    users_roles (user_id, role_id)
SELECT u.id, r.id
FROM (
        SELECT id
        FROM users
        WHERE
            username = 'hoangle191205@gmail.com'
        LIMIT 1
    ) u, (
        SELECT id
        FROM roles
        WHERE
            name = 'Administrators' -- và nó add thg hoàng vs quyền admin
        LIMIT 1
    ) r
WHERE
    NOT EXISTS (
        SELECT 1
        FROM users_roles
        WHERE
            user_id = u.id
            AND role_id = r.id
    );

# -- Migration: Update CV_REVIEW to CV_PASSED
# UPDATE applicants
# SET application_status = 'CV_PASSED'
# WHERE application_status = 'CV_REVIEW';

-- Bước 1: Thêm CV_PASSED vào enum (giữ cả CV_REVIEW)
ALTER TABLE applicant_history 
MODIFY COLUMN status 
ENUM('PENDING','CV_REVIEW','CV_PASSED','INTERVIEW','OFFER','HIRED','REJECTED') NOT NULL;

-- Bước 2: Update dữ liệu cũ
UPDATE applicant_history 
SET status = 'CV_PASSED' 
WHERE status = 'CV_REVIEW';

-- Bước 3: Xóa CV_REVIEW khỏi enum
ALTER TABLE applicant_history 
MODIFY COLUMN status 
ENUM('PENDING','CV_PASSED','INTERVIEW','OFFER','HIRED','REJECTED') NOT NULL;