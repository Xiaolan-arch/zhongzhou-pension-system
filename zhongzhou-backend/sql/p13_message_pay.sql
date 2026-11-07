-- =====================================================
-- P13 组 D：微信订阅消息 + 家属端在线缴费
-- =====================================================

-- 1. 家属绑定关系增加微信 openid（用于订阅消息推送）
SET @col_exists := (SELECT COUNT(*) FROM information_schema.COLUMNS
                    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_family' AND COLUMN_NAME = 'openid');
SET @ddl := IF(@col_exists = 0,
  'ALTER TABLE `biz_family` ADD COLUMN `openid` varchar(128) DEFAULT NULL COMMENT ''微信 openid'' AFTER `phone`',
  'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 2. 微信订阅消息发送记录
CREATE TABLE IF NOT EXISTS `biz_message_log` (
  `log_id`      bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `tenant_id`   bigint(20)   NOT NULL                COMMENT '机构ID',
  `family_id`   bigint(20)   DEFAULT NULL            COMMENT '家属ID',
  `elder_id`    bigint(20)   DEFAULT NULL            COMMENT '老人ID',
  `elder_name`  varchar(64)  DEFAULT ''              COMMENT '老人姓名',
  `phone`       varchar(20)  DEFAULT ''              COMMENT '家属手机号',
  `openid`      varchar(128) DEFAULT ''              COMMENT '微信 openid',
  `msg_type`    char(1)      NOT NULL DEFAULT '1'    COMMENT '消息类型（1账单到期 2逾期提醒 3缴费成功）',
  `title`       varchar(200) DEFAULT ''              COMMENT '消息标题',
  `content`     varchar(500) DEFAULT ''              COMMENT '消息内容',
  `send_status` char(1)      NOT NULL DEFAULT '0'    COMMENT '发送状态（0成功 1失败 2跳过）',
  `error_msg`   varchar(500) DEFAULT ''              COMMENT '错误信息/说明',
  `create_time` datetime     DEFAULT NULL            COMMENT '创建时间',
  PRIMARY KEY (`log_id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_elder_id` (`elder_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='微信订阅消息发送记录';

-- 3. 菜单：消息通知页面 + 账单提醒按钮
DELETE FROM sys_role_menu WHERE menu_id IN (4282, 4407);
DELETE FROM sys_menu WHERE menu_id IN (4282, 4407);
INSERT INTO sys_menu VALUES(4282, '消息通知', 0, 8, 'message', 'business/message/index', null, '', 1, 0, 'C', '0', '0', 'business:message:list', 'message', 'admin', sysdate(), '', null, '微信订阅消息发送记录');
INSERT INTO sys_menu VALUES(4407, '账单提醒', 4400, 7, '', null, null, '', 1, 0, 'F', '0', '0', 'business:bill:notify', '#', 'admin', sysdate(), '', null, '向家属发送账单提醒');

INSERT IGNORE INTO sys_role_menu(role_id, menu_id) VALUES
  (1, 4282), (1, 4407),
  (100, 4282), (100, 4407);

-- 4. 家属端在线缴费演示账单（张伟 2026-12 未缴，便于用 13822220001 演示缴费）
INSERT INTO biz_bill(bill_no, elder_id, elder_name, bill_month, period_start, period_end,
                     amount, prepaid, paid_amount, pay_status, bill_type, tenant_id, del_flag,
                     create_by, create_time, remark)
SELECT 'ZD20261201003', 1, '张伟', '2026-12', '2026-12-01', '2026-12-31',
       3099.00, 0.00, 0.00, '0', '1', 1, '0', 'admin', sysdate(), '家属端在线缴费演示账单'
FROM dual
WHERE NOT EXISTS (SELECT 1 FROM biz_bill WHERE bill_no = 'ZD20261201003');
