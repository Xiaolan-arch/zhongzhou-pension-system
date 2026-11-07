-- =====================================================
-- P12 组 C：老人档案入口 + 房间菜单权限统一 + 护理任务反馈
-- =====================================================

-- 1. 护理任务增加执行反馈字段（幂等）
SET @col_exists := (SELECT COUNT(*) FROM information_schema.COLUMNS
                    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_service_task' AND COLUMN_NAME = 'feedback');
SET @ddl := IF(@col_exists = 0,
  'ALTER TABLE `biz_service_task` ADD COLUMN `feedback` varchar(500) DEFAULT NULL COMMENT ''执行反馈'' AFTER `task_status`',
  'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 2. 房间管理菜单权限统一为 business:room:list（与 RoomController 对齐）
UPDATE sys_menu SET perms = 'business:room:list' WHERE menu_id = 4203;

-- 3. 房间管理按钮权限（新增/修改/删除）
DELETE FROM sys_role_menu WHERE menu_id IN (4291, 4292, 4293);
DELETE FROM sys_menu WHERE menu_id IN (4291, 4292, 4293);
INSERT INTO sys_menu VALUES(4291, '房间新增', 4203, 1, '', null, null, '', 1, 0, 'F', '0', '0', 'business:room:add',    '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES(4292, '房间修改', 4203, 2, '', null, null, '', 1, 0, 'F', '0', '0', 'business:room:edit',   '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES(4293, '房间删除', 4203, 3, '', null, null, '', 1, 0, 'F', '0', '0', 'business:room:remove', '#', 'admin', sysdate(), '', null, '');

-- 4. 老人档案入口（隐藏路由，父级在住管理，通过床位详情跳转）
DELETE FROM sys_role_menu WHERE menu_id = 4281;
DELETE FROM sys_menu WHERE menu_id = 4281;
INSERT INTO sys_menu VALUES(4281, '老人档案', 4200, 99, 'elderArchive/:elderId', 'business/elder/archive', null, '', 1, 0, 'C', '1', '0', 'business:residence:list', 'documentation', 'admin', sysdate(), '', null, '在住老人档案聚合页（隐藏路由）');

-- 5. 授权：超级管理员 role 1 + 租户管理员 role 100
INSERT IGNORE INTO sys_role_menu(role_id, menu_id) VALUES
  (1, 4281), (1, 4291), (1, 4292), (1, 4293),
  (100, 4281), (100, 4291), (100, 4292), (100, 4293);

-- 6. 修正「在住管理」菜单结构：由 C 页面改为 M 目录，子页面才能出现在动态路由中
UPDATE sys_menu SET menu_type = 'M', component = NULL, perms = '' WHERE menu_id = 4200;

DELETE FROM sys_role_menu WHERE menu_id = 4205;
DELETE FROM sys_menu WHERE menu_id = 4205;
INSERT INTO sys_menu VALUES(4205, '在住总览', 4200, 1, 'overview', 'business/residence/index', null, '', 1, 0, 'C', '0', '0', 'business:residence:list', 'home', 'admin', sysdate(), '', null, '楼层床位在住总览');

-- 父目录与总览页授权（子菜单原有授权保持不变）
INSERT IGNORE INTO sys_role_menu(role_id, menu_id) VALUES
  (1, 4200), (1, 4205),
  (100, 4200), (100, 4205);

-- 7. 演示数据补齐：为历史服务订单补派护理任务（已有任务则跳过）
INSERT INTO biz_service_task
  (order_id, order_no, elder_id, elder_name, item_id, item_name, nurse_id, nurse_name,
   service_date, plan_time, task_status, feedback, tenant_id, del_flag, create_by, create_time)
SELECT o.order_id, o.order_no, o.elder_id, o.elder_name, o.item_id, o.item_name, o.nurse_id, o.nurse_name,
       date(o.service_time), o.service_time,
       CASE o.status WHEN '2' THEN '2' WHEN '1' THEN '1' ELSE '0' END,
       CASE o.status WHEN '2' THEN '服务已完成，老人状态良好' ELSE NULL END,
       o.tenant_id, '0', 'admin', sysdate()
FROM biz_service_order o
WHERE o.del_flag = '0'
  AND NOT EXISTS (SELECT 1 FROM biz_service_task t WHERE t.order_id = o.order_id);
