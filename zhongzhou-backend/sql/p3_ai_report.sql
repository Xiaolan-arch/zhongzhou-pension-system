-- ============================================================
-- P3 AI 健康报告：建表 + 字典 + 菜单 + 双租户种子数据
-- 说明：报告表带 tenant_id，历史版本按 generation 顺序保留
-- ============================================================

DROP TABLE IF EXISTS biz_health_report;

-- ----------------------------
-- 1. AI 健康报告
-- ----------------------------
CREATE TABLE biz_health_report (
  report_id         bigint(20)      NOT NULL AUTO_INCREMENT COMMENT '报告ID',
  elder_id          bigint(20)      NOT NULL                COMMENT '老人ID',
  elder_name        varchar(50)     NOT NULL                COMMENT '老人姓名',
  report_type       char(1)         DEFAULT '0'             COMMENT '报告类型（biz_report_type）',
  report_title      varchar(200)    DEFAULT ''              COMMENT '报告标题',
  period_start      date            DEFAULT NULL            COMMENT '统计周期开始',
  period_end        date            DEFAULT NULL            COMMENT '统计周期结束',
  score             int(3)          DEFAULT 0               COMMENT '健康评分（0-100）',
  risk_level        char(1)         DEFAULT '0'             COMMENT '风险等级（0良好 1关注 2预警）',
  summary           varchar(500)    DEFAULT ''              COMMENT '摘要',
  content           longtext        COMMENT '报告正文（Markdown）',
  model_source      varchar(20)     DEFAULT 'mock'          COMMENT '模型来源（mock/qianfan）',
  model_name        varchar(100)    DEFAULT ''              COMMENT '模型名称',
  status            char(1)         DEFAULT '1'             COMMENT '状态（0生成中 1已生成 2生成失败）',
  error_msg         varchar(500)    DEFAULT ''              COMMENT '失败原因',
  archived_url      varchar(500)    DEFAULT ''              COMMENT '归档文件地址',
  tenant_id         bigint(20)      NOT NULL DEFAULT 1      COMMENT '机构ID',
  del_flag          char(1)         DEFAULT '0'             COMMENT '删除标志（0存在 2删除）',
  create_by         varchar(64)     DEFAULT ''              COMMENT '创建者',
  create_time       datetime        DEFAULT NULL            COMMENT '创建时间',
  update_by         varchar(64)     DEFAULT ''              COMMENT '更新者',
  update_time       datetime        DEFAULT NULL            COMMENT '更新时间',
  remark            varchar(500)    DEFAULT ''              COMMENT '备注',
  PRIMARY KEY (report_id),
  KEY idx_tenant_elder (tenant_id, elder_id),
  KEY idx_tenant_type (tenant_id, report_type)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='AI 健康报告';

-- ----------------------------
-- 2. 字典
-- ----------------------------
DELETE FROM sys_dict_data WHERE dict_type LIKE 'biz_report_%';
DELETE FROM sys_dict_type WHERE dict_type LIKE 'biz_report_%';

INSERT INTO sys_dict_type VALUES(1101, '报告类型', 'biz_report_type', '0', 'admin', sysdate(), '', null, 'AI 健康报告类型');
INSERT INTO sys_dict_type VALUES(1102, '风险等级', 'biz_report_risk', '0', 'admin', sysdate(), '', null, '报告风险等级');
INSERT INTO sys_dict_type VALUES(1103, '报告状态', 'biz_report_status', '0', 'admin', sysdate(), '', null, '报告生成状态');
INSERT INTO sys_dict_type VALUES(1104, '模型来源', 'biz_report_model', '0', 'admin', sysdate(), '', null, '报告生成模型');

INSERT INTO sys_dict_data VALUES(1101, 1, '综合照护报告', '0', 'biz_report_type', '', 'primary', 'Y', '0', 'admin', sysdate(), '', null, '综合照护报告');
INSERT INTO sys_dict_data VALUES(1102, 2, '健康评估报告', '1', 'biz_report_type', '', 'success', 'N', '0', 'admin', sysdate(), '', null, '健康评估报告');
INSERT INTO sys_dict_data VALUES(1103, 3, '月度总结报告', '2', 'biz_report_type', '', 'info', 'N', '0', 'admin', sysdate(), '', null, '月度总结报告');

INSERT INTO sys_dict_data VALUES(1111, 1, '良好', '0', 'biz_report_risk', '', 'success', 'Y', '0', 'admin', sysdate(), '', null, '良好');
INSERT INTO sys_dict_data VALUES(1112, 2, '关注', '1', 'biz_report_risk', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, '关注');
INSERT INTO sys_dict_data VALUES(1113, 3, '预警', '2', 'biz_report_risk', '', 'danger', 'N', '0', 'admin', sysdate(), '', null, '预警');

INSERT INTO sys_dict_data VALUES(1121, 1, '生成中', '0', 'biz_report_status', '', 'info', 'Y', '0', 'admin', sysdate(), '', null, '生成中');
INSERT INTO sys_dict_data VALUES(1122, 2, '已生成', '1', 'biz_report_status', '', 'success', 'N', '0', 'admin', sysdate(), '', null, '已生成');
INSERT INTO sys_dict_data VALUES(1123, 3, '生成失败', '2', 'biz_report_status', '', 'danger', 'N', '0', 'admin', sysdate(), '', null, '生成失败');

INSERT INTO sys_dict_data VALUES(1131, 1, '本地模拟', 'mock', 'biz_report_model', '', 'info', 'Y', '0', 'admin', sysdate(), '', null, '本地模拟');
INSERT INTO sys_dict_data VALUES(1132, 2, '千帆大模型', 'qianfan', 'biz_report_model', '', 'primary', 'N', '0', 'admin', sysdate(), '', null, '千帆大模型');

-- ----------------------------
-- 3. 菜单（4500+，避免与 P1/P2 冲突）
-- ----------------------------
DELETE FROM sys_role_menu WHERE menu_id BETWEEN 4500 AND 4599;
DELETE FROM sys_menu WHERE menu_id BETWEEN 4500 AND 4599;

INSERT INTO sys_menu VALUES(4500, 'AI健康报告', 0, 6, 'ai-report', 'business/report/index', null, '', 1, 0, 'C', '0', '0', 'business:report:list', 'chart', 'admin', sysdate(), '', null, 'AI 健康报告');
INSERT INTO sys_menu VALUES(4501, '生成报告', 4500, 1, '', null, null, '', 1, 0, 'F', '0', '0', 'business:report:generate', '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES(4502, '删除报告', 4500, 2, '', null, null, '', 1, 0, 'F', '0', '0', 'business:report:remove', '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES(4503, '导出报告', 4500, 3, '', null, null, '', 1, 0, 'F', '0', '0', 'business:report:export', '#', 'admin', sysdate(), '', null, '');

-- 角色 100（租户管理员）授权（幂等）
INSERT IGNORE INTO sys_role_menu(role_id, menu_id)
SELECT 100, menu_id FROM sys_menu WHERE menu_id BETWEEN 4500 AND 4599;

-- ----------------------------
-- 4. 双租户演示数据（历史版本示例）
-- ----------------------------
INSERT INTO biz_health_report
(report_id, elder_id, elder_name, report_type, report_title, period_start, period_end, score, risk_level, summary, content, model_source, model_name, status, tenant_id, del_flag, create_by, create_time)
VALUES
(1, 1, '张伟', '0', '张伟 · 综合照护报告', date_sub(curdate(), interval 30 day), curdate(), 86, '0',
 '老人近期精神状态平稳，护理服务完成率较高，建议维持现有照护方案并加强下肢康复训练。',
 '## 一、基本信息\n- 老人姓名：张伟\n- 护理等级：半自理\n- 统计周期：近 30 天\n\n## 二、健康评估摘要\n老人近一周期内精神状态平稳，饮食与睡眠情况良好，日常生活活动能力保持稳定。\n\n## 三、照护服务情况\n- 服务订单完成率较高，助餐、助浴等基础照护执行及时。\n- 家属探访频次正常，情感支持充足。\n\n## 四、风险提示\n- 下肢肌力略有下降，跌倒风险需持续关注。\n\n## 五、照护建议\n1. 维持现有护理等级与照护方案。\n2. 每周安排 2-3 次下肢康复训练。\n3. 居室保持地面干燥，夜间开启感应照明。\n\n## 六、家属沟通建议\n建议家属每周至少探访一次，关注老人情绪变化与用药情况。',
 'mock', '本地模拟', '1', 1, '0', 'admin', date_sub(sysdate(), interval 1 day)),
(2, 2, '李秀英', '1', '李秀英 · 健康评估报告', date_sub(curdate(), interval 30 day), curdate(), 72, '1',
 '老人存在轻度营养不良与血压波动，建议加强营养摄入监测并增加巡视频次。',
 '## 一、基本信息\n- 老人姓名：李秀英\n- 护理等级：重度失能\n- 统计周期：近 30 天\n\n## 二、健康评估摘要\n老人近期食欲一般，体重略有下降，血压存在波动，需重点关注营养与循环系统状况。\n\n## 三、照护服务情况\n- 助餐、翻身拍背等服务执行及时。\n- 护理员对皮肤状况进行了定期检查。\n\n## 四、风险提示\n- 营养不良风险：中。\n- 跌倒与压疮风险：需持续关注。\n\n## 五、照护建议\n1. 调整膳食结构，增加优质蛋白摄入。\n2. 每日监测血压并记录。\n3. 每 2 小时协助翻身一次，检查皮肤受压情况。\n\n## 六、家属沟通建议\n建议家属配合营养补充方案，并及时反馈老人进食情况。',
 'mock', '本地模拟', '1', 1, '0', 'admin', date_sub(sysdate(), interval 2 day)),
(3, 100, '钱国华', '0', '钱国华 · 综合照护报告', date_sub(curdate(), interval 30 day), curdate(), 88, '0',
 '老人自理能力较好，服务完成率稳定，建议保持现有照护节奏。',
 '## 一、基本信息\n- 老人姓名：钱国华\n- 护理等级：完全自理\n- 统计周期：近 30 天\n\n## 二、健康评估摘要\n老人精神状态良好，日常生活活动能力较强。\n\n## 三、照护服务情况\n- 基础照护服务按计划执行。\n\n## 四、风险提示\n- 暂无明显风险。\n\n## 五、照护建议\n1. 维持现有照护方案。\n2. 鼓励参加院内文娱活动。\n\n## 六、家属沟通建议\n保持每周沟通，关注老人社交与情绪状态。',
 'mock', '本地模拟', '1', 2, '0', 'admin', date_sub(sysdate(), interval 1 day));
