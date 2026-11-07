-- ============================================================
-- P9 服务订单 → 护理任务 自动派发
-- 说明：订单创建时自动生成护理任务；定时任务扫描当天逾期任务
-- ============================================================

CREATE TABLE IF NOT EXISTS biz_service_task (
  task_id           bigint(20)      NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  order_id          bigint(20)      NOT NULL                COMMENT '关联服务订单ID',
  order_no          varchar(40)     DEFAULT ''              COMMENT '关联订单号',
  elder_id          bigint(20)      DEFAULT NULL            COMMENT '老人ID',
  elder_name        varchar(50)     DEFAULT ''              COMMENT '老人姓名',
  item_id           bigint(20)      DEFAULT NULL            COMMENT '服务项目ID',
  item_name         varchar(50)     DEFAULT ''              COMMENT '项目名称',
  nurse_id          bigint(20)      DEFAULT NULL            COMMENT '护理员ID',
  nurse_name        varchar(50)     DEFAULT ''              COMMENT '护理员姓名',
  service_date      date            DEFAULT NULL            COMMENT '服务日期（订单服务时间所属日期）',
  plan_time         datetime        DEFAULT NULL            COMMENT '计划执行时间（取自订单服务时间）',
  task_status       char(1)         DEFAULT '0'             COMMENT '任务状态（0待执行 1已执行 2已完成 3已取消 4已逾期）',
  remark            varchar(500)    DEFAULT ''              COMMENT '备注',
  tenant_id         bigint(20)      NOT NULL DEFAULT 1      COMMENT '机构ID',
  del_flag          char(1)         DEFAULT '0'             COMMENT '删除标志（0存在 2删除）',
  create_by         varchar(64)     DEFAULT ''              COMMENT '创建者',
  create_time       datetime        DEFAULT NULL            COMMENT '创建时间',
  update_by         varchar(64)     DEFAULT ''              COMMENT '更新者',
  update_time       datetime        DEFAULT NULL            COMMENT '更新时间',
  PRIMARY KEY (task_id),
  UNIQUE KEY uk_task_order (tenant_id, order_id),
  KEY idx_task_date_status (service_date, task_status),
  KEY idx_task_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='护理任务表';
