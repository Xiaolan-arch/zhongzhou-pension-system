-- ============================================================
-- P7 补菜单：床位管理 / 护理员管理 / 房间管理
-- 说明：前端 views/business/{bed,nurse,room} 页面已存在，但 sys_menu 缺对应菜单与权限标识
-- 幂等：先删后插，避免重复执行主键冲突
-- ============================================================

DELETE FROM sys_role_menu WHERE menu_id BETWEEN 4201 AND 4234;
DELETE FROM sys_menu WHERE menu_id BETWEEN 4201 AND 4234;

-- 床位管理（挂在「在住管理」4200 下）
INSERT INTO sys_menu VALUES(4201, '床位管理', 4200, 4, 'bed',   'business/bed/index',   null, '', 1, 0, 'C', '0', '0', 'business:bed:list',     'build',      'admin', sysdate(), '', null, '床位信息与护理员分配');
-- 护理员管理
INSERT INTO sys_menu VALUES(4202, '护理员管理', 4200, 5, 'nurse', 'business/nurse/index', null, '', 1, 0, 'C', '0', '0', 'business:nurse:list',   'people',     'admin', sysdate(), '', null, '护理员信息');
-- 房间管理
INSERT INTO sys_menu VALUES(4203, '房间管理', 4200, 6, 'room',  'business/room/index',  null, '', 1, 0, 'C', '0', '0', 'business:residence:list', 'tree-table', 'admin', sysdate(), '', null, '房间信息');

-- 按钮权限
INSERT INTO sys_menu VALUES(4221, '床位新增', 4201, 1, '', null, null, '', 1, 0, 'F', '0', '0', 'business:bed:add',       '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES(4222, '床位修改', 4201, 2, '', null, null, '', 1, 0, 'F', '0', '0', 'business:bed:edit',      '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES(4223, '床位删除', 4201, 3, '', null, null, '', 1, 0, 'F', '0', '0', 'business:bed:remove',    '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES(4231, '护理员查询', 4202, 1, '', null, null, '', 1, 0, 'F', '0', '0', 'business:nurse:query',   '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES(4232, '护理员新增', 4202, 2, '', null, null, '', 1, 0, 'F', '0', '0', 'business:nurse:add',     '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES(4233, '护理员修改', 4202, 3, '', null, null, '', 1, 0, 'F', '0', '0', 'business:nurse:edit',    '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES(4234, '护理员删除', 4202, 4, '', null, null, '', 1, 0, 'F', '0', '0', 'business:nurse:remove',  '#', 'admin', sysdate(), '', null, '');

-- 把原挂在 4200 下的房间增删改按钮改挂到 4203
UPDATE sys_menu SET parent_id = 4203 WHERE menu_id IN (4211, 4212, 4213);

-- 授权：超级管理员 role 1 + 租户管理员 role 100
INSERT IGNORE INTO sys_role_menu(role_id, menu_id) SELECT 1,   menu_id FROM sys_menu WHERE menu_id BETWEEN 4201 AND 4234;
INSERT IGNORE INTO sys_role_menu(role_id, menu_id) SELECT 100, menu_id FROM sys_menu WHERE menu_id BETWEEN 4201 AND 4234;
