-- ============================================================
-- P5 Phase C 工作台个性化布局
-- 执行库：zhongzhou
-- ============================================================
USE zhongzhou;

CREATE TABLE IF NOT EXISTS biz_user_dashboard_layout (
  layout_id    bigint(20)  not null auto_increment comment '布局ID',
  user_id      bigint(20)  not null                comment '用户ID',
  tenant_id    bigint(20)  not null default 1      comment '机构ID',
  layout_json  text                                comment '布局配置JSON（区块顺序与显隐）',
  create_time  datetime                            comment '创建时间',
  update_time  datetime                            comment '更新时间',
  primary key (layout_id),
  unique key uk_user (user_id)
) engine=innodb auto_increment=1 comment='工作台个性化布局';
