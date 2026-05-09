-- CPT202 Project Selection System - MySQL 8.0 seed data
-- Default accounts:
--   admin / admin123
--   teacher / teacher123
--   student / student123

USE cpt202_project_selection;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE project_application;
TRUNCATE TABLE project_topic;
TRUNCATE TABLE project_category;
TRUNCATE TABLE sys_role_menu;
TRUNCATE TABLE sys_user_role;
TRUNCATE TABLE sys_menu;
TRUNCATE TABLE sys_role;
TRUNCATE TABLE sys_user;

SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO sys_role (role_id, role_name, role_key, role_sort, data_scope, status, del_flag, remark, create_by)
VALUES
    (1, 'Admin', 'admin', 1, '1', '0', '0', 'Full access', 'system'),
    (2, 'Teacher', 'teacher', 2, '5', '0', '0', 'Supervisor account', 'system'),
    (3, 'Student', 'student', 3, '5', '0', '0', 'Student account', 'system');

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by)
VALUES
    (1, 'Dashboard', 0, 1, '/index', 'dashboard', 'C', '0', '0', NULL, 'dashboard', 'system'),
    (10, 'Users', 0, 10, '/system/users', 'system/user', 'C', '0', '0', 'system:user:list', 'user', 'system'),
    (11, 'Create User', 10, 1, '', NULL, 'F', '1', '0', 'system:user:add', '#', 'system'),
    (12, 'Edit User', 10, 2, '', NULL, 'F', '1', '0', 'system:user:edit', '#', 'system'),
    (13, 'Delete User', 10, 3, '', NULL, 'F', '1', '0', 'system:user:remove', '#', 'system'),
    (20, 'Roles', 0, 20, '/system/roles', 'system/role', 'C', '0', '0', 'system:role:list', 'role', 'system'),
    (21, 'Assign Roles', 20, 1, '', NULL, 'F', '1', '0', 'system:role:edit', '#', 'system'),
    (30, 'Topics', 0, 30, '/project/topics', 'project/topic', 'C', '0', '0', 'project:topic:list', 'list', 'system'),
    (31, 'Topic Detail', 30, 1, '', NULL, 'F', '1', '0', 'project:topic:query', '#', 'system'),
    (32, 'Create Topic', 30, 2, '', NULL, 'F', '1', '0', 'project:topic:add', '#', 'system'),
    (33, 'Edit Topic', 30, 3, '', NULL, 'F', '1', '0', 'project:topic:edit', '#', 'system'),
    (34, 'Publish Topic', 30, 4, '', NULL, 'F', '1', '0', 'project:topic:publish', '#', 'system'),
    (40, 'Categories', 0, 40, '/project/categories', 'project/category', 'C', '0', '0', 'project:category:list', 'tree', 'system'),
    (50, 'Applications', 0, 50, '/project/applications', 'project/application', 'C', '0', '0', 'project:application:list', 'form', 'system'),
    (51, 'Submit Application', 50, 1, '', NULL, 'F', '1', '0', 'project:application:add', '#', 'system'),
    (52, 'Withdraw Application', 50, 2, '', NULL, 'F', '1', '0', 'project:application:withdraw', '#', 'system'),
    (53, 'Application History', 50, 3, '', NULL, 'F', '1', '0', 'project:application:history', '#', 'system'),
    (54, 'Review Application', 50, 4, '', NULL, 'F', '1', '0', 'project:application:review', '#', 'system'),
    (55, 'Batch Review', 50, 5, '', NULL, 'F', '1', '0', 'project:application:batch', '#', 'system'),
    (60, 'Reports', 0, 60, '/project/reports', 'project/report', 'C', '0', '0', 'project:report:list', 'chart', 'system'),
    (70, 'Profile', 0, 70, '/profile', 'system/profile', 'C', '0', '0', 'system:user:profile', 'profile', 'system');

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu;

INSERT INTO sys_role_menu (role_id, menu_id)
VALUES
    (2, 1), (2, 30), (2, 32), (2, 33), (2, 34), (2, 50), (2, 54), (2, 55), (2, 70),
    (3, 1), (3, 30), (3, 31), (3, 50), (3, 51), (3, 52), (3, 53), (3, 70);

