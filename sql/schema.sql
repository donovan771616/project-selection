-- CPT202 Project Selection System - MySQL 8.0 schema
-- Execute this file before sql/data.sql.

CREATE DATABASE IF NOT EXISTS cpt202_project_selection
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE cpt202_project_selection;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS project_application;
DROP TABLE IF EXISTS project_topic;
DROP TABLE IF EXISTS project_category;
DROP TABLE IF EXISTS sys_role_menu;
DROP TABLE IF EXISTS sys_user_role;
DROP TABLE IF EXISTS sys_menu;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS sys_user;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE sys_user (
    user_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    dept_id BIGINT NULL COMMENT '部门ID',
    user_name VARCHAR(30) NOT NULL COMMENT '用户名',
    nick_name VARCHAR(30) NOT NULL COMMENT '用户昵称',
    email VARCHAR(50) NOT NULL COMMENT '邮箱地址',
    phonenumber VARCHAR(11) NULL COMMENT '手机号码',
    sex CHAR(1) NOT NULL DEFAULT '2' COMMENT '性别（0男 1女 2未知）',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    status CHAR(1) NOT NULL DEFAULT '0' COMMENT '账号状态（0正常 1停用）',
    del_flag CHAR(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 2删除）',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (user_id),
    UNIQUE KEY uk_sys_user_name (user_name),
    UNIQUE KEY uk_sys_user_email (email),
    KEY idx_sys_user_status (status, del_flag)
) ENGINE=InnoDB COMMENT='用户表';

CREATE TABLE sys_role (
    role_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    role_name VARCHAR(30) NOT NULL COMMENT '角色名称',
    role_key VARCHAR(100) NOT NULL COMMENT '角色权限字符串',
    role_sort INT NOT NULL DEFAULT 0 COMMENT '显示顺序',
    data_scope CHAR(1) NOT NULL DEFAULT '1' COMMENT '数据范围',
    status CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
    del_flag CHAR(1) NOT NULL DEFAULT '0' COMMENT '删除标志',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (role_id),
    UNIQUE KEY uk_sys_role_key (role_key)
) ENGINE=InnoDB COMMENT='角色表';

CREATE TABLE sys_menu (
    menu_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
    menu_name VARCHAR(50) NOT NULL COMMENT '菜单名称',
    parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '父菜单ID',
    order_num INT NOT NULL DEFAULT 0 COMMENT '显示顺序',
    path VARCHAR(200) DEFAULT '' COMMENT '路由地址',
    component VARCHAR(255) DEFAULT NULL COMMENT '组件路径',
    is_frame INT NOT NULL DEFAULT 1 COMMENT '是否外链（0是 1否）',
    is_cache INT NOT NULL DEFAULT 1 COMMENT '是否缓存（0缓存 1不缓存）',
    menu_type CHAR(1) NOT NULL DEFAULT 'C' COMMENT '类型（M目录 C菜单 F按钮）',
    visible CHAR(1) NOT NULL DEFAULT '0' COMMENT '显示状态（0显示 1隐藏）',
    status CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
    perms VARCHAR(100) DEFAULT NULL COMMENT '权限标识',
    icon VARCHAR(100) DEFAULT '#' COMMENT '菜单图标',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (menu_id),
    KEY idx_sys_menu_parent (parent_id),
    KEY idx_sys_menu_perms (perms)
) ENGINE=InnoDB COMMENT='菜单权限表';

CREATE TABLE sys_user_role (
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user (user_id),
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role (role_id)
) ENGINE=InnoDB COMMENT='用户角色关联表';

CREATE TABLE sys_role_menu (
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (role_id, menu_id),
    CONSTRAINT fk_role_menu_role FOREIGN KEY (role_id) REFERENCES sys_role (role_id),
    CONSTRAINT fk_role_menu_menu FOREIGN KEY (menu_id) REFERENCES sys_menu (menu_id)
) ENGINE=InnoDB COMMENT='角色菜单关联表';

CREATE TABLE project_category (
    category_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    category_name VARCHAR(100) NOT NULL COMMENT '分类名称',
    parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '父分类ID',
    order_num INT NOT NULL DEFAULT 0 COMMENT '显示顺序',
    status CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
    del_flag CHAR(1) NOT NULL DEFAULT '0' COMMENT '删除标志',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (category_id),
    KEY idx_project_category_parent (parent_id),
    KEY idx_project_category_status (status, del_flag)
) ENGINE=InnoDB COMMENT='课题分类/标签表';

CREATE TABLE project_topic (
    topic_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '课题ID',
    teacher_id BIGINT NOT NULL COMMENT '创建教师ID',
    title VARCHAR(200) NOT NULL COMMENT '课题标题',
    description TEXT NOT NULL COMMENT '课题描述',
    skills VARCHAR(500) DEFAULT NULL COMMENT '所需技能',
    keywords VARCHAR(200) DEFAULT NULL COMMENT '关键词',
    category_id BIGINT DEFAULT NULL COMMENT '课题分类ID',
    max_students INT NOT NULL DEFAULT 1 COMMENT '最大参与学生数',
    status VARCHAR(20) NOT NULL DEFAULT 'Draft' COMMENT '状态',
    del_flag CHAR(1) NOT NULL DEFAULT '0' COMMENT '删除标志',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (topic_id),
    KEY idx_project_topic_teacher (teacher_id),
    KEY idx_project_topic_category (category_id),
    KEY idx_project_topic_status (status, del_flag),
    FULLTEXT KEY ft_project_topic_search (title, description, skills),
    CONSTRAINT fk_project_topic_teacher FOREIGN KEY (teacher_id) REFERENCES sys_user (user_id),
    CONSTRAINT fk_project_topic_category FOREIGN KEY (category_id) REFERENCES project_category (category_id),
    CONSTRAINT chk_project_topic_status CHECK (status IN ('Draft','Available','Requested','Agreed','Closed','Archived')),
    CONSTRAINT chk_project_topic_max_students CHECK (max_students > 0)
) ENGINE=InnoDB COMMENT='课题表';

CREATE TABLE project_application (
    application_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '申请ID',
    topic_id BIGINT NOT NULL COMMENT '课题ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    personal_note VARCHAR(500) DEFAULT NULL COMMENT '申请备注',
    status VARCHAR(20) NOT NULL DEFAULT 'Pending' COMMENT '状态',
    reject_reason VARCHAR(500) DEFAULT NULL COMMENT '拒绝原因',
    reject_by BIGINT DEFAULT NULL COMMENT '拒绝操作人ID',
    reject_time DATETIME DEFAULT NULL COMMENT '拒绝时间',
    del_flag CHAR(1) NOT NULL DEFAULT '0' COMMENT '删除标志',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (application_id),
    KEY idx_project_application_topic (topic_id),
    KEY idx_project_application_student (student_id),
    KEY idx_project_application_status (status, del_flag),
    CONSTRAINT fk_project_application_topic FOREIGN KEY (topic_id) REFERENCES project_topic (topic_id),
    CONSTRAINT fk_project_application_student FOREIGN KEY (student_id) REFERENCES sys_user (user_id),
    CONSTRAINT fk_project_application_reject_by FOREIGN KEY (reject_by) REFERENCES sys_user (user_id),
    CONSTRAINT chk_project_application_status CHECK (status IN ('Pending','Accepted','Rejected','Withdrawn'))
) ENGINE=InnoDB COMMENT='课题申请表';
