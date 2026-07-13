-- File script này sẽ được tự động thực thi BỞI USER POSTGRES khi container chạy lần ĐẦU TIÊN.
-- Tạo các database tách biệt cho từng Microservice để đảm bảo tính độc lập.

CREATE DATABASE aw_master_data;
CREATE DATABASE aw_auth;
CREATE DATABASE aw_hr;
CREATE DATABASE aw_workflow;
CREATE DATABASE aw_proposal;
CREATE DATABASE aw_document;
CREATE DATABASE aw_finance;

-- Cấp toàn quyền cho user 'awuser' trên các database vừa tạo
GRANT ALL PRIVILEGES ON DATABASE aw_master_data TO awuser;
GRANT ALL PRIVILEGES ON DATABASE aw_auth TO awuser;
GRANT ALL PRIVILEGES ON DATABASE aw_hr TO awuser;
GRANT ALL PRIVILEGES ON DATABASE aw_workflow TO awuser;
GRANT ALL PRIVILEGES ON DATABASE aw_proposal TO awuser;
GRANT ALL PRIVILEGES ON DATABASE aw_document TO awuser;
GRANT ALL PRIVILEGES ON DATABASE aw_finance TO awuser;

-- I. Chuyển sang context database 'aw_auth' để tạo bảng
\c aw_auth;

-- Kích hoạt extension tự động sinh UUID
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. Bảng Roles (Các quyền hạn)
CREATE TABLE roles (
                       id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                       name VARCHAR(50) UNIQUE NOT NULL,
                       description VARCHAR(255)
);

-- 2. Bảng Groups (Nhóm - Dùng làm Role Templates)
CREATE TABLE groups (
                        id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                        name VARCHAR(100) UNIQUE NOT NULL,
                        description VARCHAR(255)
);

