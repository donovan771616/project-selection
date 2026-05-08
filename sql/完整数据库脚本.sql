/*
SQLyog Ultimate v13.1.1 (64 bit)
MySQL - 5.7.36 : Database - cpt202_project_selection
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`cpt202_project_selection` /*!40100 DEFAULT CHARACTER SET utf8mb4 */;

USE `cpt202_project_selection`;

/*Table structure for table `project_application` */

DROP TABLE IF EXISTS `project_application`;

CREATE TABLE `project_application` (
  `application_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '申请ID',
  `topic_id` bigint(20) NOT NULL COMMENT '课题ID',
  `student_id` bigint(20) NOT NULL COMMENT '学生ID',
  `personal_note` varchar(500) DEFAULT NULL COMMENT '申请备注',
  `status` varchar(20) NOT NULL DEFAULT 'Pending' COMMENT '状态',
  `reject_reason` varchar(500) DEFAULT NULL COMMENT '拒绝原因',
  `reject_by` bigint(20) DEFAULT NULL COMMENT '拒绝操作人ID',
  `reject_time` datetime DEFAULT NULL COMMENT '拒绝时间',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '删除标志',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`application_id`),
  KEY `idx_project_application_topic` (`topic_id`),
  KEY `idx_project_application_student` (`student_id`),
  KEY `idx_project_application_status` (`status`,`del_flag`),
  KEY `idx_application_student_status` (`student_id`,`status`,`del_flag`),
  KEY `idx_application_topic_status` (`topic_id`,`status`,`del_flag`),
  KEY `fk_project_application_reject_by` (`reject_by`),
  CONSTRAINT `fk_project_application_reject_by` FOREIGN KEY (`reject_by`) REFERENCES `sys_user` (`user_id`),
  CONSTRAINT `fk_project_application_student` FOREIGN KEY (`student_id`) REFERENCES `sys_user` (`user_id`),
  CONSTRAINT `fk_project_application_topic` FOREIGN KEY (`topic_id`) REFERENCES `project_topic` (`topic_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COMMENT='课题申请表';

/*Data for the table `project_application` */

insert  into `project_application`(`application_id`,`topic_id`,`student_id`,`personal_note`,`status`,`reject_reason`,`reject_by`,`reject_time`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`) values 
(1,1,6,'I am very interested in this topic because it aligns with my academic interests and career goals. I believe my background in related subjects will enable me to contribute effectively to this project.','Withdrawn',NULL,NULL,NULL,'0','caofan','2026-04-29 07:00:40','caofan','2026-04-29 07:01:03'),
(2,1,6,'I have worked on similar projects in the past, which have given me relevant experience. I am excited about applying my knowledge to this new challenge.','Approved',NULL,NULL,NULL,'0','caofan','2026-04-29 07:01:10','teacher','2026-04-29 07:03:15'),
(3,1,3,'I have experience with the required skills including programming languages, frameworks, and tools mentioned in the requirements. I am confident in my ability to tackle the challenges of this project.','Rejected','111111111111111111111111111',2,'2026-04-29 11:49:00','0','student','2026-04-29 07:14:51','teacher','2026-04-29 11:49:00'),
(4,1,3,'I have worked on similar projects in the past, which have given me relevant experience. I am excited about applying my knowledge to this new challenge.','Rejected','123123123333333333333333333',2,'2026-04-29 12:45:52','0','student','2026-04-29 11:49:19','teacher','2026-04-29 12:45:52'),
(5,1,3,'I am eager to learn more about this field and believe this project will provide valuable learning opportunities. I am a quick learner and committed to delivering quality work.','Approved',NULL,NULL,NULL,'0','student','2026-04-29 12:46:03','teacher','2026-04-29 13:14:55'),
(6,2,3,'I have experience with the required skills including programming languages, frameworks, and tools mentioned in the requirements. I am confident in my ability to tackle the challenges of this project.','Withdrawn',NULL,NULL,NULL,'0','student','2026-04-29 13:55:17','student','2026-04-29 14:05:55'),
(7,2,3,'I am eager to learn more about this field and believe this project will provide valuable learning opportunities. I am a quick learner and committed to delivering quality work.','Approved',NULL,NULL,NULL,'0','student','2026-04-29 14:06:12','teacher','2026-04-29 14:40:12'),
(8,2,6,'I have worked on similar projects in the past, which have given me relevant experience. I am excited about applying my knowledge to this new challenge.','Rejected','Reject Application\r\nReject Application\r\nReject Application\r\nReject Application\r\n',2,'2026-04-29 16:08:36','0','caofan','2026-04-29 14:40:38','teacher','2026-04-29 16:08:36'),
(9,3,3,'I am eager to learn more about this field and believe this project will provide valuable learning opportunities. I am a quick learner and committed to delivering quality work.','Rejected','Reject Application\r\nReject Application\r\nReject Application\r\n',2,'2026-04-29 14:54:51','0','student','2026-04-29 14:54:31','teacher','2026-04-29 14:54:51'),
(10,3,5,'I am eager to learn more about this field and believe this project will provide valuable learning opportunities. I am a quick learner and committed to delivering quality work.','Approved',NULL,NULL,NULL,'0','student2','2026-04-29 16:28:34','teacher','2026-04-29 16:34:05');

/*Table structure for table `project_application_log` */

DROP TABLE IF EXISTS `project_application_log`;

CREATE TABLE `project_application_log` (
  `log_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `application_id` bigint(20) NOT NULL COMMENT '申请ID',
  `action` varchar(30) NOT NULL COMMENT '操作动作',
  `operator_id` bigint(20) DEFAULT NULL COMMENT '操作人ID',
  `operator_name` varchar(64) DEFAULT '' COMMENT '操作人账号',
  `reason` varchar(500) DEFAULT NULL COMMENT '操作原因',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`log_id`),
  KEY `idx_application_log_application` (`application_id`),
  KEY `fk_application_log_operator` (`operator_id`),
  CONSTRAINT `fk_application_log_application` FOREIGN KEY (`application_id`) REFERENCES `project_application` (`application_id`),
  CONSTRAINT `fk_application_log_operator` FOREIGN KEY (`operator_id`) REFERENCES `sys_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COMMENT='课题申请操作日志表';

/*Data for the table `project_application_log` */

insert  into `project_application_log`(`log_id`,`application_id`,`action`,`operator_id`,`operator_name`,`reason`,`create_time`) values 
(1,1,'Submitted',6,'caofan','Application submitted','2026-04-29 07:00:40'),
(2,1,'Withdrawn',6,'caofan','Application withdrawn','2026-04-29 07:01:04'),
(3,2,'Submitted',6,'caofan','Application submitted','2026-04-29 07:01:10'),
(4,2,'Approved',2,'teacher','Application accepted','2026-04-29 07:03:15'),
(5,3,'Submitted',3,'student','Application submitted','2026-04-29 07:14:51'),
(6,3,'Rejected',2,'teacher','111111111111111111111111111','2026-04-29 11:49:00'),
(7,4,'Submitted',3,'student','Application submitted','2026-04-29 11:49:19'),
(8,4,'Rejected',2,'teacher','123123123333333333333333333','2026-04-29 12:45:52'),
(9,5,'Submitted',3,'student','Application submitted','2026-04-29 12:46:03'),
(10,5,'Approved',2,'teacher','Application accepted','2026-04-29 13:14:56'),
(11,6,'Submitted',3,'student','Application submitted','2026-04-29 13:55:17'),
(12,6,'Withdrawn',3,'student','Application withdrawn','2026-04-29 14:05:56'),
(13,7,'Submitted',3,'student','Application submitted','2026-04-29 14:06:12'),
(14,7,'Approved',2,'teacher','Application accepted','2026-04-29 14:40:12'),
(15,8,'Submitted',6,'caofan','Application submitted','2026-04-29 14:40:39'),
(16,9,'Submitted',3,'student','Application submitted','2026-04-29 14:54:31'),
(17,9,'Rejected',2,'teacher','Reject Application\r\nReject Application\r\nReject Application\r\n','2026-04-29 14:54:51'),
(18,8,'Rejected',2,'teacher','Reject Application\r\nReject Application\r\nReject Application\r\nReject Application\r\n','2026-04-29 16:08:37'),
(19,10,'Submitted',5,'student2','Application submitted','2026-04-29 16:28:34'),
(20,10,'Approved',2,'teacher','Application accepted','2026-04-29 16:34:05');

/*Table structure for table `project_category` */

DROP TABLE IF EXISTS `project_category`;

CREATE TABLE `project_category` (
  `category_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `category_name` varchar(100) NOT NULL COMMENT '分类名称',
  `parent_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '父分类ID',
  `order_num` int(11) NOT NULL DEFAULT '0' COMMENT '显示顺序',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '删除标志',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`category_id`),
  KEY `idx_project_category_parent` (`parent_id`),
  KEY `idx_project_category_status` (`status`,`del_flag`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COMMENT='课题分类/标签表';

/*Data for the table `project_category` */

insert  into `project_category`(`category_id`,`category_name`,`parent_id`,`order_num`,`status`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`) values 
(1,'Artificial Intelligence',0,1,'0','0','admin','2026-04-28 22:48:31','',NULL),
(2,'Web Development',0,2,'0','0','admin','2026-04-28 22:48:31','',NULL),
(3,'Data Analytics',0,3,'0','0','admin','2026-04-28 22:48:31','',NULL),
(4,'Mobile Applications',0,4,'0','0','admin','2026-04-28 22:48:31','',NULL),
(5,'Software Engineering Tools',0,5,'0','0','admin','2026-04-28 22:48:31','',NULL),
(6,'Mathematics',0,1,'0','0','admin','2026-04-29 15:19:27','',NULL);

