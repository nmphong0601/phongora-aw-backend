-- File script này sẽ được tự động thực thi BỞI USER POSTGRES khi container chạy lần ĐẦU TIÊN.
-- Tạo các database tách biệt cho từng Microservice để đảm bảo tính độc lập.

CREATE DATABASE aw_auth;
CREATE DATABASE aw_workflow;
CREATE DATABASE aw_proposal;
CREATE DATABASE aw_document;
CREATE DATABASE aw_data;
CREATE DATABASE aw_finance;

-- Cấp toàn quyền cho user 'awuser' trên các database vừa tạo
GRANT ALL PRIVILEGES ON DATABASE aw_auth TO awuser;
GRANT ALL PRIVILEGES ON DATABASE aw_workflow TO awuser;
GRANT ALL PRIVILEGES ON DATABASE aw_proposal TO awuser;
GRANT ALL PRIVILEGES ON DATABASE aw_document TO awuser;
GRANT ALL PRIVILEGES ON DATABASE aw_data TO awuser;
GRANT ALL PRIVILEGES ON DATABASE aw_finance TO awuser;

-- 3. Chuyển sang context database 'groupware_auth' để tạo bảng
\c aw_auth;

-- Kích hoạt extension tự động sinh UUID
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. Bảng Roles (Các quyền hạn)
CREATE TABLE roles (
                       id SERIAL PRIMARY KEY,
                       name VARCHAR(50) UNIQUE NOT NULL,
                       description VARCHAR(255)
);

-- 2. Bảng Groups (Nhóm - Dùng làm Role Templates)
CREATE TABLE groups (
                        id SERIAL PRIMARY KEY,
                        name VARCHAR(100) UNIQUE NOT NULL,
                        description VARCHAR(255)
);

-- 3. Bảng Users (Người dùng)
CREATE TABLE users (
                       id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                       username VARCHAR(50) UNIQUE NOT NULL,
                       password VARCHAR(255) NOT NULL, -- Mật khẩu băm (BCrypt)
                       email VARCHAR(100) UNIQUE NOT NULL,
                       is_active BOOLEAN DEFAULT TRUE,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. Bảng Group_Roles (Trung gian N-N: Group chứa Roles)
CREATE TABLE group_roles (
                             group_id INT NOT NULL,
                             role_id INT NOT NULL,
                             PRIMARY KEY (group_id, role_id),
                             CONSTRAINT fk_group FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE,
                             CONSTRAINT fk_role_group FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- 5. Bảng User_Roles (Trung gian N-N: User có Roles)
CREATE TABLE user_roles (
                            user_id UUID NOT NULL,
                            role_id INT NOT NULL,
                            PRIMARY KEY (user_id, role_id),
                            CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                            CONSTRAINT fk_role_user FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

CREATE TABLE user_role_menus (
                                 role_name VARCHAR(50) NOT NULL,
                                 menu_code VARCHAR(100) NOT NULL, -- Ví dụ: 'MENU_INVOICE', 'FUNC_PAYMENT'
                                 can_read BOOLEAN DEFAULT FALSE,
                                 can_write BOOLEAN DEFAULT FALSE,
                                 can_update BOOLEAN DEFAULT FALSE,
                                 can_delete BOOLEAN DEFAULT FALSE,
                                 can_approve BOOLEAN DEFAULT FALSE,
                                 can_return BOOLEAN DEFAULT FALSE,
                                 can_reject BOOLEAN DEFAULT FALSE,
                                 PRIMARY KEY (role_name, menu_code)
);

-- --- INSERT DỮ LIỆU MẪU ---
-- Tạo 3 Roles: admin, staff, manager
INSERT INTO roles (id, name, description) VALUES
                                              (1, 'ROLE_ADMIN', 'Quản trị viên hệ thống'),
                                              (2, 'ROLE_STAFF', 'Nhân viên thông thường'),
                                              (3, 'ROLE_LEADER', 'Trưởng nhóm'),
                                              (4, 'ROLE_SUPERVISOR', 'Giám sát'),
                                              (5, 'ROLE_DEPARTMENT_MANAGER', 'Trưởng phòng'),
                                              (6, 'ROLE_DIVISION_MANAGER', 'Trưởng khối'),
                                              (6, 'ROLE_GD', 'Giám đốc');

-- Tạo 2 Groups
INSERT INTO groups (id, name, description) VALUES
                                               (1, 'GROUP_SYSTEM', 'Nhóm vận hành hệ thống'),
                                               (2, 'GROUP_FINANCE', 'Nhóm tài chính'),
                                               (3, 'GROUP_PURCHASING', 'Nhóm mua hàng'),
                                               (4, 'GROUP_HRM', 'Nhóm quản lý nhân sự');

-- Gán quyền cho nhóm (Group -> Role)
-- Nhóm System chứa quyền admin và manager
INSERT INTO group_roles (group_id, role_id) VALUES (1, 1), (1, 3);
-- Nhóm General chứa quyền staff
INSERT INTO group_roles (group_id, role_id) VALUES (2, 2);

-- Tạo tài khoản mẫu (Mật khẩu '123456' băm bằng BCrypt)
INSERT INTO users (id, username, password, email) VALUES
                                                                 ('11111111-1111-1111-1111-111111111111', 'sys_admin', '$2a$10$wEkiK/Q.4qX4nE8.hG5g/.mYhL3NItN4N2E.h/wE6t/MvU/U/m6/K', 'sysadmin@groupware.com'),
                                                                 ('22222222-2222-2222-2222-222222222222', 'general_user', '$2a$10$wEkiK/Q.4qX4nE8.hG5g/.mYhL3NItN4N2E.h/wE6t/MvU/U/m6/K', 'user@groupware.com');

-- Gán trực tiếp Quyền cho User (User -> Role) thay vì gán Group
-- sys_admin được cấp quyền admin, manager
INSERT INTO user_roles (user_id, role_id) VALUES
                                              ('11111111-1111-1111-1111-111111111111', 1),
                                              ('11111111-1111-1111-1111-111111111111', 3);
-- general_user được cấp quyền staff
INSERT INTO user_roles (user_id, role_id) VALUES ('22222222-2222-2222-2222-222222222222', 2);

-- Chuyển sang database workflow
\c aw_workflow;

CREATE TABLE workflow_instances (
                                    id SERIAL PRIMARY KEY,
                                    proposal_id VARCHAR(50) UNIQUE NOT NULL,
                                    status VARCHAR(30) NOT NULL,          -- PENDING, APPROVED, REJECTED
                                    current_step VARCHAR(50) NOT NULL,    -- MANAGER_REVIEW, ADMIN_REVIEW, DONE
                                    assignee VARCHAR(50),                 -- Người chịu trách nhiệm bước hiện tại
                                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);