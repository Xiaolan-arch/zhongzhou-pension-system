-- ============================================================
-- P15 家属端服务评价
-- 家属对已完成的护理服务进行满意度评价，同一订单同一家属仅可评价一次
-- ============================================================

CREATE TABLE IF NOT EXISTS `biz_service_evaluation` (
  `eval_id`      bigint       NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `tenant_id`    bigint       NOT NULL COMMENT '机构ID',
  `order_id`     bigint       NOT NULL COMMENT '服务订单ID',
  `order_no`     varchar(32)  DEFAULT NULL COMMENT '服务单号',
  `elder_id`     bigint       NOT NULL COMMENT '老人ID',
  `elder_name`   varchar(50)  DEFAULT NULL COMMENT '老人姓名',
  `family_id`    bigint       NOT NULL COMMENT '家属ID',
  `family_name`  varchar(50)  DEFAULT NULL COMMENT '家属姓名',
  `phone`        varchar(20)  DEFAULT NULL COMMENT '家属手机号',
  `rating`       tinyint      NOT NULL COMMENT '满意度（1需改进 2一般 3满意 4非常满意）',
  `content`      varchar(500) DEFAULT NULL COMMENT '评价内容',
  `create_by`    varchar(64)  DEFAULT '' COMMENT '创建者',
  `create_time`  datetime     DEFAULT NULL COMMENT '创建时间',
  `update_by`    varchar(64)  DEFAULT '' COMMENT '更新者',
  `update_time`  datetime     DEFAULT NULL COMMENT '更新时间',
  `del_flag`     char(1)      DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
  PRIMARY KEY (`eval_id`),
  UNIQUE KEY `uk_order_family` (`order_id`, `family_id`),
  KEY `idx_tenant_elder` (`tenant_id`, `elder_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务评价表';
