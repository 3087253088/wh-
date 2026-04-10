CREATE DATABASE IF NOT EXISTS `smart_finance` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `smart_finance`;

-- ==================== 1. 部门表 ====================
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `dept_name` varchar(100) NOT NULL COMMENT '部门名称',
  `parent_id` bigint(20) DEFAULT '0' COMMENT '父部门ID',
  `sort_order` int(11) DEFAULT '0' COMMENT '排序',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态：1-启用，0-禁用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- 初始化部门数据
INSERT INTO `sys_dept` VALUES (1, '市场部', 0, 1, 1, NOW(), NOW());
INSERT INTO `sys_dept` VALUES (2, '销售部', 0, 2, 1, NOW(), NOW());
INSERT INTO `sys_dept` VALUES (3, '技术部', 0, 3, 1, NOW(), NOW());
INSERT INTO `sys_dept` VALUES (4, '行政部', 0, 4, 1, NOW(), NOW());
INSERT INTO `sys_dept` VALUES (5, '财务部', 0, 5, 1, NOW(), NOW());

-- ==================== 2. 用户表 ====================
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '密码',
  `name` varchar(50) NOT NULL COMMENT '姓名',
  `dept_id` bigint(20) DEFAULT NULL COMMENT '部门ID',
  `position` varchar(50) DEFAULT NULL COMMENT '职位',
  `role_code` varchar(20) NOT NULL DEFAULT 'employee' COMMENT '角色代码',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态：1-启用，0-禁用',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(50) DEFAULT NULL COMMENT '最后登录IP',
  `entry_date` date DEFAULT NULL COMMENT '入职日期',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 初始化用户数据（密码：123456 BCrypt加密）