-- 3. Bảng Users (Người dùng)
CREATE TABLE users (
                       id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                       username VARCHAR(50) UNIQUE NOT NULL,
                       password VARCHAR(255) NOT NULL, -- Mật khẩu băm (BCrypt)
                       email VARCHAR(100) UNIQUE NULL,
                       is_active BOOLEAN DEFAULT TRUE,
                       employee_id UUID UNIQUE, -- Tham chiếu tới hồ sơ nhân sự
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. Bảng Group_Roles (Trung gian N-N: Group chứa Roles)
CREATE TABLE group_roles (
                             group_name VARCHAR(100) NOT NULL,
                             role_name VARCHAR(50) NOT NULL,
                             PRIMARY KEY (group_name, role_name),
                             CONSTRAINT fk_group FOREIGN KEY (group_name) REFERENCES groups(name) ON DELETE CASCADE,
                             CONSTRAINT fk_role_group FOREIGN KEY (role_name) REFERENCES roles(name) ON DELETE CASCADE
);

-- 5. Bảng User_Roles (Trung gian N-N: User có Roles)
CREATE TABLE user_roles (
                            user_name VARCHAR(50) NOT NULL,
                            role_name VARCHAR(50) NOT NULL,
                            PRIMARY KEY (user_name, role_name),
                            CONSTRAINT fk_user FOREIGN KEY (user_name) REFERENCES users(username) ON DELETE CASCADE,
                            CONSTRAINT fk_role_user FOREIGN KEY (role_name) REFERENCES roles(name) ON DELETE CASCADE
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
INSERT INTO roles (name, description) VALUES
                                              ('ROLE_ADMIN', 'Quản trị viên hệ thống'),
                                              ('ROLE_STAFF', 'Nhân viên thông thường'),
                                              ('ROLE_LEADER', 'Trưởng nhóm'),
                                              ('ROLE_SUP', 'Giám sát'),
                                              ('ROLE_DEPT_MANAGER', 'Trưởng phòng'),
                                              ('ROLE_HR_EXEC', 'Chuyên viên Nhân sự'),
                                              ('ROLE_HR_MANAGER', 'Trưởng phòng Nhân sự'),
                                              ('ROLE_ACC_MANAGER', 'Trưởng phòng Kế toán'),
                                              ('ROLE_DIV_DIRECTOR', 'Trưởng khối'),
                                              ('ROLE_BR_DIRECTOR', 'Giám đốc chi nhánh'),
                                              ('ROLE_GD', 'Giám đốc');

-- Tạo 2 Groups
INSERT INTO groups (name, description) VALUES
                                               ('GROUP_SYSTEM', 'Nhóm vận hành hệ thống'),
                                               ('GROUP_GENERAL', 'Nhóm chung'),
                                               ('GROUP_FINANCE', 'Nhóm tài chính'),
                                               ('GROUP_PURCHASING', 'Nhóm mua hàng'),
                                               ('GROUP_HRM', 'Nhóm quản lý nhân sự');

-- Gán quyền cho nhóm (Group -> Role)
-- Nhóm System chứa quyền admin và manager
INSERT INTO group_roles (group_name, role_name) VALUES ('GROUP_SYSTEM', 'ROLE_ADMIN');
-- Nhóm General chứa quyền staff
INSERT INTO group_roles (group_name, role_name) VALUES ('GROUP_GENERAL', 'ROLE_STAFF');

-- Tạo tài khoản mẫu (Mật khẩu '123456' băm bằng BCrypt)
INSERT INTO users (username, password, email) VALUES ('sys_admin', '$2a$10$wEkiK/Q.4qX4nE8.hG5g/.mYhL3NItN4N2E.h/wE6t/MvU/U/m6/K', 'sysadmin@phongora-aw.com');

-- Gán trực tiếp Quyền cho User (User -> Role) thay vì gán Group
-- sys_admin được cấp quyền admin
INSERT INTO user_roles (user_name, role_name) VALUES ('sys_admin', 'ROLE_ADMIN');

-- II. Chuyển sang database master data
\c aw_hr;

CREATE TABLE employee_code_sequences (
                                   company_code VARCHAR(10) PRIMARY KEY, -- Ví dụ: 'AW'
                                   next_sequence INT DEFAULT 1           -- Số thứ tự tiếp theo
);

-- Khởi tạo cho công ty mẫu
INSERT INTO employee_code_sequences (company_code, next_sequence) VALUES ('AW', 1);

-- 1. Bảng Cấp độ đơn vị tổ chức
CREATE TABLE org_unit_levels (
                                   id SERIAL PRIMARY KEY,
                                   level_index INT NOT NULL UNIQUE, -- 1 là cao nhất, 5 là thấp nhất
                                   level_code VARCHAR(20) NOT NULL, -- COMPANY, BRANCH, DIVISION, DEPARTMENT, TEAM
                                   level_name VARCHAR(50) NOT NULL, -- Tên hiển thị (ví dụ: Công ty, Chi nhánh...)
                                   can_have_headcount BOOLEAN DEFAULT TRUE, -- Cấp nào được phép lập định biên?
                                   description TEXT
);

-- 2. Bảng Đơn vị tổ chức (Sử dụng cấu trúc cây bằng parent_id)
CREATE TABLE org_units (
                                       id UUID PRIMARY KEY,
                                       code VARCHAR(50) NOT NULL UNIQUE,
                                       name VARCHAR(100) NOT NULL,

                                       -- Liên kết với định nghĩa cấp độ
                                       level_id INT NOT NULL,

                                       parent_id UUID,                 -- Liên kết tới id của unit cha
                                       manager_id UUID,                -- Trưởng đơn vị

                                       status VARCHAR(20) DEFAULT 'ACTIVE',
                                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                       FOREIGN KEY (level_id) REFERENCES org_unit_levels(id),
                                       FOREIGN KEY (parent_id) REFERENCES org_units(id)
);

-- Index để tăng tốc truy vấn phân cấp
CREATE INDEX idx_org_parent ON org_units(parent_id);

-- 3. Bảng Danh mục Cấp bậc (Phân dải lương sau này cho khối C&B)
CREATE TABLE job_levels (
                               id UUID PRIMARY KEY,
                               code VARCHAR(50) NOT NULL UNIQUE,       -- Ví dụ: 'L1', 'L2', 'M1'
                               name VARCHAR(100) NOT NULL,             -- Tên cấp bậc (Junior, Senior, Manager)
                               rank_value INT NOT NULL,                -- Trọng số để so sánh cao thấp (1, 2, 3...)
                               status VARCHAR(20) DEFAULT 'ACTIVE',
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. Bảng Danh mục Chức danh
CREATE TABLE job_titles (
                               id UUID PRIMARY KEY,
                               code VARCHAR(50) NOT NULL UNIQUE,       -- Ví dụ: 'DEV', 'BA', 'HR_EXEC'
                               name VARCHAR(100) NOT NULL,             -- Tên chức danh
                               job_level_id UUID,                      -- Liên kết với cấp bậc
                               status VARCHAR(20) DEFAULT 'ACTIVE',
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               FOREIGN KEY (job_level_id) REFERENCES job_levels(id)
);

-- 3. Bảng Employee (Nhân sự)
CREATE TABLE employees (
                              id UUID PRIMARY KEY,
                              employee_code VARCHAR(20) UNIQUE NOT NULL, -- AW00001
                              full_name VARCHAR(100) NOT NULL,
                              org_unit_code VARCHAR(50) NOT NULL,
                              title_code VARCHAR(50),
                              employment_status VARCHAR(20) DEFAULT 'ACTIVE',
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. Bảng Kế hoạch headcount nhân sự (Theo năm/quý)
CREATE TABLE headcount_plans (
                                 id UUID PRIMARY KEY,
                                 department_id UUID NOT NULL,   -- Từ Master Data
                                 title_id UUID NOT NULL,        -- Từ Master Data (Chức danh: Developer, BA,...)
                                 plan_year INT NOT NULL,        -- Ví dụ: 2026
                                 target_count INT NOT NULL DEFAULT 0, -- Số lượng kỳ vọng tuyển được
                                 current_count INT NOT NULL DEFAULT 0, -- Số lượng nhân sự hiện tại
                                 status VARCHAR(30) DEFAULT 'DRAFT',   -- DRAFT, PENDING, APPROVED, REJECTED, RETURNED
                                 workflow_instance_id UUID,            -- Tham chiếu ID tiến trình bên module WORKFLOW
                                 created_by UUID NOT NULL,             -- Người lập headcount
                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 CONSTRAINT uq_dept_title_year UNIQUE (department_id, title_id, plan_year)
);

-- -- 5. Bảng Yêu cầu tuyển dụng (Recruitment Request)
-- CREATE TABLE recruitment_requests (
--                                          id UUID PRIMARY KEY,
--                                          headcount_plan_id UUID,        -- Tham chiếu đến định biên (Có thể NULL nếu tuyển ngoài kế hoạch)
--                                          department_id UUID NOT NULL,
--                                          title_id UUID NOT NULL,
--                                          quantity INT NOT NULL DEFAULT 1,
--                                          salary_range_min NUMERIC(15,2),
--                                          salary_range_max NUMERIC(15,2),
--                                          requirement_description TEXT,
--                                          created_by UUID NOT NULL,      -- Người tạo request (User ID từ Auth)
--                                          workflow_instance_id UUID,     -- ID luồng phê duyệt từ module WORKFLOW
--                                          status VARCHAR(30) DEFAULT 'DRAFT', -- DRAFT, PENDING, APPROVED, REJECTED, RETURNED
--                                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--                                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--                                          FOREIGN KEY (headcount_plan_id) REFERENCES hr_headcount_plans(id)
-- );
--
-- -- 6. Bảng Hồ sơ Tiếp nhận nhân sự mới (Onboarding Profile)
-- CREATE TABLE onboarding_profiles (
--                                         id UUID PRIMARY KEY,
--                                         recruitment_request_id UUID,   -- Thuộc yêu cầu tuyển dụng nào
--                                         candidate_name VARCHAR(100) NOT NULL,
--                                         email VARCHAR(100) NOT NULL,
--                                         phone VARCHAR(20),
--                                         department_id UUID NOT NULL,
--                                         title_id UUID NOT NULL,
--                                         expected_start_date DATE,
--                                         status VARCHAR(30) DEFAULT 'PREPARING', -- PREPARING, IN_PROGRESS, COMPLETED, CANCELED
--                                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--                                         FOREIGN KEY (recruitment_request_id) REFERENCES recruitment_requests(id)
-- );
--
-- -- 4. Bảng Checklist công việc Onboarding (Tạo tự động theo mẫu khi Profile được kích hoạt)
-- CREATE TABLE onboarding_tasks (
--                                      id UUID PRIMARY KEY,
--                                      onboarding_profile_id UUID NOT NULL,
--                                      task_name VARCHAR(255) NOT NULL,       -- Ví dụ: "Cấp máy tính", "Tạo tài khoản Email"
--                                      assigned_role VARCHAR(50) NOT NULL,    -- Gán cho VAI TRÒ nào xử lý (Ví dụ: ROLE_IT, ROLE_ADMIN)
--                                      is_completed BOOLEAN DEFAULT FALSE,
--                                      completed_at TIMESTAMP,
--                                      FOREIGN KEY (onboarding_profile_id) REFERENCES onboarding_profiles(id) ON DELETE CASCADE
-- );

-- Chèn dữ liệu mẫu cho cấu trúc 5 cấp
INSERT INTO org_unit_levels (level_index, level_code, level_name, can_have_headcount)
VALUES
    (1, 'COMPANY', 'Tổng công ty', FALSE),
    (2, 'BRANCH', 'Chi nhánh', FALSE),
    (3, 'DIVISION', 'Khối nghiệp vụ', TRUE),
    (4, 'DEPARTMENT', 'Phòng ban', TRUE),
    (5, 'TEAM', 'Đội nhóm', TRUE);

-- Giả định các ID cho level_id từ 1 đến 5:
-- 1: COMPANY, 2: BRANCH, 3: DIVISION, 4: DEPARTMENT, 5: TEAM

DO $$
DECLARE
    v_company_id UUID := gen_random_uuid();
    v_branch_hcm_id UUID := gen_random_uuid();
    v_div_bo_id UUID := gen_random_uuid();
    v_dept_hr_id UUID := gen_random_uuid();
    v_div_tech_id UUID := gen_random_uuid();
    v_dept_dev_id UUID := gen_random_uuid();
BEGIN
    -- 1. Cấp 1: Company (Root)
    INSERT INTO org_units (id, code, name, level_id, parent_id)
    VALUES (v_company_id, 'AW_CORP', 'Awesome Corporation', 1, NULL);

    -- 2. Cấp 2: Branch
    INSERT INTO org_units (id, code, name, level_id, parent_id)
    VALUES (v_branch_hcm_id, 'BR_HCM', 'Chi nhánh Hồ Chí Minh', 2, v_company_id);

    -- 3. Cấp 3: Division
    INSERT INTO org_units (id, code, name, level_id, parent_id)
    VALUES (v_div_bo_id, 'DIV_BO', 'Khối Vận hành nội bộ', 3, v_branch_hcm_id);

    INSERT INTO org_units (id, code, name, level_id, parent_id)
    VALUES (v_div_tech_id, 'DIV_TECH', 'Khối Công nghệ', 3, v_branch_hcm_id);

    -- 4. Cấp 4: Department
    INSERT INTO org_units (id, code, name, level_id, parent_id)
    VALUES (v_dept_hr_id, 'DEPT_HR', 'Phòng Nhân sự', 4, v_div_bo_id);

    INSERT INTO org_units (id, code, name, level_id, parent_id)
    VALUES (gen_random_uuid(), 'DEPT_ACC', 'Phòng Kế toán', 4, v_div_bo_id);

    INSERT INTO org_units (id, code, name, level_id, parent_id)
    VALUES (gen_random_uuid(), 'DEPT_ADMIN', 'Phòng Hành chính', 4, v_div_bo_id);

    INSERT INTO org_units (id, code, name, level_id, parent_id)
    VALUES (v_dept_dev_id, 'DEPT_DEV', 'Phòng Phát triển Phần mềm', 4, v_div_tech_id);

    -- 5. Cấp 5: Team
    INSERT INTO org_units (id, code, name, level_id, parent_id)
    VALUES
        (gen_random_uuid(), 'TEAM_BE', 'Team Backend', 5, v_dept_dev_id),
        (gen_random_uuid(), 'TEAM_FE', 'Team Frontend', 5, v_dept_dev_id);

    -- Thêm một nhánh khác để thấy rõ sự phân cấp
    INSERT INTO org_units (id, code, name, level_id, parent_id)
    VALUES (gen_random_uuid(), 'DEPT_QA', 'Phòng Kiểm thử (QA)', 4, v_div_tech_id);
END $$;

INSERT INTO job_levels (id, code, name, rank_value)
VALUES
    (gen_random_uuid(), 'INT', 'Intern / Thực tập sinh', 1),
    (gen_random_uuid(), 'FRE', 'Fresher / Sinh viên mới tốt nghiệp', 2),
    (gen_random_uuid(), 'JUN', 'Junior / Chuyên viên', 3),
    (gen_random_uuid(), 'MID', 'Middle / Chuyên viên chính', 4),
    (gen_random_uuid(), 'SEN', 'Senior / Chuyên viên cao cấp', 5),
    (gen_random_uuid(), 'MGR', 'Manager / Quản lý', 6),
    (gen_random_uuid(), 'DIR', 'Director / Giám đốc', 7),
    (gen_random_uuid(), 'C_LEVEL', 'C-Level / Ban điều hành', 8);

DO $$
DECLARE
    lvl_jun UUID; lvl_sen UUID; lvl_mgr UUID; lvl_dir UUID; lvl_c_level UUID;
BEGIN
     -- Lấy ID của các Level
    SELECT id INTO lvl_jun FROM job_levels WHERE code = 'JUN';
    SELECT id INTO lvl_sen FROM job_levels WHERE code = 'SEN';
    SELECT id INTO lvl_mgr FROM job_levels WHERE code = 'MGR';
    SELECT id INTO lvl_dir FROM job_levels WHERE code = 'DIR';
    SELECT id INTO lvl_c_level FROM job_levels WHERE code = 'C_LEVEL';

    -- Khởi tạo chức danh Khối IT
    INSERT INTO job_titles (id, code, name, job_level_id) VALUES
                                                                 (gen_random_uuid(), 'BE_DEV_JUN', 'Backend Developer (Junior)', lvl_jun),
                                                                 (gen_random_uuid(), 'BE_DEV_SEN', 'Backend Developer (Senior)', lvl_sen),
                                                                 (gen_random_uuid(), 'FE_DEV_JUN', 'Frontend Developer (Junior)', lvl_jun),
                                                                 (gen_random_uuid(), 'QA_QC', 'QA/QC Engineer', lvl_jun),
                                                                 (gen_random_uuid(), 'IT_MGR', 'IT Manager', lvl_mgr);

    -- Khởi tạo chức danh Khối HR & FIN
    INSERT INTO job_titles (id, code, name, job_level_id) VALUES
                                                                 (gen_random_uuid(), 'HR_EXEC', 'HR Executive', lvl_dir),
                                                                 (gen_random_uuid(), 'HR_MGR', 'HR Manager', lvl_dir),
                                                                 (gen_random_uuid(), 'ACC', 'Accountant', lvl_dir);

    -- Khởi tạo chức danh Giám đốc và Ban điều hành
    INSERT INTO job_titles (id, code, name, job_level_id) VALUES
                                                              (gen_random_uuid(), 'DIV_DIR', 'Division Director', lvl_dir),
                                                              (gen_random_uuid(), 'BR_DIR', 'Branch Director', lvl_dir),
                                                              (gen_random_uuid(), 'CEO', 'General Director', lvl_c_level);

END $$;

-- III. Chuyển sang database workflow
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