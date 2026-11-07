-- =====================================================
-- P17 日常照护包 + 家属服务包
-- 请假/外出、用药提醒、护理日志、家属投诉建议、发票申请、探访核销
-- =====================================================

-- 1. 请假/外出申请
CREATE TABLE IF NOT EXISTS `biz_leave` (
  `leave_id`      bigint       NOT NULL AUTO_INCREMENT COMMENT '请假ID',
  `elder_id`      bigint       NOT NULL COMMENT '老人ID',
  `elder_name`    varchar(50)  DEFAULT NULL COMMENT '老人姓名',
  `tenant_id`     bigint       NOT NULL DEFAULT 1 COMMENT '机构ID',
  `leave_type`    char(1)      DEFAULT '0' COMMENT '类型（0请假 1外出就医 2回家居住）',
  `start_time`    datetime     NOT NULL COMMENT '开始时间',
  `end_time`      datetime     NOT NULL COMMENT '预计返回时间',
  `reason`        varchar(500) DEFAULT NULL COMMENT '请假事由',
  `contact_phone` varchar(20)  DEFAULT NULL COMMENT '外出联系人电话',
  `status`        char(1)      DEFAULT '0' COMMENT '状态（0待审批 1已通过 2已驳回 3已销假 4已撤销）',
  `audit_opinion` varchar(500) DEFAULT NULL COMMENT '审批意见',
  `audit_by`      varchar(64)  DEFAULT NULL COMMENT '审批人',
  `audit_time`    datetime     DEFAULT NULL COMMENT '审批时间',
  `back_time`     datetime     DEFAULT NULL COMMENT '实际返回时间',
  `del_flag`      char(1)      DEFAULT '0' COMMENT '删除标志',
  `create_by`     varchar(64)  DEFAULT NULL,
  `create_time`   datetime     DEFAULT NULL,
  `update_by`     varchar(64)  DEFAULT NULL,
  `update_time`   datetime     DEFAULT NULL,
  PRIMARY KEY (`leave_id`),
  KEY `idx_leave_elder` (`elder_id`),
  KEY `idx_leave_tenant` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='请假外出申请';

-- 2. 用药提醒
CREATE TABLE IF NOT EXISTS `biz_medication` (
  `medication_id` bigint        NOT NULL AUTO_INCREMENT COMMENT '用药ID',
  `elder_id`      bigint        NOT NULL COMMENT '老人ID',
  `elder_name`    varchar(50)   DEFAULT NULL COMMENT '老人姓名',
  `tenant_id`     bigint        NOT NULL DEFAULT 1 COMMENT '机构ID',
  `medicine_name` varchar(100)  NOT NULL COMMENT '药品名称',
  `dosage`        varchar(50)   DEFAULT NULL COMMENT '每次用量',
  `frequency`     varchar(50)   DEFAULT NULL COMMENT '服用频次',
  `time_points`   varchar(100)  DEFAULT NULL COMMENT '服用时间点（逗号分隔）',
  `start_date`    date          DEFAULT NULL COMMENT '开始日期',
  `end_date`      date          DEFAULT NULL COMMENT '结束日期',
  `status`        char(1)       DEFAULT '0' COMMENT '状态（0进行中 1已停用）',
  `remark`        varchar(500)  DEFAULT NULL COMMENT '用药说明',
  `del_flag`      char(1)       DEFAULT '0' COMMENT '删除标志',
  `create_by`     varchar(64)   DEFAULT NULL,
  `create_time`   datetime      DEFAULT NULL,
  `update_by`     varchar(64)   DEFAULT NULL,
  `update_time`   datetime      DEFAULT NULL,
  PRIMARY KEY (`medication_id`),
  KEY `idx_med_elder` (`elder_id`),
  KEY `idx_med_tenant` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用药提醒';

-- 3. 护理日志
CREATE TABLE IF NOT EXISTS `biz_care_log` (
  `log_id`      bigint        NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `elder_id`    bigint        NOT NULL COMMENT '老人ID',
  `elder_name`  varchar(50)   DEFAULT NULL COMMENT '老人姓名',
  `tenant_id`   bigint        NOT NULL DEFAULT 1 COMMENT '机构ID',
  `log_date`    date          NOT NULL COMMENT '日志日期',
  `care_type`   char(1)       DEFAULT '0' COMMENT '类型（0日常照护 1饮食 2清洁 3康复 4心理关怀）',
  `content`     varchar(1000) DEFAULT NULL COMMENT '护理内容',
  `nurse_name`  varchar(50)   DEFAULT NULL COMMENT '护理员',
  `del_flag`    char(1)       DEFAULT '0' COMMENT '删除标志',
  `create_by`   varchar(64)   DEFAULT NULL,
  `create_time` datetime      DEFAULT NULL,
  `update_by`   varchar(64)   DEFAULT NULL,
  `update_time` datetime      DEFAULT NULL,
  PRIMARY KEY (`log_id`),
  KEY `idx_care_elder` (`elder_id`),
  KEY `idx_care_tenant` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='护理日志';

-- 4. 家属投诉建议
CREATE TABLE IF NOT EXISTS `biz_feedback` (
  `feedback_id`  bigint        NOT NULL AUTO_INCREMENT COMMENT '反馈ID',
  `elder_id`     bigint        DEFAULT NULL COMMENT '老人ID',
  `elder_name`   varchar(50)   DEFAULT NULL COMMENT '老人姓名',
  `tenant_id`    bigint        NOT NULL DEFAULT 1 COMMENT '机构ID',
  `family_phone` varchar(20)   DEFAULT NULL COMMENT '家属手机号',
  `family_name`  varchar(50)   DEFAULT NULL COMMENT '家属姓名',
  `feedback_type` char(1)      DEFAULT '1' COMMENT '类型（0投诉 1建议 2表扬）',
  `content`      varchar(1000) NOT NULL COMMENT '反馈内容',
  `status`       char(1)       DEFAULT '0' COMMENT '状态（0待处理 1处理中 2已回复）',
  `reply`        varchar(1000) DEFAULT NULL COMMENT '回复内容',
  `reply_by`     varchar(64)   DEFAULT NULL COMMENT '回复人',
  `reply_time`   datetime      DEFAULT NULL COMMENT '回复时间',
  `del_flag`     char(1)       DEFAULT '0' COMMENT '删除标志',
  `create_by`    varchar(64)   DEFAULT NULL,
  `create_time`  datetime      DEFAULT NULL,
  `update_by`    varchar(64)   DEFAULT NULL,
  `update_time`  datetime      DEFAULT NULL,
  PRIMARY KEY (`feedback_id`),
  KEY `idx_feedback_tenant` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家属投诉建议';

-- 5. 发票申请
CREATE TABLE IF NOT EXISTS `biz_invoice` (
  `invoice_id`   bigint        NOT NULL AUTO_INCREMENT COMMENT '发票ID',
  `bill_id`      bigint        DEFAULT NULL COMMENT '账单ID',
  `bill_no`      varchar(40)   DEFAULT NULL COMMENT '账单编号',
  `elder_id`     bigint        DEFAULT NULL COMMENT '老人ID',
  `elder_name`   varchar(50)   DEFAULT NULL COMMENT '老人姓名',
  `tenant_id`    bigint        NOT NULL DEFAULT 1 COMMENT '机构ID',
  `title`        varchar(100)  NOT NULL COMMENT '发票抬头',
  `tax_no`       varchar(50)   DEFAULT NULL COMMENT '纳税人识别号',
  `amount`       decimal(10,2) DEFAULT 0.00 COMMENT '开票金额',
  `email`        varchar(100)  DEFAULT NULL COMMENT '接收邮箱',
  `status`       char(1)       DEFAULT '0' COMMENT '状态（0待开票 1已开票 2已驳回）',
  `invoice_no`   varchar(50)   DEFAULT NULL COMMENT '发票号码',
  `remark`       varchar(500)  DEFAULT NULL COMMENT '备注/驳回原因',
  `del_flag`     char(1)       DEFAULT '0' COMMENT '删除标志',
  `create_by`    varchar(64)   DEFAULT NULL,
  `create_time`  datetime      DEFAULT NULL,
  `update_by`    varchar(64)   DEFAULT NULL,
  `update_time`  datetime      DEFAULT NULL,
  PRIMARY KEY (`invoice_id`),
  KEY `idx_invoice_tenant` (`tenant_id`),
  KEY `idx_invoice_bill` (`bill_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发票申请';

-- 6. 探访核销字段（幂等）
SET @col_exists := (SELECT COUNT(*) FROM information_schema.COLUMNS
                    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_visit' AND COLUMN_NAME = 'visit_code');
SET @ddl := IF(@col_exists = 0,
  'ALTER TABLE `biz_visit` ADD COLUMN `visit_code` varchar(20) DEFAULT NULL COMMENT ''探访预约码'' AFTER `status`',
  'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (SELECT COUNT(*) FROM information_schema.COLUMNS
                    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_visit' AND COLUMN_NAME = 'checkin_time');
SET @ddl := IF(@col_exists = 0,
  'ALTER TABLE `biz_visit` ADD COLUMN `checkin_time` datetime DEFAULT NULL COMMENT ''到访核销时间'' AFTER `visit_code`',
  'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 7. 菜单
DELETE FROM sys_role_menu WHERE menu_id BETWEEN 4600 AND 4652;
DELETE FROM sys_menu WHERE menu_id BETWEEN 4600 AND 4652;

INSERT INTO sys_menu VALUES(4600, '照护服务', 0, 9, 'care', null, null, '', 1, 0, 'M', '0', '0', '', 'first-aid-kit', 'admin', sysdate(), '', null, '日常照护与家属服务');
INSERT INTO sys_menu VALUES(4601, '请假外出', 4600, 1, 'leave', 'business/leave/index', null, '', 1, 0, 'C', '0', '0', 'business:leave:list', 'date', 'admin', sysdate(), '', null, '家属请假外出申请与审批');
INSERT INTO sys_menu VALUES(4602, '用药提醒', 4600, 2, 'medication', 'business/medication/index', null, '', 1, 0, 'C', '0', '0', 'business:medication:list', 'medicine-box', 'admin', sysdate(), '', null, '老人用药计划维护');
INSERT INTO sys_menu VALUES(4603, '护理日志', 4600, 3, 'careLog', 'business/careLog/index', null, '', 1, 0, 'C', '0', '0', 'business:careLog:list', 'documentation', 'admin', sysdate(), '', null, '每日护理记录');
INSERT INTO sys_menu VALUES(4604, '投诉建议', 4600, 4, 'feedback', 'business/feedback/index', null, '', 1, 0, 'C', '0', '0', 'business:feedback:list', 'message', 'admin', sysdate(), '', null, '家属反馈处理');
INSERT INTO sys_menu VALUES(4605, '发票申请', 4600, 5, 'invoice', 'business/invoice/index', null, '', 1, 0, 'C', '0', '0', 'business:invoice:list', 'money', 'admin', sysdate(), '', null, '家属开票申请处理');

INSERT INTO sys_menu VALUES(4611, '请假审批', 4601, 1, '', null, null, '', 1, 0, 'F', '0', '0', 'business:leave:audit', '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES(4612, '请假删除', 4601, 2, '', null, null, '', 1, 0, 'F', '0', '0', 'business:leave:remove', '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES(4621, '用药新增', 4602, 1, '', null, null, '', 1, 0, 'F', '0', '0', 'business:medication:add', '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES(4622, '用药修改', 4602, 2, '', null, null, '', 1, 0, 'F', '0', '0', 'business:medication:edit', '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES(4623, '用药删除', 4602, 3, '', null, null, '', 1, 0, 'F', '0', '0', 'business:medication:remove', '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES(4631, '日志新增', 4603, 1, '', null, null, '', 1, 0, 'F', '0', '0', 'business:careLog:add', '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES(4632, '日志修改', 4603, 2, '', null, null, '', 1, 0, 'F', '0', '0', 'business:careLog:edit', '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES(4633, '日志删除', 4603, 3, '', null, null, '', 1, 0, 'F', '0', '0', 'business:careLog:remove', '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES(4641, '反馈回复', 4604, 1, '', null, null, '', 1, 0, 'F', '0', '0', 'business:feedback:reply', '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES(4642, '反馈删除', 4604, 2, '', null, null, '', 1, 0, 'F', '0', '0', 'business:feedback:remove', '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES(4651, '发票处理', 4605, 1, '', null, null, '', 1, 0, 'F', '0', '0', 'business:invoice:handle', '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES(4652, '发票删除', 4605, 2, '', null, null, '', 1, 0, 'F', '0', '0', 'business:invoice:remove', '#', 'admin', sysdate(), '', null, '');

-- 探访核销按钮
DELETE FROM sys_role_menu WHERE menu_id = 4005;
DELETE FROM sys_menu WHERE menu_id = 4005;
INSERT INTO sys_menu VALUES(4005, '来访核销', 4000, 5, '', null, null, '', 1, 0, 'F', '0', '0', 'business:visit:checkin', '#', 'admin', sysdate(), '', null, '');

-- 授权：超级管理员 role 1 + 租户管理员 role 100
INSERT IGNORE INTO sys_role_menu(role_id, menu_id) VALUES
  (1, 4600), (1, 4601), (1, 4602), (1, 4603), (1, 4604), (1, 4605),
  (1, 4611), (1, 4612), (1, 4621), (1, 4622), (1, 4623),
  (1, 4631), (1, 4632), (1, 4633), (1, 4641), (1, 4642), (1, 4651), (1, 4652), (1, 4005),
  (100, 4600), (100, 4601), (100, 4602), (100, 4603), (100, 4604), (100, 4605),
  (100, 4611), (100, 4612), (100, 4621), (100, 4622), (100, 4623),
  (100, 4631), (100, 4632), (100, 4633), (100, 4641), (100, 4642), (100, 4651), (100, 4652), (100, 4005);

-- 8. 演示数据（仅当无记录时插入）
INSERT INTO biz_medication (elder_id, elder_name, tenant_id, medicine_name, dosage, frequency, time_points,
  start_date, end_date, status, remark, del_flag, create_by, create_time)
SELECT 1, '张伟', 1, '苯磺酸氨氯地平片', '5mg（1片）', '每日1次', '08:00',
  date_sub(curdate(), interval 10 day), date_add(curdate(), interval 20 day), '0', '早餐后服用，监测血压', '0', 'admin', sysdate()
FROM dual WHERE NOT EXISTS (SELECT 1 FROM biz_medication WHERE elder_id = 1 AND del_flag = '0');

INSERT INTO biz_medication (elder_id, elder_name, tenant_id, medicine_name, dosage, frequency, time_points,
  start_date, end_date, status, remark, del_flag, create_by, create_time)
SELECT 2, '李秀英', 1, '二甲双胍缓释片', '0.5g（1片）', '每日2次', '08:00,18:00',
  date_sub(curdate(), interval 5 day), date_add(curdate(), interval 25 day), '0', '餐后服用', '0', 'admin', sysdate()
FROM dual WHERE NOT EXISTS (SELECT 1 FROM biz_medication WHERE elder_id = 2 AND del_flag = '0');

INSERT INTO biz_care_log (elder_id, elder_name, tenant_id, log_date, care_type, content, nurse_name,
  del_flag, create_by, create_time)
SELECT 1, '张伟', 1, curdate(), '0', '上午协助老人洗漱、整理床铺，精神状态良好，血压 132/82mmHg。', '王护理', '0', 'admin', sysdate()
FROM dual WHERE NOT EXISTS (SELECT 1 FROM biz_care_log WHERE elder_id = 1 AND log_date = curdate() AND del_flag = '0');

INSERT INTO biz_care_log (elder_id, elder_name, tenant_id, log_date, care_type, content, nurse_name,
  del_flag, create_by, create_time)
SELECT 1, '张伟', 1, date_sub(curdate(), interval 1 day), '3', '下午进行下肢康复训练 30 分钟，步行较昨日平稳，无不适。', '李护理', '0', 'admin', sysdate()
FROM dual WHERE NOT EXISTS (SELECT 1 FROM biz_care_log WHERE elder_id = 1 AND log_date = date_sub(curdate(), interval 1 day) AND del_flag = '0');

INSERT INTO biz_care_log (elder_id, elder_name, tenant_id, log_date, care_type, content, nurse_name,
  del_flag, create_by, create_time)
SELECT 2, '李秀英', 1, curdate(), '1', '午餐进食米饭半碗、青菜与瘦肉，食欲一般，已提醒少量多餐。', '王护理', '0', 'admin', sysdate()
FROM dual WHERE NOT EXISTS (SELECT 1 FROM biz_care_log WHERE elder_id = 2 AND log_date = curdate() AND del_flag = '0');