/*Table structure for table `project_notification` */

DROP TABLE IF EXISTS `project_notification`;

CREATE TABLE `project_notification` (
  `notification_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `title` varchar(200) NOT NULL COMMENT '通知标题',
  `content` text COMMENT '通知内容',
  `type` varchar(50) NOT NULL COMMENT '通知类型：APPLICATION_APPROVED-申请通过，APPLICATION_REJECTED-申请拒绝，CONFIRM_REMINDER-确认提醒，OVERDUE_REMINDER-逾期提醒，REVIEW_REMINDER-审核提醒，PUSH_FAILED-推送失败',
  `related_id` bigint(20) DEFAULT NULL COMMENT '关联ID（如申请ID）',
  `related_type` varchar(50) DEFAULT NULL COMMENT '关联类型（如APPLICATION）',
  `sender_id` bigint(20) DEFAULT NULL COMMENT '发送者ID',
  `sender_name` varchar(100) DEFAULT NULL COMMENT '发送者名称',
  `priority` varchar(20) DEFAULT 'NORMAL' COMMENT '优先级：HIGH-高，NORMAL-普通，LOW-低',
  `status` varchar(20) DEFAULT 'PENDING' COMMENT '状态：PENDING-待处理，CONFIRMED-已确认，SIGNED-已签收，EXPIRED-已过期，CANCELLED-已取消',
  `require_confirmation` tinyint(1) DEFAULT '0' COMMENT '是否需要确认签收：0-否，1-是',
  `confirmation_deadline` datetime DEFAULT NULL COMMENT '确认截止时间',
  `send_status` varchar(20) DEFAULT 'SENT' COMMENT '发送状态：PENDING-待发送，SENT-已发送，FAILED-失败，PARTIAL_FAILED-部分失败',
  `send_error` text COMMENT '发送错误信息',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志：0-正常，1-删除',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`notification_id`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`),
  KEY `idx_related_id` (`related_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

/*Data for the table `project_notification` */

insert  into `project_notification`(`notification_id`,`title`,`content`,`type`,`related_id`,`related_type`,`sender_id`,`sender_name`,`priority`,`status`,`require_confirmation`,`confirmation_deadline`,`send_status`,`send_error`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`) values 
(1,'课题申请未通过','很遗憾，您的课题申请未通过。\n\n课题名称：Deep Learning Image Recognition Application\n拒绝原因：Reject Application\r\nReject Application\r\nReject Application\r\n\n\n如有疑问，请联系指导教师。','APPLICATION_REJECTED',9,'APPLICATION',2,'teacher','NORMAL','PENDING',0,NULL,'SENT',NULL,'0','teacher','2026-04-29 14:54:52','',NULL),
(2,'Application Rejected','Unfortunately, your application has been rejected.\n\nTopic: Container Orchestration Platform\nReason: Reject Application\r\nReject Application\r\nReject Application\r\nReject Application\r\n\n\nPlease contact your supervisor if you have any questions.','APPLICATION_REJECTED',8,'APPLICATION',2,'teacher','NORMAL','PENDING',0,NULL,'SENT',NULL,'0','teacher','2026-04-29 16:08:37','',NULL),
(3,'Application Approved','Congratulations! Your application has been approved.\n\nTopic: Deep Learning Image Recognition Application\nSupervisor: Dr. Taylor\n\nPlease confirm and sign by 2026-05-03 00:34.','APPLICATION_APPROVED',10,'APPLICATION',2,'teacher','HIGH','PENDING',1,'2026-05-03 00:34:06','SENT',NULL,'0','teacher','2026-04-29 16:34:06','',NULL);