INSERT INTO `sys_user` VALUES (1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', '管理员', 5, '系统管理员', 'admin', '13800138000', 'admin@finance.com', NULL, 1, NULL, NULL, '2024-01-01', NOW(), NOW(), 'system', 'system', 0);
INSERT INTO `sys_user` VALUES (2, 'finance', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', '张财务', 5, '财务经理', 'finance', '13800138001', 'finance@finance.com', NULL, 1, NULL, NULL, '2024-01-01', NOW(), NOW(), 'system', 'system', 0);
INSERT INTO `sys_user` VALUES (3, 'employee', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', '李员工', 1, '市场专员', 'employee', '13800138002', 'employee@finance.com', NULL, 1, NULL, NULL, '2024-01-01', NOW(), NOW(), 'system', 'system', 0);

-- ==================== 3. 报销单表 ====================
DROP TABLE IF EXISTS `expense_claim`;
CREATE TABLE `expense_claim` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `claim_no` varchar(50) NOT NULL COMMENT '报销单号',
  `user_id` bigint(20) NOT NULL COMMENT '申请人ID',
  `dept_id` bigint(20) NOT NULL COMMENT '部门ID',
  `title` varchar(200) NOT NULL COMMENT '报销事由',
  `total_amount` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '报销总金额',
  `status` varchar(20) NOT NULL DEFAULT 'draft' COMMENT '状态：draft-草稿，pending-审批中，approved-已通过，rejected-已驳回',
  `expense_date` date NOT NULL COMMENT '费用发生日期',
  `description` text COMMENT '报销说明',
  `current_approver_id` bigint(20) DEFAULT NULL COMMENT '当前审批人ID',
  `submit_time` datetime DEFAULT NULL COMMENT '提交时间',
  `approve_time` datetime DEFAULT NULL COMMENT '审批完成时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_claim_no` (`claim_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报销单表';

-- ==================== 4. 报销明细表 ====================
DROP TABLE IF EXISTS `expense_detail`;
CREATE TABLE `expense_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `claim_id` bigint(20) NOT NULL COMMENT '报销单ID',
  `expense_type` varchar(30) NOT NULL COMMENT '费用类型',
  `amount` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '金额',
  `invoice_no` varchar(50) DEFAULT NULL COMMENT '发票号',
  `description` varchar(500) DEFAULT NULL COMMENT '明细说明',
  `expense_date` date NOT NULL COMMENT '费用日期',
  PRIMARY KEY (`id`),
  KEY `idx_claim_id` (`claim_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报销明细表';

-- ==================== 5. 审批记录表 ====================
DROP TABLE IF EXISTS `approval_record`;
CREATE TABLE `approval_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `claim_id` bigint(20) NOT NULL COMMENT '报销单ID',
  `approver_id` bigint(20) NOT NULL COMMENT '审批人ID',
  `approver_name` varchar(50) NOT NULL COMMENT '审批人姓名',
  `result` varchar(20) NOT NULL COMMENT '结果：approved-通过，rejected-驳回',
  `comment` varchar(500) DEFAULT NULL COMMENT '审批意见',
  `approval_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '审批时间',
  `approval_order` int(11) NOT NULL DEFAULT '1' COMMENT '审批顺序',
  PRIMARY KEY (`id`),
  KEY `idx_claim_id` (`claim_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批记录表';

-- ==================== 6. 发票表 ====================
DROP TABLE IF EXISTS `invoice`;
CREATE TABLE `invoice` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `invoice_code` varchar(50) NOT NULL COMMENT '发票号码',
  `invoice_type` varchar(30) NOT NULL COMMENT '发票类型',
  `amount` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '发票金额',
  `drawer` varchar(200) NOT NULL COMMENT '开票方',
  `drawer_tax_no` varchar(50) DEFAULT NULL COMMENT '开票方税号',
  `invoice_date` date NOT NULL COMMENT '开票日期',
  `user_id` bigint(20) NOT NULL COMMENT '所属用户ID',
  `claim_id` bigint(20) DEFAULT NULL COMMENT '关联报销单ID',
  `image_url` varchar(255) DEFAULT NULL COMMENT '发票图片URL',
  `verify_status` varchar(20) NOT NULL DEFAULT 'unverified' COMMENT '验证状态',
  `verify_time` datetime DEFAULT NULL COMMENT '验证时间',
  `verify_result` varchar(500) DEFAULT NULL COMMENT '验证结果详情',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '录入时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_invoice_code` (`invoice_code`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_claim_id` (`claim_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发票表';

-- ==================== 7. 预算表 ====================
DROP TABLE IF EXISTS `budget`;
CREATE TABLE `budget` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `dept_id` bigint(20) NOT NULL COMMENT '部门ID',
  `expense_type` varchar(30) NOT NULL COMMENT '费用类型',
  `year` int(4) NOT NULL COMMENT '年份',
  `month` int(2) NOT NULL COMMENT '月份',
  `budget_amount` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '预算金额',
  `used_amount` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '已使用金额',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态：1-启用，0-停用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dept_type_year_month` (`dept_id`,`expense_type`,`year`,`month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预算表';

-- ==================== 8. 消息通知表 ====================
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '接收用户ID',
  `type` varchar(30) NOT NULL COMMENT '消息类型',
  `title` varchar(200) NOT NULL COMMENT '消息标题',
  `content` text NOT NULL COMMENT '消息内容',
  `related_id` bigint(20) DEFAULT NULL COMMENT '关联业务ID',
  `is_read` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已读：0-未读，1-已读',
  `read_time` datetime DEFAULT NULL COMMENT '阅读时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_is_read` (`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息通知表';

-- ==================== 9. 操作日志表 ====================
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) DEFAULT NULL COMMENT '操作用户ID',
  `username` varchar(50) DEFAULT NULL COMMENT '操作用户名',
  `module` varchar(50) NOT NULL COMMENT '操作模块',
  `operation` varchar(50) NOT NULL COMMENT '操作类型',
  `method` varchar(200) DEFAULT NULL COMMENT '请求方法',
  `params` text COMMENT '请求参数',
  `result` text COMMENT '返回结果',
  `ip` varchar(50) DEFAULT NULL COMMENT '操作IP',
  `duration` int(11) DEFAULT NULL COMMENT '耗时(毫秒)',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态：1-成功，0-失败',
  `error_msg` text COMMENT '错误信息',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_module` (`module`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';