INSERT INTO sys_user (user_id, dept_id, user_name, nick_name, email, phonenumber, sex, password, status, del_flag, create_by)
VALUES
    (1, NULL, 'admin', 'System Admin', 'admin@example.com', '13800000001', '2',
     '$2b$12$FYLJiK/DLFhyudvmbQFvDuM2TZ4JIa5mp1VRgZevJ.Ba3fCsEuDMi', '0', '0', 'system'),
    (2, NULL, 'teacher', 'Dr. Taylor', 'teacher@example.com', '13800000002', '2',
     '$2b$12$VcLhJvykO/0zHKT7sqrw1OzswmeePtBN.spVawEkCVJydw9CVRZhe', '0', '0', 'system'),
    (3, NULL, 'student', 'Alex Lee', 'student@example.com', '13800000003', '2',
     '$2b$12$H1g6Oar9x03egAZ/GVfHL.IiWr16UCxQY6/oRkvHOn/pWV6BorAHS', '0', '0', 'system'),
    (4, NULL, 'teacher2', 'Prof. Morgan', 'teacher2@example.com', '13800000004', '2',
     '$2b$12$VcLhJvykO/0zHKT7sqrw1OzswmeePtBN.spVawEkCVJydw9CVRZhe', '0', '0', 'system'),
    (5, NULL, 'student2', 'Casey Chen', 'student2@example.com', '13800000005', '2',
     '$2b$12$H1g6Oar9x03egAZ/GVfHL.IiWr16UCxQY6/oRkvHOn/pWV6BorAHS', '0', '0', 'system');

INSERT INTO sys_user_role (user_id, role_id)
VALUES
    (1, 1),
    (2, 2),
    (3, 3),
    (4, 2),
    (5, 3);

INSERT INTO project_category (category_id, category_name, parent_id, order_num, status, del_flag, create_by)
VALUES
    (1, 'Artificial Intelligence', 0, 1, '0', '0', 'admin'),
    (2, 'Web Development', 0, 2, '0', '0', 'admin'),
    (3, 'Data Analytics', 0, 3, '0', '0', 'admin'),
    (4, 'Mobile Applications', 0, 4, '0', '0', 'admin'),
    (5, 'Software Engineering Tools', 0, 5, '0', '0', 'admin');

INSERT INTO project_topic (topic_id, teacher_id, title, description, skills, keywords, category_id, max_students, status, del_flag, create_by)
VALUES
    (1, 2, 'Course Project Selection System with Spring Boot',
     'Design and implement an online project selection system for students and teachers, including topic publishing, application review, and basic reporting.',
     'Java, Spring Boot, Thymeleaf, MyBatis, MySQL', 'selection system,Spring Boot,RBAC', 2, 2, 'Available', '0', 'teacher'),
    (2, 2, 'Student Learning Behavior Analytics Dashboard',
     'Collect and organize learning data, then build dashboards for activity, submission, and grade trends.',
     'Python, SQL, ECharts, data cleaning', 'analytics,visualization,learning behavior', 3, 1, 'Requested', '0', 'teacher'),
    (3, 4, 'AI-assisted Code Review Prototype',
     'Explore how large language models can support code review and build a prototype that generates improvement suggestions.',
     'Java, REST API, Prompt Engineering', 'AI,code review,LLM', 1, 1, 'Draft', '0', 'teacher2'),
    (4, 4, 'Mobile App for Campus Event Registration',
     'Build a clean mobile workflow for browsing activities, registration, and check-in.',
     'Flutter, REST API, MySQL', 'mobile app,event registration', 4, 3, 'Closed', '0', 'teacher2');

INSERT INTO project_application (application_id, topic_id, student_id, personal_note, status, reject_reason, reject_by, reject_time, del_flag, create_by)
VALUES
    (1, 2, 3, 'I am interested in data analytics and have practiced basic ECharts visualizations.', 'Pending', NULL, NULL, NULL, '0', 'student'),
    (2, 1, 5, 'I want to learn the complete Spring Boot development workflow through this topic.', 'Rejected', 'The current skill profile does not fully match this topic. Please strengthen the prerequisites and apply again.', 2, NOW(), '0', 'student2');