/*Table structure for table `project_notification_recipient` */

DROP TABLE IF EXISTS `project_notification_recipient`;

CREATE TABLE `project_notification_recipient` (
  `recipient_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '接收者ID',
  `notification_id` bigint(20) NOT NULL COMMENT '通知ID',
  `user_id` bigint(20) NOT NULL COMMENT '接收者用户ID',
  `user_name` varchar(100) DEFAULT NULL COMMENT '接收者用户名',
  `is_read` tinyint(1) DEFAULT '0' COMMENT '是否已读：0-未读，1-已读',
  `read_time` datetime DEFAULT NULL COMMENT '阅读时间',
  `is_confirmed` tinyint(1) DEFAULT '0' COMMENT '是否已确认：0-未确认，1-已确认',
  `confirmed_time` datetime DEFAULT NULL COMMENT '确认时间',
  `is_notified` tinyint(1) DEFAULT '0' COMMENT '是否已发送提醒：0-否，1-是',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志：0-正常，1-删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`recipient_id`),
  KEY `idx_notification_id` (`notification_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_is_read` (`is_read`),
  KEY `idx_user_unread` (`user_id`,`is_read`,`del_flag`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COMMENT='通知接收者表';

/*Data for the table `project_notification_recipient` */

insert  into `project_notification_recipient`(`recipient_id`,`notification_id`,`user_id`,`user_name`,`is_read`,`read_time`,`is_confirmed`,`confirmed_time`,`is_notified`,`del_flag`,`create_time`,`update_time`) values 
(1,1,3,'Alex Lee',1,'2026-04-29 15:48:37',0,NULL,0,'0','2026-04-29 14:54:52','2026-04-29 15:48:37'),
(2,2,6,'caofan',0,NULL,0,NULL,0,'0','2026-04-29 16:08:37',NULL),
(3,3,5,'Casey Chen',0,NULL,0,NULL,0,'0','2026-04-29 16:34:06',NULL);

/*Table structure for table `project_notification_send_log` */

DROP TABLE IF EXISTS `project_notification_send_log`;

CREATE TABLE `project_notification_send_log` (
  `log_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `notification_id` bigint(20) NOT NULL COMMENT '通知ID',
  `recipient_id` bigint(20) DEFAULT NULL COMMENT '接收者ID',
  `user_id` bigint(20) NOT NULL COMMENT '目标用户ID',
  `send_status` varchar(20) NOT NULL COMMENT '发送状态：SUCCESS-成功，FAILED-失败',
  `error_message` text COMMENT '错误信息',
  `send_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  `retry_count` int(11) DEFAULT '0' COMMENT '重试次数',
  PRIMARY KEY (`log_id`),
  KEY `idx_notification_id` (`notification_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_send_status` (`send_status`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COMMENT='通知发送历史表';

/*Data for the table `project_notification_send_log` */

insert  into `project_notification_send_log`(`log_id`,`notification_id`,`recipient_id`,`user_id`,`send_status`,`error_message`,`send_time`,`retry_count`) values 
(1,1,NULL,3,'SENT',NULL,'2026-04-29 14:54:52',0),
(2,2,NULL,6,'SENT',NULL,'2026-04-29 16:08:37',0),
(3,3,NULL,5,'SENT',NULL,'2026-04-29 16:34:06',0);

/*Table structure for table `project_topic` */

DROP TABLE IF EXISTS `project_topic`;

CREATE TABLE `project_topic` (
  `topic_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '课题ID',
  `teacher_id` bigint(20) NOT NULL COMMENT '创建教师ID',
  `title` varchar(200) NOT NULL COMMENT '课题标题',
  `description` text NOT NULL COMMENT '课题描述',
  `skills` varchar(500) DEFAULT NULL COMMENT '所需技能',
  `keywords` varchar(200) DEFAULT NULL COMMENT '关键词',
  `category_id` bigint(20) DEFAULT NULL COMMENT '课题分类ID',
  `max_students` int(11) NOT NULL DEFAULT '1' COMMENT '最大参与学生数',
  `status` varchar(20) NOT NULL DEFAULT 'Draft' COMMENT '状态',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '删除标志',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`topic_id`),
  KEY `idx_project_topic_teacher` (`teacher_id`),
  KEY `idx_project_topic_category` (`category_id`),
  KEY `idx_project_topic_status` (`status`,`del_flag`),
  KEY `idx_topic_teacher_status` (`teacher_id`,`status`,`del_flag`),
  CONSTRAINT `fk_project_topic_category` FOREIGN KEY (`category_id`) REFERENCES `project_category` (`category_id`),
  CONSTRAINT `fk_project_topic_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `sys_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COMMENT='课题表';

