-- =====================================================
-- P11 押金管理 + 逾期滞纳金
-- =====================================================

-- 1. 押金记录表（收押金 / 退押金流水）
CREATE TABLE IF NOT EXISTS `biz_deposit` (
  `deposit_id`     bigint(20)    NOT NULL AUTO_INCREMENT COMMENT '押金记录ID',
  `tenant_id`      bigint(20)    NOT NULL                COMMENT '机构ID',
  `elder_id`       bigint(20)    NOT NULL                COMMENT '老人ID',
  `elder_name`     varchar(64)   DEFAULT ''              COMMENT '老人姓名（冗余）',
  `deposit_type`   char(1)       NOT NULL DEFAULT '1'    COMMENT '类型（1收押金 2退押金）',
  `amount`         decimal(12,2) NOT NULL DEFAULT 0.00   COMMENT '金额',
  `pay_method`     varchar(20)   DEFAULT ''              COMMENT '支付方式（biz_pay_method）',
  `remark`         varchar(500)  DEFAULT ''              COMMENT '备注',
  `operator_id`    bigint(20)    DEFAULT NULL            COMMENT '操作人ID',
  `operator_name`  varchar(64)   DEFAULT ''              COMMENT '操作人姓名',
  `del_flag`       char(1)       DEFAULT '0'             COMMENT '删除标志（0存在 2删除）',
  `create_by`      varchar(64)   DEFAULT ''              COMMENT '创建者',
  `create_time`    datetime      DEFAULT NULL            COMMENT '创建时间',
  PRIMARY KEY (`deposit_id`),
  KEY `idx_elder_id` (`elder_id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='押金记录表';

-- 2. 账单增加滞纳金字段（幂等）
SET @col_exists := (SELECT COUNT(*) FROM information_schema.COLUMNS
                    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_bill' AND COLUMN_NAME = 'penalty_amount');
SET @ddl := IF(@col_exists = 0,
  'ALTER TABLE `biz_bill` ADD COLUMN `penalty_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT ''滞纳金'' AFTER `paid_amount`',
  'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (SELECT COUNT(*) FROM information_schema.COLUMNS
                    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_bill' AND COLUMN_NAME = 'penalty_time');
SET @ddl := IF(@col_exists = 0,
  'ALTER TABLE `biz_bill` ADD COLUMN `penalty_time` datetime DEFAULT NULL COMMENT ''滞纳金最近计算时间'' AFTER `penalty_amount`',
  'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 3. 字典：押金类型
DELETE FROM sys_dict_data WHERE dict_type = 'biz_deposit_type';
SET @next_dict_code := (SELECT MAX(dict_code)+1 FROM (SELECT dict_code FROM sys_dict_data) t);
INSERT INTO sys_dict_data
  (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
VALUES
  (@next_dict_code,     1, '收押金', '1', 'biz_deposit_type', '', 'success', 'Y', '0', 'admin', sysdate(), '收押金'),
  (@next_dict_code + 1, 2, '退押金', '2', 'biz_deposit_type', '', 'warning', 'N', '0', 'admin', sysdate(), '退押金');

-- 4. 菜单：押金管理（一级菜单）+ 收/退按钮 + 滞纳金重算按钮
DELETE FROM sys_role_menu WHERE menu_id IN (4271, 4272, 4273, 4406);
DELETE FROM sys_menu WHERE menu_id IN (4271, 4272, 4273, 4406);

INSERT INTO sys_menu VALUES(4271, '押金管理', 0, 7, 'deposit', 'business/deposit/index', null, '', 1, 0, 'C', '0', '0', 'business:deposit:list', 'money', 'admin', sysdate(), '', null, '老人押金收取与退还');
INSERT INTO sys_menu VALUES(4272, '收押金', 4271, 1, '', null, null, '', 1, 0, 'F', '0', '0', 'business:deposit:add', '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES(4273, '退押金', 4271, 2, '', null, null, '', 1, 0, 'F', '0', '0', 'business:deposit:refund', '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES(4406, '滞纳金重算', 4400, 6, '', null, null, '', 1, 0, 'F', '0', '0', 'business:bill:penalty', '#', 'admin', sysdate(), '', null, '逾期账单滞纳金重新计算');

-- 5. 授权：超级管理员 role 1 + 租户管理员 role 100
INSERT IGNORE INTO sys_role_menu(role_id, menu_id) VALUES
  (1, 4271), (1, 4272), (1, 4273), (1, 4406),
  (100, 4271), (100, 4272), (100, 4273), (100, 4406);

-- 6. 演示押金数据（张伟 elder_id=1 / 钱国华 elder_id=100），已有记录则跳过
INSERT INTO biz_deposit (tenant_id, elder_id, elder_name, deposit_type, amount, pay_method, remark, operator_name, del_flag, create_by, create_time)
SELECT e.tenant_id, e.elder_id, e.elder_name, '1', 10000.00, '1', '入住押金', 'admin', '0', 'admin', sysdate()
FROM biz_elder e
WHERE e.elder_id IN (1, 100) AND e.del_flag = '0'
  AND NOT EXISTS (SELECT 1 FROM biz_deposit d WHERE d.elder_id = e.elder_id AND d.deposit_type = '1');
