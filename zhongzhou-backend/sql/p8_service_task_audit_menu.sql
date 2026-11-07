-- ============================================================
-- P8 补菜单：护理任务管理 / 业务审计日志
-- 幂等：先删后插
-- ============================================================

DELETE FROM sys_role_menu WHERE menu_id BETWEEN 4241 AND 4262;
DELETE FROM sys_menu WHERE menu_id BETWEEN 4241 AND 4262;

-- 护理任务管理（挂在「在住管理」4200 下）
INSERT INTO sys_menu VALUES(4241, '护理任务', 4200, 7, 'serviceTask', 'business/serviceTask/index', null, '', 1, 0, 'C', '0', '0', 'business:serviceTask:list', 'job', 'admin', sysdate(), '', null, '服务订单派发的护理任务');
INSERT INTO sys_menu VALUES(4251, '任务状态更新', 4241, 1, '', null, null, '', 1, 0, 'F', '0', '0', 'business:serviceTask:edit', '#', 'admin', sysdate(), '', null, '');

-- 业务审计日志（挂在「系统管理」1 下）
INSERT INTO sys_menu VALUES(4261, '业务审计日志', 1, 11, 'auditLog', 'business/auditLog/index', null, '', 1, 0, 'C', '0', '0', 'business:auditLog:list', 'log', 'admin', sysdate(), '', null, '收款/退款等关键业务操作审计');

-- 授权：超级管理员 role 1 + 租户管理员 role 100
INSERT IGNORE INTO sys_role_menu(role_id, menu_id) SELECT 1,   menu_id FROM sys_menu WHERE menu_id BETWEEN 4241 AND 4262;
INSERT IGNORE INTO sys_role_menu(role_id, menu_id) SELECT 100, menu_id FROM sys_menu WHERE menu_id BETWEEN 4241 AND 4262;
