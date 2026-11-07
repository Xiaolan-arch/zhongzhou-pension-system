-- ============================================================
-- P1 多租户（机构）改造迁移脚本
-- 执行库：zhongzhou
-- ============================================================
USE zhongzhou;

-- 1. 机构（租户）表
CREATE TABLE IF NOT EXISTS sys_tenant (
  tenant_id     bigint(20)   not null auto_increment comment '机构ID',
  tenant_name   varchar(100) not null                comment '机构名称',
  tenant_code   varchar(50)  default ''              comment '机构编码',
  contact_name  varchar(30)  default ''              comment '联系人',
  contact_phone varchar(20)  default ''              comment '联系电话',
  address       varchar(200) default ''              comment '机构地址',
  expire_time   datetime                             comment '到期时间',
  status        char(1)      default '0'             comment '状态（0正常 1停用）',
  del_flag      char(1)      default '0'             comment '删除标志（0存在 2删除）',
  create_by     varchar(64)  default ''              comment '创建者',
  create_time   datetime                             comment '创建时间',
  update_by     varchar(64)  default ''              comment '更新者',
  update_time   datetime                             comment '更新时间',
  remark        varchar(500) default null            comment '备注',
  primary key (tenant_id),
  unique key uk_tenant_name (tenant_name)
) engine=innodb auto_increment=1 comment='机构(租户)表';

-- 2. 核心表增加租户字段（存量数据默认归属机构1）
ALTER TABLE sys_user ADD COLUMN tenant_id bigint(20) NOT NULL DEFAULT 1 COMMENT '机构ID' AFTER dept_id;
ALTER TABLE sys_dept ADD COLUMN tenant_id bigint(20) NOT NULL DEFAULT 1 COMMENT '机构ID' AFTER dept_id;
ALTER TABLE sys_role ADD COLUMN tenant_id bigint(20) NOT NULL DEFAULT 1 COMMENT '机构ID' AFTER role_id;
ALTER TABLE sys_post ADD COLUMN tenant_id bigint(20) NOT NULL DEFAULT 1 COMMENT '机构ID' AFTER post_id;

-- 3. 机构种子数据
INSERT INTO sys_tenant (tenant_id, tenant_name, tenant_code, contact_name, contact_phone, address, status, del_flag, create_by, create_time, remark)
VALUES (1, '中州养老院', 'ZZ', '王院长', '13800000000', '河南省郑州市', '0', '0', 'admin', sysdate(), '默认机构（存量数据归属）'),
       (2, '春晖颐养院', 'CH', '李春晖', '13900000000', '河南省洛阳市', '0', '0', 'admin', sysdate(), '演示机构，用于验证数据隔离');

-- 4. 演示机构（tenant_id = 2）的部门/角色/账号
INSERT INTO sys_dept (dept_id, tenant_id, parent_id, ancestors, dept_name, order_num, leader, phone, email, status, del_flag, create_by, create_time)
VALUES (200, 2, 0, '0', '春晖颐养院', 0, '李春晖', '13900000000', 'ch@qq.com', '0', '0', 'admin', sysdate());

INSERT INTO sys_role (role_id, tenant_id, role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, remark)
VALUES (100, 2, '机构管理员', 'tenant_admin', 1, '1', 1, 1, '0', '0', 'admin', sysdate(), '演示机构管理员角色');

-- 密码与 admin 相同：admin123
INSERT INTO sys_user (user_id, tenant_id, dept_id, user_name, nick_name, user_type, email, phonenumber, sex, avatar, password, status, del_flag, login_ip, login_date, create_by, create_time, remark)
VALUES (100, 2, 200, 'chunhui', '春晖管理员', '00', 'ch@qq.com', '13900000000', '1', '',
        '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', sysdate(), 'admin', sysdate(), '演示机构管理员');

INSERT INTO sys_user_role (user_id, role_id) VALUES (100, 100);

-- 5. 机构管理菜单（仅超级管理员可通过权限访问）
DELETE FROM sys_menu WHERE menu_id IN (2000, 2001, 2002, 2003, 2004);
INSERT INTO sys_menu VALUES ('2000', '机构管理', '1', '10', 'tenant', 'system/tenant/index', '', '', 1, 0, 'C', '0', '0', 'system:tenant:list',   'company', 'admin', sysdate(), '', NULL, '机构管理菜单');
INSERT INTO sys_menu VALUES ('2001', '机构查询', '2000', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'system:tenant:query',  '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES ('2002', '机构新增', '2000', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'system:tenant:add',    '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES ('2003', '机构修改', '2000', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'system:tenant:edit',   '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO sys_menu VALUES ('2004', '机构删除', '2000', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'system:tenant:remove', '#', 'admin', sysdate(), '', NULL, '');

-- 6. 演示机构管理员角色：拥有除机构管理外的全部菜单权限
DELETE FROM sys_role_menu WHERE role_id = 100;
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 100, menu_id FROM sys_menu WHERE menu_id NOT IN (2000, 2001, 2002, 2003, 2004);