/*Data for the table `project_topic` */

insert  into `project_topic`(`topic_id`,`teacher_id`,`title`,`description`,`skills`,`keywords`,`category_id`,`max_students`,`status`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`) values 
(1,2,'Cloud-native Container Orchestration Platform','The objective of this project is to create a scalable and maintainable application that can handle real-world usage scenarios. Students will learn about software architecture, design patterns, and best practices in software engineering. The project involves both frontend and backend development.','C++, Qt, OpenGL, CUDA','security, cryptography, authentication',5,2,'Closed','0','teacher','2026-04-29 06:25:29','teacher','2026-04-29 13:18:30'),
(2,2,'Container Orchestration Platform','This research-oriented project focuses on exploring cutting-edge technologies and applying them to solve practical problems. Students will conduct literature reviews, design experiments, and implement prototypes. The project requires analytical thinking and problem-solving skills.','Python, Django, PostgreSQL, Docker','IoT, embedded systems, sensors',3,1,'Open','0','teacher','2026-04-29 13:15:35','teacher','2026-04-29 13:15:50'),
(3,2,'Deep Learning Image Recognition Application','The objective of this project is to create a scalable and maintainable application that can handle real-world usage scenarios. Students will learn about software architecture, design patterns, and best practices in software engineering. The project involves both frontend and backend development.','C++, Qt, OpenGL, CUDA','IoT, embedded systems, sensors',4,3,'Open','0','teacher','2026-04-29 14:53:55','teacher','2026-04-29 14:54:06');

/*Table structure for table `sys_menu` */

DROP TABLE IF EXISTS `sys_menu`;

CREATE TABLE `sys_menu` (
  `menu_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `menu_name` varchar(50) NOT NULL COMMENT '菜单名称',
  `parent_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '父菜单ID',
  `order_num` int(11) NOT NULL DEFAULT '0' COMMENT '显示顺序',
  `path` varchar(200) DEFAULT '' COMMENT '路由地址',
  `component` varchar(255) DEFAULT NULL COMMENT '组件路径',
  `is_frame` int(11) NOT NULL DEFAULT '1' COMMENT '是否外链（0是 1否）',
  `is_cache` int(11) NOT NULL DEFAULT '1' COMMENT '是否缓存（0缓存 1不缓存）',
  `menu_type` char(1) NOT NULL DEFAULT 'C' COMMENT '类型（M目录 C菜单 F按钮）',
  `visible` char(1) NOT NULL DEFAULT '0' COMMENT '显示状态（0显示 1隐藏）',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `perms` varchar(100) DEFAULT NULL COMMENT '权限标识',
  `icon` varchar(100) DEFAULT '#' COMMENT '菜单图标',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`menu_id`),
  KEY `idx_sys_menu_parent` (`parent_id`),
  KEY `idx_sys_menu_perms` (`perms`),
  KEY `idx_sys_menu_visible_status` (`visible`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=446 DEFAULT CHARSET=utf8mb4 COMMENT='菜单权限表';

/*Data for the table `sys_menu` */

insert  into `sys_menu`(`menu_id`,`menu_name`,`parent_id`,`order_num`,`path`,`component`,`is_frame`,`is_cache`,`menu_type`,`visible`,`status`,`perms`,`icon`,`create_by`,`create_time`,`update_by`,`update_time`) values 
(1,'Dashboard',0,1,'/index','dashboard',1,1,'C','0','0',NULL,'dashboard','system','2026-04-28 22:48:31','',NULL),
(10,'Users',0,2,'/system/users','system/user',1,1,'C','0','0','system:user:list','user','system','2026-04-28 22:48:31','','2026-04-29 00:46:27'),
(11,'Create User',10,1,'',NULL,1,1,'F','1','0','system:user:add','#','system','2026-04-28 22:48:31','',NULL),
(12,'Edit User',10,2,'',NULL,1,1,'F','1','0','system:user:edit','#','system','2026-04-28 22:48:31','',NULL),
(13,'Delete User',10,3,'',NULL,1,1,'F','1','0','system:user:remove','#','system','2026-04-28 22:48:31','',NULL),
(20,'Roles',0,20,'/system/roles','system/role',1,1,'C','0','0','system:role:list','role','system','2026-04-28 22:48:31','',NULL),
(21,'Assign Roles',20,1,'',NULL,1,1,'F','1','0','system:role:edit','#','system','2026-04-28 22:48:31','',NULL),
(101,'Create Topic',100,1,'',NULL,1,1,'F','1','0','teacher:topic:add','#','system','2026-04-28 16:48:26','',NULL),
(102,'Edit Topic',100,2,'',NULL,1,1,'F','1','0','teacher:topic:edit','#','system','2026-04-28 16:48:26','',NULL),
(103,'Publish Topic',100,3,'',NULL,1,1,'F','1','0','teacher:topic:publish','#','system','2026-04-28 16:48:26','',NULL),
(104,'Close Topic',100,4,'',NULL,1,1,'F','1','0','teacher:topic:close','#','system','2026-04-28 16:48:26','',NULL),
(105,'Archive Topic',100,5,'',NULL,1,1,'F','1','0','teacher:topic:archive','#','system','2026-04-28 16:48:26','',NULL),
(111,'Review Application',110,1,'',NULL,1,1,'F','1','0','teacher:application:review','#','system','2026-04-28 16:48:26','',NULL),
(112,'Batch Review',110,2,'',NULL,1,1,'F','1','0','teacher:application:batch','#','system','2026-04-28 16:48:26','',NULL),
(201,'View Topic Detail',200,1,'',NULL,1,1,'F','1','0','student:topic:query','#','system','2026-04-28 16:48:27','',NULL),
(211,'Submit Application',210,1,'',NULL,1,1,'F','1','0','student:application:add','#','system','2026-04-28 16:48:27','',NULL),
(212,'Withdraw Application',210,2,'',NULL,1,1,'F','1','0','student:application:withdraw','#','system','2026-04-28 16:48:27','',NULL),
(321,'View Agreement',320,1,'',NULL,1,1,'F','1','0','admin:agreement:query','#','system','2026-04-28 16:48:27','',NULL),
(322,'Revoke Agreement',320,2,'',NULL,1,1,'F','1','0','admin:agreement:revoke','#','system','2026-04-28 16:48:27','',NULL),
(331,'Export Logs',330,1,'',NULL,1,1,'F','1','0','admin:audit:export','#','system','2026-04-28 16:48:27','',NULL),
(401,'Mark Read',400,1,'',NULL,1,1,'F','1','0','system:notification:read','#','system','2026-04-28 16:48:28','',NULL),
(410,'Profile',0,110,'/profile','user/profile',1,1,'C','0','0','system:user:profile','user','system','2026-04-28 16:48:28','',NULL),
(431,'Create User',430,1,'',NULL,1,1,'F','1','0','admin:user:add','#','system','2026-04-28 16:48:28','',NULL),
(432,'Edit User',430,2,'',NULL,1,1,'F','1','0','admin:user:edit','#','system','2026-04-28 16:48:28','',NULL),
(433,'Delete User',430,3,'',NULL,1,1,'F','1','0','admin:user:remove','#','system','2026-04-28 16:48:28','',NULL),
(434,'Teacher Topics',0,1,'/teacher','Layout',1,1,'M','0','0',NULL,'edit','system','2026-04-29 00:44:21','',NULL),
(435,'My Topics',434,1,'topics','teacher/topics',1,1,'C','0','0',NULL,'list','system','2026-04-29 00:44:21','',NULL),
(436,'Review Applications',434,2,'applications','teacher/applications',1,1,'C','0','0',NULL,'check','system','2026-04-29 00:44:21','',NULL),
(437,'Student Topics',0,2,'/student','Layout',1,1,'M','0','0',NULL,'form','system','2026-04-29 00:44:21','',NULL),
(438,'Browse Topics',437,1,'topics','student/topics',1,1,'C','0','0',NULL,'list','system','2026-04-29 00:44:22','',NULL),
(439,'My Applications',437,2,'applications','student/applications',1,1,'C','0','0',NULL,'documentation','system','2026-04-29 00:44:22','',NULL),
(440,'Admin Management',0,3,'/admin','Layout',1,1,'M','0','0',NULL,'setting','system','2026-04-29 00:44:22','',NULL),
(441,'Topic Management',440,1,'topics','admin/topics',1,1,'C','0','0',NULL,'list','system','2026-04-29 00:44:22','',NULL),
(442,'Application Management',440,2,'applications','admin/applications',1,1,'C','0','0',NULL,'check','system','2026-04-29 00:44:22','',NULL),
(443,'User Management',440,3,'user','system/user/index',1,1,'C','0','0','system:user:list','user','system','2026-04-29 00:44:22','',NULL),
(444,'Category Management',440,4,'categories','admin/categories',1,1,'C','0','0',NULL,'chart','system','2026-04-29 00:44:22','',NULL),
(445,'Reports',440,5,'reports','admin/reports',1,1,'C','0','0',NULL,'chart','system','2026-04-29 00:44:22','',NULL);

/*Table structure for table `sys_role` */

DROP TABLE IF EXISTS `sys_role`;

CREATE TABLE `sys_role` (
  `role_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(30) NOT NULL COMMENT '角色名称',
  `role_key` varchar(100) NOT NULL COMMENT '角色权限字符串',
  `role_sort` int(11) NOT NULL DEFAULT '0' COMMENT '显示顺序',
  `data_scope` char(1) NOT NULL DEFAULT '1' COMMENT '数据范围',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '删除标志',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`role_id`),
  UNIQUE KEY `uk_sys_role_key` (`role_key`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

/*Data for the table `sys_role` */

insert  into `sys_role`(`role_id`,`role_name`,`role_key`,`role_sort`,`data_scope`,`status`,`del_flag`,`remark`,`create_by`,`create_time`,`update_by`,`update_time`) values 
(1,'Admin','admin',1,'1','0','0','Full access','system','2026-04-28 22:48:31','',NULL),
(2,'Teacher','teacher',2,'5','0','0','Supervisor account','system','2026-04-28 22:48:31','',NULL),
(3,'Student','student',3,'5','0','0','Student account','system','2026-04-28 22:48:31','',NULL);

/*Table structure for table `sys_role_menu` */

DROP TABLE IF EXISTS `sys_role_menu`;

CREATE TABLE `sys_role_menu` (
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `menu_id` bigint(20) NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (`role_id`,`menu_id`),
  KEY `idx_sys_role_menu_role` (`role_id`),
  KEY `fk_role_menu_menu` (`menu_id`),
  CONSTRAINT `fk_role_menu_menu` FOREIGN KEY (`menu_id`) REFERENCES `sys_menu` (`menu_id`),
  CONSTRAINT `fk_role_menu_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

/*Data for the table `sys_role_menu` */

insert  into `sys_role_menu`(`role_id`,`menu_id`) values 
(1,1),
(1,10),
(1,11),
(1,12),
(1,13),
(1,20),
(1,21),
(1,101),
(1,102),
(1,103),
(1,104),
(1,105),
(1,111),
(1,112),
(1,201),
(1,211),
(1,212),
(1,321),
(1,322),
(1,331),
(1,401),
(1,410),
(1,431),
(1,432),
(1,433),
(1,434),
(1,435),
(1,436),
(1,437),
(1,438),
(1,439),
(1,440),
(1,441),
(1,442),
(1,443),
(1,444),
(1,445),
(2,434),
(3,437);

/*Table structure for table `sys_user` */

DROP TABLE IF EXISTS `sys_user`;

CREATE TABLE `sys_user` (
  `user_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `student_no` varchar(30) DEFAULT NULL COMMENT '学号',
  `employee_no` varchar(30) DEFAULT NULL COMMENT '工号/教职工号',
  `title` varchar(100) DEFAULT NULL COMMENT '职称/职位',
  `grade` varchar(20) DEFAULT NULL COMMENT '年级（如2022级）',
  `class_name` varchar(50) DEFAULT NULL COMMENT '班级名称（如软件1班）',
  `user_name` varchar(30) NOT NULL COMMENT '用户名',
  `nick_name` varchar(30) NOT NULL COMMENT '用户昵称',
  `email` varchar(50) NOT NULL COMMENT '邮箱地址',
  `phonenumber` varchar(11) DEFAULT NULL COMMENT '手机号码',
  `sex` char(1) NOT NULL DEFAULT '2' COMMENT '性别（0男 1女 2未知）',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '账号状态（0正常 1停用）',
  `email_activated` char(1) NOT NULL DEFAULT '1' COMMENT '邮箱激活状态（0未激活 1已激活）',
  `activation_token` varchar(100) DEFAULT NULL COMMENT '邮箱激活令牌',
  `activation_expires_at` datetime DEFAULT NULL COMMENT '激活令牌过期时间',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 2删除）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `dept_name` varchar(100) DEFAULT NULL COMMENT '院系/部门名称',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_sys_user_name` (`user_name`),
  UNIQUE KEY `uk_sys_user_email` (`email`),
  KEY `idx_sys_user_status` (`status`,`del_flag`),
  KEY `idx_sys_user_activation_token` (`activation_token`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

/*Data for the table `sys_user` */

insert  into `sys_user`(`user_id`,`student_no`,`employee_no`,`title`,`grade`,`class_name`,`user_name`,`nick_name`,`email`,`phonenumber`,`sex`,`password`,`status`,`email_activated`,`activation_token`,`activation_expires_at`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`,`dept_name`) values 
(1,NULL,NULL,NULL,NULL,NULL,'admin','System Admin','admin@example.com','13800000001','2','$2b$12$FYLJiK/DLFhyudvmbQFvDuM2TZ4JIa5mp1VRgZevJ.Ba3fCsEuDMi','0','1',NULL,NULL,'0','system','2026-04-28 22:48:31','',NULL,NULL),
(2,NULL,NULL,NULL,NULL,NULL,'teacher','Dr. Taylor','teacher@example.com','13800000002','2','$2b$12$VcLhJvykO/0zHKT7sqrw1OzswmeePtBN.spVawEkCVJydw9CVRZhe','0','1',NULL,NULL,'0','system','2026-04-28 22:48:31','',NULL,NULL),
(3,NULL,NULL,NULL,NULL,NULL,'student','Alex Lee','student@example.com','13800000003','2','$2b$12$H1g6Oar9x03egAZ/GVfHL.IiWr16UCxQY6/oRkvHOn/pWV6BorAHS','0','1',NULL,NULL,'0','system','2026-04-28 22:48:31','',NULL,NULL),
(4,NULL,NULL,NULL,NULL,NULL,'teacher2','Prof. Morgan','teacher2@example.com','13800000004','2','$2b$12$VcLhJvykO/0zHKT7sqrw1OzswmeePtBN.spVawEkCVJydw9CVRZhe','0','1',NULL,NULL,'0','system','2026-04-28 22:48:31','',NULL,NULL),
(5,NULL,NULL,NULL,NULL,NULL,'student2','Casey Chen','student2@example.com','13800000005','2','$2b$12$H1g6Oar9x03egAZ/GVfHL.IiWr16UCxQY6/oRkvHOn/pWV6BorAHS','0','1',NULL,NULL,'0','system','2026-04-28 22:48:31','',NULL,NULL),
(6,NULL,NULL,NULL,NULL,NULL,'caofan','caofan','unigood@163.com',NULL,'2','$2a$10$jzkmlmG8KMRBzJRUI2dcAuszS364v7BKIZsLpxPyI1z44/5xO2MiS','0','1',NULL,NULL,'0','register','2026-04-28 15:12:23','activation','2026-04-28 15:49:00',NULL);

/*Table structure for table `sys_user_role` */

DROP TABLE IF EXISTS `sys_user_role`;

CREATE TABLE `sys_user_role` (
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`,`role_id`),
  KEY `idx_sys_user_role_user` (`user_id`),
  KEY `fk_user_role_role` (`role_id`),
  CONSTRAINT `fk_user_role_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`role_id`),
  CONSTRAINT `fk_user_role_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

/*Data for the table `sys_user_role` */

insert  into `sys_user_role`(`user_id`,`role_id`) values 
(1,1),
(2,2),
(3,3),
(4,2),
(5,3),
(6,3);

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
