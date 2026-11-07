SET NAMES utf8mb4;

-- ============================================================
-- P8 业务关键操作审计日志表
-- 说明：记录收款、退款、入住、退住等关键业务操作，
--       用于追溯「谁在什么时候对哪张单据做了什么变更」
-- ============================================================

DROP TABLE IF EXISTS biz_audit_log;

CREATE TABLE biz_audit_log (
  log_id            bigint(20)      NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  biz_type          varchar(32)     NOT NULL                COMMENT '业务类型（BILL_PAY/BILL_REFUND/CHECKIN/CHECKOUT）',
  biz_id            bigint(20)      NOT NULL DEFAULT 0      COMMENT '业务主键',
  action            varchar(64)     DEFAULT ''              COMMENT '动作描述',
  before_json       text            DEFAULT NULL            COMMENT '变更前快照（可为空）',
  after_json        text            DEFAULT NULL            COMMENT '变更后快照（可为空）',
  operator_id       bigint(20)      DEFAULT NULL            COMMENT '操作人ID',
  operator_name     varchar(64)     DEFAULT ''              COMMENT '操作人姓名',
  tenant_id         bigint(20)      NOT NULL DEFAULT 1      COMMENT '机构ID',
  create_time       datetime        DEFAULT NULL            COMMENT '创建时间',
  PRIMARY KEY (log_id),
  KEY idx_biz (biz_type, biz_id),
  KEY idx_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='业务审计日志表';
