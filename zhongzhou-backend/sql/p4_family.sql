SET NAMES utf8mb4;

-- ============================================================
-- P4 家属端小程序：家属绑定表 + 双租户演示数据
-- 登录方式：手机号 + 短信验证码（演示环境验证码由接口直接返回）
-- ============================================================

DROP TABLE IF EXISTS biz_family;

CREATE TABLE biz_family (
  family_id         bigint(20)      NOT NULL AUTO_INCREMENT COMMENT '家属ID',
  family_name       varchar(50)     NOT NULL                COMMENT '家属姓名',
  phone             varchar(20)     NOT NULL                COMMENT '登录手机号',
  relation          varchar(20)     DEFAULT ''              COMMENT '与老人关系',
  elder_id          bigint(20)      NOT NULL                COMMENT '绑定老人ID',
  status            char(1)         DEFAULT '0'             COMMENT '状态（0正常 1停用）',
  remark            varchar(200)    DEFAULT ''              COMMENT '备注',
  tenant_id         bigint(20)      NOT NULL DEFAULT 1      COMMENT '机构ID',
  del_flag          char(1)         DEFAULT '0'             COMMENT '删除标志（0存在 2删除）',
  create_by         varchar(64)     DEFAULT ''              COMMENT '创建者',
  create_time       datetime        DEFAULT NULL            COMMENT '创建时间',
  update_by         varchar(64)     DEFAULT ''              COMMENT '更新者',
  update_time       datetime        DEFAULT NULL            COMMENT '更新时间',
  PRIMARY KEY (family_id),
  UNIQUE KEY uk_family (tenant_id, phone, elder_id),
  KEY idx_family_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家属绑定表';

-- 租户 1（中州养老院）
INSERT INTO biz_family(family_id, family_name, phone, relation, elder_id, status, remark, tenant_id, del_flag, create_by, create_time) VALUES
(1, '张敏', '13822220001', '女儿', 1,  '0', '演示账号：张伟家属', 1, '0', 'admin', sysdate()),
(2, '李强', '13822220002', '儿子', 2,  '0', '演示账号：李秀英家属', 1, '0', 'admin', sysdate()),
(3, '王芳', '13822220003', '女儿', 3,  '0', '演示账号：王建国家属', 1, '0', 'admin', sysdate()),
(4, '刘洋', '13822220004', '儿子', 4,  '0', '演示账号：刘桂芳家属', 1, '0', 'admin', sysdate()),
(5, '周涛', '13822220008', '儿子', 8,  '0', '演示账号：周凤英家属', 1, '0', 'admin', sysdate());

-- 租户 2（春晖颐养院）
INSERT INTO biz_family(family_id, family_name, phone, relation, elder_id, status, remark, tenant_id, del_flag, create_by, create_time) VALUES
(6, '钱小华', '13844440001', '儿子', 100, '0', '演示账号：钱国华家属', 2, '0', 'chunhui', sysdate());
