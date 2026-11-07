-- =====================================================
-- P7 服务任务签到打卡
-- =====================================================

-- 1. biz_service_order 加签到/完成字段
ALTER TABLE biz_service_order
  ADD COLUMN checkin_time    datetime      DEFAULT NULL   COMMENT '签到时间'      AFTER status,
  ADD COLUMN checkin_operator varchar(64)  DEFAULT ''     COMMENT '签到操作人'    AFTER checkin_time,
  ADD COLUMN finish_time     datetime      DEFAULT NULL   COMMENT '完成时间'      AFTER checkin_operator,
  ADD COLUMN finish_operator varchar(64)  DEFAULT ''     COMMENT '完成操作人'    AFTER finish_time,
  ADD COLUMN service_remark  varchar(500)  DEFAULT ''     COMMENT '服务记录/备注'  AFTER finish_operator,
  ADD INDEX idx_service_nurse_status (nurse_id, status);

-- 2. 字典：服务订单状态（确认 biz_service_order_status 完整）
-- 现有：0 待执行 1 已执行 2 已完成 3 已取消 — 无新增
