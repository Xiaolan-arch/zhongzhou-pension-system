-- =====================================================
-- P6 财务收款闭环：收款记录表 + 字典 + 菜单权限
-- =====================================================

-- 1. 收款记录表
CREATE TABLE IF NOT EXISTS `biz_payment_record` (
  `record_id`      bigint(20)    NOT NULL AUTO_INCREMENT COMMENT '收款记录ID',
  `record_no`      varchar(32)   NOT NULL                COMMENT '收款单号（PAYyyyyMMddHHmmssSSS）',
  `bill_id`        bigint(20)    NOT NULL                COMMENT '账单ID',
  `bill_no`        varchar(32)   DEFAULT ''              COMMENT '账单编号（冗余，便于列表展示）',
  `elder_id`       bigint(20)    NOT NULL                COMMENT '老人ID',
  `elder_name`     varchar(64)   DEFAULT ''              COMMENT '老人姓名（冗余）',
  `pay_amount`     decimal(12,2) NOT NULL DEFAULT 0.00   COMMENT '本次收款金额',
  `pay_method`     varchar(20)   DEFAULT ''              COMMENT '支付方式（biz_pay_method）',
  `pay_voucher`    varchar(500)  DEFAULT ''              COMMENT '支付凭证URL',
  `remark`         varchar(500)  DEFAULT ''              COMMENT '备注',
  `operator_id`     bigint(20)    DEFAULT NULL            COMMENT '操作人ID',
  `operator_name`  varchar(64)   DEFAULT ''              COMMENT '操作人姓名',
  `tenant_id`      bigint(20)    NOT NULL                COMMENT '机构ID',
  `del_flag`       char(1)       DEFAULT '0'             COMMENT '删除标志（0存在 2删除）',
  `create_by`      varchar(64)   DEFAULT ''              COMMENT '创建者',
  `create_time`    datetime      DEFAULT NULL            COMMENT '创建时间',
  PRIMARY KEY (`record_id`),
  UNIQUE KEY `uk_record_no` (`record_no`),
  KEY `idx_bill_id` (`bill_id`),
  KEY `idx_elder_id` (`elder_id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_pay_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='收款记录表';

-- 2. 字典：账单支付状态 新增"部分已收"
SET @next_dict_code := (SELECT MAX(dict_code)+1 FROM (SELECT dict_code FROM sys_dict_data) t);
INSERT INTO sys_dict_data
  (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
VALUES
  (@next_dict_code, 4, '部分已收', '3', 'biz_bill_status', '', 'warning', 'N', '0', 'admin', sysdate(), '部分已收');

-- 3. 字典：支付方式 新增"其他"（可选）
SET @next_dict_code := (SELECT MAX(dict_code)+1 FROM (SELECT dict_code FROM sys_dict_data) t);
INSERT INTO sys_dict_data
  (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
VALUES
  (@next_dict_code, 5, '其他', '4', 'biz_pay_method', '', '', 'N', '0', 'admin', sysdate(), '其他');

-- 4. 菜单权限（按钮）：账单管理 4400 下已有 export=4401 / pay=4402 / generate=4403，新增：
INSERT INTO sys_menu
  (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES
  (4404, '收款记录', 4400, 4, '',  '', 1, 0, 'F', '0', '0', 'business:bill:record',  '#', 'admin', sysdate(), '', NULL, '账单收款记录查看'),
  (4405, '发票打印', 4400, 5, '',  '', 1, 0, 'F', '0', '0', 'business:bill:invoice', '#', 'admin', sysdate(), '', NULL, '账单发票打印');

-- 5. 给超级管理员（role_id=1）和中州养老院（role_id=100）授权新按钮
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
  (1, 4404), (1, 4405),
  (100, 4404), (100, 4405);

-- 6. 种子收款记录（演示用：让现有部分已支付的账单有历史记录）
-- 注：执行前先查一下 biz_bill 里 paid_amount > 0 的账单
-- INSERT INTO biz_payment_record (...) VALUES (...);
