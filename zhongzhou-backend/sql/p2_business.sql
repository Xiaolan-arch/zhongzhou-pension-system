SET NAMES utf8mb4;

-- ============================================================
-- P2 养老服务业务模块：建表 + 字典 + 菜单 + 双租户演示数据
-- 说明：所有业务表带 tenant_id，唯一索引均含 tenant_id
-- ============================================================

DROP TABLE IF EXISTS biz_bill_item;
DROP TABLE IF EXISTS biz_bill;
DROP TABLE IF EXISTS biz_service_order;
DROP TABLE IF EXISTS biz_service_item;
DROP TABLE IF EXISTS biz_bed_nurse;
DROP TABLE IF EXISTS biz_bed;
DROP TABLE IF EXISTS biz_room;
DROP TABLE IF EXISTS biz_nurse;
DROP TABLE IF EXISTS biz_visit;
DROP TABLE IF EXISTS biz_checkout;
DROP TABLE IF EXISTS biz_checkin;
DROP TABLE IF EXISTS biz_elder;

-- ----------------------------
-- 1. 老人档案
-- ----------------------------
CREATE TABLE biz_elder (
  elder_id          bigint(20)      NOT NULL AUTO_INCREMENT COMMENT '老人ID',
  elder_name        varchar(50)     NOT NULL                COMMENT '老人姓名',
  gender            char(1)         DEFAULT '0'             COMMENT '性别（0男 1女）',
  id_card           varchar(30)     DEFAULT ''              COMMENT '身份证号',
  birthday          date            DEFAULT NULL            COMMENT '出生日期',
  age               int(4)          DEFAULT 0               COMMENT '年龄',
  phone             varchar(20)     DEFAULT ''              COMMENT '联系电话',
  address           varchar(200)    DEFAULT ''              COMMENT '户籍地址',
  care_level        varchar(20)     DEFAULT ''              COMMENT '护理等级（biz_care_level）',
  checkin_date      date            DEFAULT NULL            COMMENT '入住日期',
  status            char(1)         DEFAULT '0'             COMMENT '状态（0在住 1已退住 2待入住）',
  avatar            varchar(255)    DEFAULT ''              COMMENT '头像',
  family_name       varchar(50)     DEFAULT ''              COMMENT '家属姓名',
  family_relation   varchar(20)     DEFAULT ''              COMMENT '与老人关系',
  family_phone      varchar(20)     DEFAULT ''              COMMENT '家属电话',
  remark            varchar(500)    DEFAULT ''              COMMENT '备注',
  tenant_id         bigint(20)      NOT NULL DEFAULT 1      COMMENT '机构ID',
  del_flag          char(1)         DEFAULT '0'             COMMENT '删除标志（0存在 2删除）',
  create_by         varchar(64)     DEFAULT ''              COMMENT '创建者',
  create_time       datetime        DEFAULT NULL            COMMENT '创建时间',
  update_by         varchar(64)     DEFAULT ''              COMMENT '更新者',
  update_time       datetime        DEFAULT NULL            COMMENT '更新时间',
  PRIMARY KEY (elder_id),
  KEY idx_elder_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='老人档案表';

-- ----------------------------
-- 2. 房间
-- ----------------------------
CREATE TABLE biz_room (
  room_id           bigint(20)      NOT NULL AUTO_INCREMENT COMMENT '房间ID',
  floor_no          int(4)          NOT NULL                COMMENT '楼层',
  room_no           varchar(20)     NOT NULL                COMMENT '房间号',
  room_type         varchar(20)     DEFAULT ''              COMMENT '房型（biz_room_type）',
  bed_count         int(4)          DEFAULT 0               COMMENT '床位数',
  status            char(1)         DEFAULT '0'             COMMENT '状态（0启用 1停用）',
  remark            varchar(500)    DEFAULT ''              COMMENT '备注',
  tenant_id         bigint(20)      NOT NULL DEFAULT 1      COMMENT '机构ID',
  del_flag          char(1)         DEFAULT '0'             COMMENT '删除标志',
  create_by         varchar(64)     DEFAULT '',
  create_time       datetime        DEFAULT NULL,
  update_by         varchar(64)     DEFAULT '',
  update_time       datetime        DEFAULT NULL,
  PRIMARY KEY (room_id),
  UNIQUE KEY uk_room (tenant_id, floor_no, room_no),
  KEY idx_room_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='房间表';

-- ----------------------------
-- 3. 床位
-- ----------------------------
CREATE TABLE biz_bed (
  bed_id            bigint(20)      NOT NULL AUTO_INCREMENT COMMENT '床位ID',
  room_id           bigint(20)      NOT NULL                COMMENT '房间ID',
  bed_no            varchar(20)     NOT NULL                COMMENT '床位号',
  status            char(1)         DEFAULT '0'             COMMENT '状态（0空闲 1已入住 2外出中）',
  elder_id          bigint(20)      DEFAULT NULL            COMMENT '老人ID',
  tenant_id         bigint(20)      NOT NULL DEFAULT 1      COMMENT '机构ID',
  del_flag          char(1)         DEFAULT '0'             COMMENT '删除标志',
  create_by         varchar(64)     DEFAULT '',
  create_time       datetime        DEFAULT NULL,
  update_by         varchar(64)     DEFAULT '',
  update_time       datetime        DEFAULT NULL,
  PRIMARY KEY (bed_id),
  UNIQUE KEY uk_bed (tenant_id, room_id, bed_no),
  KEY idx_bed_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='床位表';

-- ----------------------------
-- 4. 护理员
-- ----------------------------
CREATE TABLE biz_nurse (
  nurse_id          bigint(20)      NOT NULL AUTO_INCREMENT COMMENT '护理员ID',
  nurse_name        varchar(50)     NOT NULL                COMMENT '姓名',
  phone             varchar(20)     DEFAULT ''              COMMENT '电话',
  status            char(1)         DEFAULT '0'             COMMENT '状态（0正常 1停用）',
  tenant_id         bigint(20)      NOT NULL DEFAULT 1      COMMENT '机构ID',
  del_flag          char(1)         DEFAULT '0'             COMMENT '删除标志',
  create_by         varchar(64)     DEFAULT '',
  create_time       datetime        DEFAULT NULL,
  update_by         varchar(64)     DEFAULT '',
  update_time       datetime        DEFAULT NULL,
  PRIMARY KEY (nurse_id),
  KEY idx_nurse_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='护理员表';

-- ----------------------------
-- 5. 床位-护理员关联
-- ----------------------------
CREATE TABLE biz_bed_nurse (
  id                bigint(20)      NOT NULL AUTO_INCREMENT COMMENT '主键',
  bed_id            bigint(20)      NOT NULL                COMMENT '床位ID',
  nurse_id          bigint(20)      NOT NULL                COMMENT '护理员ID',
  tenant_id         bigint(20)      NOT NULL DEFAULT 1      COMMENT '机构ID',
  create_time       datetime        DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_bed_nurse (tenant_id, bed_id, nurse_id),
  KEY idx_bednurse_bed (bed_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='床位护理员关联表';

-- ----------------------------
-- 6. 入住申请（7 步向导）
-- ----------------------------
CREATE TABLE biz_checkin (
  checkin_id        bigint(20)      NOT NULL AUTO_INCREMENT COMMENT '入住申请ID',
  apply_no          varchar(40)     NOT NULL                COMMENT '申请单号',
  elder_name        varchar(50)     NOT NULL                COMMENT '老人姓名',
  gender            char(1)         DEFAULT '0'             COMMENT '性别（0男 1女）',
  id_card           varchar(30)     DEFAULT ''              COMMENT '身份证号',
  birthday          date            DEFAULT NULL            COMMENT '出生日期',
  age               int(4)          DEFAULT 0               COMMENT '年龄',
  phone             varchar(20)     DEFAULT ''              COMMENT '联系电话',
  address           varchar(200)    DEFAULT ''              COMMENT '户籍地址',
  avatar            varchar(255)    DEFAULT ''              COMMENT '一寸照片',
  id_card_front     varchar(255)    DEFAULT ''              COMMENT '身份证人像面',
  id_card_back      varchar(255)    DEFAULT ''              COMMENT '身份证国徽面',
  family_name       varchar(50)     DEFAULT ''              COMMENT '家属姓名',
  family_relation   varchar(20)     DEFAULT ''              COMMENT '与老人关系',
  family_phone      varchar(20)     DEFAULT ''              COMMENT '家属电话',
  apply_status      char(1)         DEFAULT '0'             COMMENT '状态（0申请中 1待评估 2待审核 3待配置 4待签约 5待缴费 6已入住 9已驳回）',
  self_care         varchar(200)    DEFAULT ''              COMMENT '自理能力评估',
  mental_state      varchar(200)    DEFAULT ''              COMMENT '精神状态评估',
  disease           varchar(500)    DEFAULT ''              COMMENT '疾病情况',
  assessment_result varchar(500)    DEFAULT ''              COMMENT '评估结论',
  suggest_care_level varchar(20)    DEFAULT ''              COMMENT '建议护理等级',
  assessor          varchar(50)     DEFAULT ''              COMMENT '评估人',
  assess_time       datetime        DEFAULT NULL            COMMENT '评估时间',
  audit_result      char(1)         DEFAULT NULL            COMMENT '审核结果（0通过 1驳回）',
  audit_opinion     varchar(500)    DEFAULT ''              COMMENT '审核意见',
  auditor           varchar(50)     DEFAULT ''              COMMENT '审核人',
  audit_time        datetime        DEFAULT NULL            COMMENT '审核时间',
  room_id           bigint(20)      DEFAULT NULL            COMMENT '房间ID',
  bed_id            bigint(20)      DEFAULT NULL            COMMENT '床位ID',
  care_level        varchar(20)     DEFAULT ''              COMMENT '护理等级',
  nurse_ids         varchar(200)    DEFAULT ''              COMMENT '护理员ID串',
  fee_standard      decimal(10,2)   DEFAULT 0.00            COMMENT '费用标准（元/月）',
  contract_no       varchar(50)     DEFAULT ''              COMMENT '合同编号',
  contract_start    date            DEFAULT NULL            COMMENT '合同开始日期',
  contract_end      date            DEFAULT NULL            COMMENT '合同结束日期',
  contract_file     varchar(255)    DEFAULT ''              COMMENT '合同文件',
  sign_date         date            DEFAULT NULL            COMMENT '签约日期',
  pay_status        char(1)         DEFAULT '0'             COMMENT '首期缴费状态（0待支付 1已支付）',
  first_bill_id     bigint(20)      DEFAULT NULL            COMMENT '首期账单ID',
  checkin_date      date            DEFAULT NULL            COMMENT '入住日期',
  tenant_id         bigint(20)      NOT NULL DEFAULT 1      COMMENT '机构ID',
  del_flag          char(1)         DEFAULT '0'             COMMENT '删除标志',
  create_by         varchar(64)     DEFAULT '',
  create_time       datetime        DEFAULT NULL,
  update_by         varchar(64)     DEFAULT '',
  update_time       datetime        DEFAULT NULL,
  remark            varchar(500)    DEFAULT '',
  PRIMARY KEY (checkin_id),
  UNIQUE KEY uk_checkin_no (tenant_id, apply_no),
  KEY idx_checkin_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入住申请表';

-- ----------------------------
-- 7. 退住申请（7 步向导）
-- ----------------------------
CREATE TABLE biz_checkout (
  checkout_id       bigint(20)      NOT NULL AUTO_INCREMENT COMMENT '退住申请ID',
  apply_no          varchar(40)     NOT NULL                COMMENT '申请单号',
  elder_id          bigint(20)      NOT NULL                COMMENT '老人ID',
  elder_name        varchar(50)     DEFAULT ''              COMMENT '老人姓名',
  checkout_date     date            DEFAULT NULL            COMMENT '退住日期',
  reason            varchar(200)    DEFAULT ''              COMMENT '退住原因',
  apply_status      char(1)         DEFAULT '0'             COMMENT '状态（0申请退住 1待申请审批 2待解除合同 3待调整账单 4待账单审批 5待退住审批 6待费用清算 7已完成 9已驳回）',
  audit1_result     char(1)         DEFAULT NULL            COMMENT '申请审批结果',
  audit1_opinion    varchar(500)    DEFAULT '',
  audit1_by         varchar(50)     DEFAULT '',
  audit1_time       datetime        DEFAULT NULL,
  terminate_date    date            DEFAULT NULL            COMMENT '解除合同日期',
  terminate_file    varchar(255)    DEFAULT ''              COMMENT '解除合同文件',
  adjust_amount     decimal(10,2)   DEFAULT 0.00            COMMENT '调整金额',
  adjust_remark     varchar(500)    DEFAULT '',
  audit2_result     char(1)         DEFAULT NULL            COMMENT '账单审批结果',
  audit2_opinion    varchar(500)    DEFAULT '',
  audit2_by         varchar(50)     DEFAULT '',
  audit2_time       datetime        DEFAULT NULL,
  audit3_result     char(1)         DEFAULT NULL            COMMENT '退住审批结果',
  audit3_opinion    varchar(500)    DEFAULT '',
  audit3_by         varchar(50)     DEFAULT '',
  audit3_time       datetime        DEFAULT NULL,
  settle_amount     decimal(10,2)   DEFAULT 0.00            COMMENT '结算金额',
  refund_method     varchar(20)     DEFAULT ''              COMMENT '退款方式',
  settle_time       datetime        DEFAULT NULL            COMMENT '清算时间',
  tenant_id         bigint(20)      NOT NULL DEFAULT 1      COMMENT '机构ID',
  del_flag          char(1)         DEFAULT '0'             COMMENT '删除标志',
  create_by         varchar(64)     DEFAULT '',
  create_time       datetime        DEFAULT NULL,
  update_by         varchar(64)     DEFAULT '',
  update_time       datetime        DEFAULT NULL,
  remark            varchar(500)    DEFAULT '',
  PRIMARY KEY (checkout_id),
  UNIQUE KEY uk_checkout_no (tenant_id, apply_no),
  KEY idx_checkout_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退住申请表';

-- ----------------------------
-- 8. 来访/预约
-- ----------------------------
CREATE TABLE biz_visit (
  visit_id          bigint(20)      NOT NULL AUTO_INCREMENT COMMENT '来访ID',
  visit_type        char(1)         DEFAULT '0'             COMMENT '类型（0参观 1探访）',
  visitor_name      varchar(50)     NOT NULL                COMMENT '来访人姓名',
  visitor_phone     varchar(20)     DEFAULT ''              COMMENT '来访人电话',
  elder_id          bigint(20)      DEFAULT NULL            COMMENT '探访老人ID',
  elder_name        varchar(50)     DEFAULT ''              COMMENT '探访老人姓名',
  visit_date        date            DEFAULT NULL            COMMENT '来访日期',
  visit_time        varchar(20)     DEFAULT ''              COMMENT '来访时间',
  people_count      int(4)          DEFAULT 1               COMMENT '来访人数',
  status            char(1)         DEFAULT '0'             COMMENT '状态（0待确认 1已确认 2已完成 3已取消）',
  remark            varchar(500)    DEFAULT '',
  tenant_id         bigint(20)      NOT NULL DEFAULT 1      COMMENT '机构ID',
  del_flag          char(1)         DEFAULT '0'             COMMENT '删除标志',
  create_by         varchar(64)     DEFAULT '',
  create_time       datetime        DEFAULT NULL,
  update_by         varchar(64)     DEFAULT '',
  update_time       datetime        DEFAULT NULL,
  PRIMARY KEY (visit_id),
  KEY idx_visit_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='来访预约表';

-- ----------------------------
-- 9. 服务项目
-- ----------------------------
CREATE TABLE biz_service_item (
  item_id           bigint(20)      NOT NULL AUTO_INCREMENT COMMENT '服务项目ID',
  item_name         varchar(50)     NOT NULL                COMMENT '项目名称',
  price             decimal(10,2)   DEFAULT 0.00            COMMENT '价格',
  unit              varchar(10)     DEFAULT '次'            COMMENT '单位',
  image             varchar(255)    DEFAULT ''              COMMENT '图片',
  description       varchar(500)    DEFAULT ''              COMMENT '服务说明',
  status            char(1)         DEFAULT '0'             COMMENT '状态（0上架 1下架）',
  tenant_id         bigint(20)      NOT NULL DEFAULT 1      COMMENT '机构ID',
  del_flag          char(1)         DEFAULT '0',
  create_by         varchar(64)     DEFAULT '',
  create_time       datetime        DEFAULT NULL,
  update_by         varchar(64)     DEFAULT '',
  update_time       datetime        DEFAULT NULL,
  PRIMARY KEY (item_id),
  KEY idx_item_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务项目表';

-- ----------------------------
-- 10. 服务订单
-- ----------------------------
CREATE TABLE biz_service_order (
  order_id          bigint(20)      NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  order_no          varchar(40)     NOT NULL                COMMENT '订单号',
  elder_id          bigint(20)      NOT NULL                COMMENT '老人ID',
  elder_name        varchar(50)     DEFAULT ''              COMMENT '老人姓名',
  item_id           bigint(20)      NOT NULL                COMMENT '服务项目ID',
  item_name         varchar(50)     DEFAULT ''              COMMENT '项目名称',
  nurse_id          bigint(20)      DEFAULT NULL            COMMENT '护理员ID',
  nurse_name        varchar(50)     DEFAULT ''              COMMENT '护理员姓名',
  service_time      datetime        DEFAULT NULL            COMMENT '服务时间',
  amount            decimal(10,2)   DEFAULT 0.00            COMMENT '金额',
  status            char(1)         DEFAULT '0'             COMMENT '状态（0待执行 1已执行 2已完成 3已取消）',
  remark            varchar(500)    DEFAULT '',
  tenant_id         bigint(20)      NOT NULL DEFAULT 1      COMMENT '机构ID',
  del_flag          char(1)         DEFAULT '0',
  create_by         varchar(64)     DEFAULT '',
  create_time       datetime        DEFAULT NULL,
  update_by         varchar(64)     DEFAULT '',
  update_time       datetime        DEFAULT NULL,
  PRIMARY KEY (order_id),
  UNIQUE KEY uk_order_no (tenant_id, order_no),
  KEY idx_order_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务订单表';

-- ----------------------------
-- 11. 账单
-- ----------------------------
CREATE TABLE biz_bill (
  bill_id           bigint(20)      NOT NULL AUTO_INCREMENT COMMENT '账单ID',
  bill_no           varchar(40)     NOT NULL                COMMENT '账单编号',
  elder_id          bigint(20)      NOT NULL                COMMENT '老人ID',
  elder_name        varchar(50)     DEFAULT ''              COMMENT '老人姓名',
  checkin_id        bigint(20)      DEFAULT NULL            COMMENT '关联入住申请ID',
  bill_month        varchar(10)     DEFAULT ''              COMMENT '账单月份',
  period_start      date            DEFAULT NULL            COMMENT '账单周期开始',
  period_end        date            DEFAULT NULL            COMMENT '账单周期结束',
  amount            decimal(10,2)   DEFAULT 0.00            COMMENT '账单金额',
  deposit           decimal(10,2)   DEFAULT 0.00            COMMENT '押金',
  prepaid           decimal(10,2)   DEFAULT 0.00            COMMENT '预交款',
  paid_amount       decimal(10,2)   DEFAULT 0.00            COMMENT '已付金额',
  pay_status        char(1)         DEFAULT '0'             COMMENT '支付状态（0待支付 1已支付 2已退款）',
  pay_time          datetime        DEFAULT NULL            COMMENT '支付时间',
  pay_method        varchar(20)     DEFAULT ''              COMMENT '支付方式（biz_pay_method）',
  pay_voucher       varchar(255)    DEFAULT ''              COMMENT '支付凭证',
  bill_type         char(1)         DEFAULT '1'             COMMENT '账单类型（0首期 1月度 2退住结算）',
  tenant_id         bigint(20)      NOT NULL DEFAULT 1      COMMENT '机构ID',
  del_flag          char(1)         DEFAULT '0',
  create_by         varchar(64)     DEFAULT '',
  create_time       datetime        DEFAULT NULL,
  update_by         varchar(64)     DEFAULT '',
  update_time       datetime        DEFAULT NULL,
  remark            varchar(500)    DEFAULT '',
  PRIMARY KEY (bill_id),
  UNIQUE KEY uk_bill (tenant_id, bill_type, checkin_id, bill_month),
  KEY idx_bill_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账单表';

-- ----------------------------
-- 12. 账单明细
-- ----------------------------
CREATE TABLE biz_bill_item (
  item_id           bigint(20)      NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  bill_id           bigint(20)      NOT NULL                COMMENT '账单ID',
  item_type         char(1)         DEFAULT '1'             COMMENT '类型（1加项 2减项）',
  fee_name          varchar(50)     DEFAULT ''              COMMENT '费用项目',
  service_content   varchar(200)    DEFAULT ''              COMMENT '服务内容',
  amount            decimal(10,2)   DEFAULT 0.00            COMMENT '金额（正数）',
  tenant_id         bigint(20)      NOT NULL DEFAULT 1      COMMENT '机构ID',
  create_time       datetime        DEFAULT NULL,
  PRIMARY KEY (item_id),
  KEY idx_billitem_bill (bill_id),
  KEY idx_billitem_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账单明细表';

-- ============================================================
-- 字典
-- ============================================================
DELETE FROM sys_dict_data WHERE dict_type LIKE 'biz_%';
DELETE FROM sys_dict_type WHERE dict_type LIKE 'biz_%';

INSERT INTO sys_dict_type VALUES(1001, '护理等级', 'biz_care_level', '0', 'admin', sysdate(), '', null, '护理等级列表');
INSERT INTO sys_dict_type VALUES(1002, '房型', 'biz_room_type', '0', 'admin', sysdate(), '', null, '房型列表');
INSERT INTO sys_dict_type VALUES(1003, '床位状态', 'biz_bed_status', '0', 'admin', sysdate(), '', null, '床位状态列表');
INSERT INTO sys_dict_type VALUES(1004, '入住状态', 'biz_checkin_status', '0', 'admin', sysdate(), '', null, '入住流程状态');
INSERT INTO sys_dict_type VALUES(1005, '退住状态', 'biz_checkout_status', '0', 'admin', sysdate(), '', null, '退住流程状态');
INSERT INTO sys_dict_type VALUES(1006, '来访类型', 'biz_visit_type', '0', 'admin', sysdate(), '', null, '来访类型');
INSERT INTO sys_dict_type VALUES(1007, '来访状态', 'biz_visit_status', '0', 'admin', sysdate(), '', null, '来访状态');
INSERT INTO sys_dict_type VALUES(1008, '服务订单状态', 'biz_order_status', '0', 'admin', sysdate(), '', null, '服务订单状态');
INSERT INTO sys_dict_type VALUES(1009, '账单支付状态', 'biz_bill_status', '0', 'admin', sysdate(), '', null, '账单支付状态');
INSERT INTO sys_dict_type VALUES(1010, '支付方式', 'biz_pay_method', '0', 'admin', sysdate(), '', null, '支付方式');

INSERT INTO sys_dict_data VALUES(1001, 1, '完全自理', '1', 'biz_care_level', '', 'primary', 'Y', '0', 'admin', sysdate(), '', null, '完全自理');
INSERT INTO sys_dict_data VALUES(1002, 2, '半自理', '2', 'biz_care_level', '', 'info', 'N', '0', 'admin', sysdate(), '', null, '半自理');
INSERT INTO sys_dict_data VALUES(1003, 3, '重度失能', '3', 'biz_care_level', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, '重度失能');
INSERT INTO sys_dict_data VALUES(1004, 4, '特护', '4', 'biz_care_level', '', 'danger', 'N', '0', 'admin', sysdate(), '', null, '特护');
INSERT INTO sys_dict_data VALUES(1011, 1, '单人间', '1', 'biz_room_type', '', '', 'Y', '0', 'admin', sysdate(), '', null, '单人间');
INSERT INTO sys_dict_data VALUES(1012, 2, '双人间', '2', 'biz_room_type', '', '', 'N', '0', 'admin', sysdate(), '', null, '双人间');
INSERT INTO sys_dict_data VALUES(1013, 3, '三人间', '3', 'biz_room_type', '', '', 'N', '0', 'admin', sysdate(), '', null, '三人间');
INSERT INTO sys_dict_data VALUES(1014, 4, '四人间', '4', 'biz_room_type', '', '', 'N', '0', 'admin', sysdate(), '', null, '四人间');
INSERT INTO sys_dict_data VALUES(1015, 5, '套房', '5', 'biz_room_type', '', '', 'N', '0', 'admin', sysdate(), '', null, '套房');
INSERT INTO sys_dict_data VALUES(1016, 6, '特护房', '6', 'biz_room_type', '', '', 'N', '0', 'admin', sysdate(), '', null, '特护房');
INSERT INTO sys_dict_data VALUES(1021, 1, '空闲', '0', 'biz_bed_status', '', 'primary', 'Y', '0', 'admin', sysdate(), '', null, '空闲');
INSERT INTO sys_dict_data VALUES(1022, 2, '已入住', '1', 'biz_bed_status', '', 'success', 'N', '0', 'admin', sysdate(), '', null, '已入住');
INSERT INTO sys_dict_data VALUES(1023, 3, '外出中', '2', 'biz_bed_status', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, '外出中');
INSERT INTO sys_dict_data VALUES(1031, 1, '申请中', '0', 'biz_checkin_status', '', 'info', 'Y', '0', 'admin', sysdate(), '', null, '申请中');
INSERT INTO sys_dict_data VALUES(1032, 2, '待评估', '1', 'biz_checkin_status', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, '待评估');
INSERT INTO sys_dict_data VALUES(1033, 3, '待审核', '2', 'biz_checkin_status', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, '待审核');
INSERT INTO sys_dict_data VALUES(1034, 4, '待配置', '3', 'biz_checkin_status', '', 'info', 'N', '0', 'admin', sysdate(), '', null, '待配置');
INSERT INTO sys_dict_data VALUES(1035, 5, '待签约', '4', 'biz_checkin_status', '', 'info', 'N', '0', 'admin', sysdate(), '', null, '待签约');
INSERT INTO sys_dict_data VALUES(1036, 6, '待缴费', '5', 'biz_checkin_status', '', 'danger', 'N', '0', 'admin', sysdate(), '', null, '待缴费');
INSERT INTO sys_dict_data VALUES(1037, 7, '已入住', '6', 'biz_checkin_status', '', 'success', 'N', '0', 'admin', sysdate(), '', null, '已入住');
INSERT INTO sys_dict_data VALUES(1038, 8, '已驳回', '9', 'biz_checkin_status', '', 'danger', 'N', '0', 'admin', sysdate(), '', null, '已驳回');
INSERT INTO sys_dict_data VALUES(1041, 1, '申请退住', '0', 'biz_checkout_status', '', 'info', 'Y', '0', 'admin', sysdate(), '', null, '申请退住');
INSERT INTO sys_dict_data VALUES(1042, 2, '待申请审批', '1', 'biz_checkout_status', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, '待申请审批');
INSERT INTO sys_dict_data VALUES(1043, 3, '待解除合同', '2', 'biz_checkout_status', '', 'info', 'N', '0', 'admin', sysdate(), '', null, '待解除合同');
INSERT INTO sys_dict_data VALUES(1044, 4, '待调整账单', '3', 'biz_checkout_status', '', 'info', 'N', '0', 'admin', sysdate(), '', null, '待调整账单');
INSERT INTO sys_dict_data VALUES(1045, 5, '待账单审批', '4', 'biz_checkout_status', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, '待账单审批');
INSERT INTO sys_dict_data VALUES(1046, 6, '待退住审批', '5', 'biz_checkout_status', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, '待退住审批');
INSERT INTO sys_dict_data VALUES(1047, 7, '待费用清算', '6', 'biz_checkout_status', '', 'danger', 'N', '0', 'admin', sysdate(), '', null, '待费用清算');
INSERT INTO sys_dict_data VALUES(1048, 8, '已完成', '7', 'biz_checkout_status', '', 'success', 'N', '0', 'admin', sysdate(), '', null, '已完成');
INSERT INTO sys_dict_data VALUES(1049, 9, '已驳回', '9', 'biz_checkout_status', '', 'danger', 'N', '0', 'admin', sysdate(), '', null, '已驳回');
INSERT INTO sys_dict_data VALUES(1051, 1, '参观', '0', 'biz_visit_type', '', 'info', 'Y', '0', 'admin', sysdate(), '', null, '参观');
INSERT INTO sys_dict_data VALUES(1052, 2, '探访', '1', 'biz_visit_type', '', 'primary', 'N', '0', 'admin', sysdate(), '', null, '探访');
INSERT INTO sys_dict_data VALUES(1061, 1, '待确认', '0', 'biz_visit_status', '', 'warning', 'Y', '0', 'admin', sysdate(), '', null, '待确认');
INSERT INTO sys_dict_data VALUES(1062, 2, '已确认', '1', 'biz_visit_status', '', 'primary', 'N', '0', 'admin', sysdate(), '', null, '已确认');
INSERT INTO sys_dict_data VALUES(1063, 3, '已完成', '2', 'biz_visit_status', '', 'success', 'N', '0', 'admin', sysdate(), '', null, '已完成');
INSERT INTO sys_dict_data VALUES(1064, 4, '已取消', '3', 'biz_visit_status', '', 'info', 'N', '0', 'admin', sysdate(), '', null, '已取消');
INSERT INTO sys_dict_data VALUES(1071, 1, '待执行', '0', 'biz_order_status', '', 'warning', 'Y', '0', 'admin', sysdate(), '', null, '待执行');
INSERT INTO sys_dict_data VALUES(1072, 2, '已执行', '1', 'biz_order_status', '', 'primary', 'N', '0', 'admin', sysdate(), '', null, '已执行');
INSERT INTO sys_dict_data VALUES(1073, 3, '已完成', '2', 'biz_order_status', '', 'success', 'N', '0', 'admin', sysdate(), '', null, '已完成');
INSERT INTO sys_dict_data VALUES(1074, 4, '已取消', '3', 'biz_order_status', '', 'info', 'N', '0', 'admin', sysdate(), '', null, '已取消');
INSERT INTO sys_dict_data VALUES(1081, 1, '待支付', '0', 'biz_bill_status', '', 'danger', 'Y', '0', 'admin', sysdate(), '', null, '待支付');
INSERT INTO sys_dict_data VALUES(1082, 2, '已支付', '1', 'biz_bill_status', '', 'success', 'N', '0', 'admin', sysdate(), '', null, '已支付');
INSERT INTO sys_dict_data VALUES(1083, 3, '已退款', '2', 'biz_bill_status', '', 'info', 'N', '0', 'admin', sysdate(), '', null, '已退款');
INSERT INTO sys_dict_data VALUES(1091, 1, '微信', '0', 'biz_pay_method', '', '', 'Y', '0', 'admin', sysdate(), '', null, '微信');
INSERT INTO sys_dict_data VALUES(1092, 2, '支付宝', '1', 'biz_pay_method', '', '', 'N', '0', 'admin', sysdate(), '', null, '支付宝');
INSERT INTO sys_dict_data VALUES(1093, 3, '现金', '2', 'biz_pay_method', '', '', 'N', '0', 'admin', sysdate(), '', null, '现金');
INSERT INTO sys_dict_data VALUES(1094, 4, '银行卡', '3', 'biz_pay_method', '', '', 'N', '0', 'admin', sysdate(), '', null, '银行卡');

-- ============================================================
-- 菜单（4000~4499）与按钮权限
-- ============================================================
DELETE FROM sys_role_menu WHERE menu_id BETWEEN 4000 AND 4499;
DELETE FROM sys_menu WHERE menu_id BETWEEN 4000 AND 4499;

INSERT INTO sys_menu VALUES(4000, '来访管理', 0, 1, 'visit',           'business/visit/index',           null, '', 1, 0, 'C', '0', '0', 'business:visit:list',        'peoples', 'admin', sysdate(), '', null, '养老来访预约');
INSERT INTO sys_menu VALUES(4100, '入退管理', 0, 2, 'checkin',         null,                              null, '', 1, 0, 'M', '0', '0', '',                          'guide',   'admin', sysdate(), '', null, '入住与退住');
INSERT INTO sys_menu VALUES(4101, '入住办理', 4100, 1, 'apply',        'business/checkin/index',          null, '', 1, 0, 'C', '0', '0', 'business:checkin:list',      'edit',    'admin', sysdate(), '', null, '入住七步办理');
INSERT INTO sys_menu VALUES(4102, '入住审核', 4100, 2, 'audit',        'business/checkin/audit',          null, '', 1, 0, 'C', '0', '0', 'business:checkin:audit',     'checkbox', 'admin', sysdate(), '', null, '入住审核');
INSERT INTO sys_menu VALUES(4103, '退住管理', 4100, 3, 'checkout',     'business/checkout/index',         null, '', 1, 0, 'C', '0', '0', 'business:checkout:list',     'back',    'admin', sysdate(), '', null, '退住七步办理');
INSERT INTO sys_menu VALUES(4200, '在住管理', 0, 3, 'residence',       'business/residence/index',        null, '', 1, 0, 'C', '0', '0', 'business:residence:list',    'home',    'admin', sysdate(), '', null, '楼层房间床位');
INSERT INTO sys_menu VALUES(4300, '服务管理', 0, 4, 'service',         null,                              null, '', 1, 0, 'M', '0', '0', '',                          'server',  'admin', sysdate(), '', null, '养老服务');
INSERT INTO sys_menu VALUES(4301, '服务项目', 4300, 1, 'item',         'business/service/item',           null, '', 1, 0, 'C', '0', '0', 'business:service:item:list', 'shopping', 'admin', sysdate(), '', null, '服务项目');
INSERT INTO sys_menu VALUES(4302, '护理任务', 4300, 2, 'task',         'business/service/task',           null, '', 1, 0, 'C', '0', '0', 'business:service:task',     'people',  'admin', sysdate(), '', null, '床位护理员分配');
INSERT INTO sys_menu VALUES(4303, '服务订单', 4300, 3, 'order',        'business/service/order',          null, '', 1, 0, 'C', '0', '0', 'business:service:order:list', 'documentation', 'admin', sysdate(), '', null, '服务订单');
INSERT INTO sys_menu VALUES(4400, '财务管理', 0, 5, 'bill',            'business/finance/index',          null, '', 1, 0, 'C', '0', '0', 'business:bill:list',        'money',   'admin', sysdate(), '', null, '账单与缴费');

-- 按钮
INSERT INTO sys_menu VALUES(4001, '来访新增', 4000, 1, '', null, null, '', 1, 0, 'F', '0', '0', 'business:visit:add',      '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES(4002, '来访修改', 4000, 2, '', null, null, '', 1, 0, 'F', '0', '0', 'business:visit:edit',     '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES(4003, '来访删除', 4000, 3, '', null, null, '', 1, 0, 'F', '0', '0', 'business:visit:remove',   '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES(4004, '来访确认', 4000, 4, '', null, null, '', 1, 0, 'F', '0', '0', 'business:visit:confirm',  '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES(4111, '入住编辑', 4101, 1, '', null, null, '', 1, 0, 'F', '0', '0', 'business:checkin:edit',   '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES(4112, '入住删除', 4101, 2, '', null, null, '', 1, 0, 'F', '0', '0', 'business:checkin:remove', '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES(4113, '入住完成', 4101, 3, '', null, null, '', 1, 0, 'F', '0', '0', 'business:checkin:complete', '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES(4121, '入住审核', 4102, 1, '', null, null, '', 1, 0, 'F', '0', '0', 'business:checkin:audit',  '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES(4122, '入住驳回', 4102, 2, '', null, null, '', 1, 0, 'F', '0', '0', 'business:checkin:reject', '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES(4131, '退住编辑', 4103, 1, '', null, null, '', 1, 0, 'F', '0', '0', 'business:checkout:edit',  '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES(4132, '退住删除', 4103, 2, '', null, null, '', 1, 0, 'F', '0', '0', 'business:checkout:remove', '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES(4133, '退住完成', 4103, 3, '', null, null, '', 1, 0, 'F', '0', '0', 'business:checkout:complete', '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES(4211, '房间新增', 4200, 1, '', null, null, '', 1, 0, 'F', '0', '0', 'business:room:add',       '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES(4212, '房间修改', 4200, 2, '', null, null, '', 1, 0, 'F', '0', '0', 'business:room:edit',      '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES(4213, '房间删除', 4200, 3, '', null, null, '', 1, 0, 'F', '0', '0', 'business:room:remove',    '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES(4311, '项目新增', 4301, 1, '', null, null, '', 1, 0, 'F', '0', '0', 'business:service:item:add',    '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES(4312, '项目修改', 4301, 2, '', null, null, '', 1, 0, 'F', '0', '0', 'business:service:item:edit',   '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES(4313, '项目删除', 4301, 3, '', null, null, '', 1, 0, 'F', '0', '0', 'business:service:item:remove', '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES(4321, '护理员分配', 4302, 1, '', null, null, '', 1, 0, 'F', '0', '0', 'business:service:assign', '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES(4331, '订单新增', 4303, 1, '', null, null, '', 1, 0, 'F', '0', '0', 'business:service:order:add',     '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES(4332, '订单修改', 4303, 2, '', null, null, '', 1, 0, 'F', '0', '0', 'business:service:order:edit',    '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES(4333, '订单删除', 4303, 3, '', null, null, '', 1, 0, 'F', '0', '0', 'business:service:order:remove',  '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES(4334, '订单执行', 4303, 4, '', null, null, '', 1, 0, 'F', '0', '0', 'business:service:order:execute', '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES(4401, '账单导出', 4400, 1, '', null, null, '', 1, 0, 'F', '0', '0', 'business:bill:export',   '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES(4402, '账单缴费', 4400, 2, '', null, null, '', 1, 0, 'F', '0', '0', 'business:bill:pay',      '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES(4403, '生成账单', 4400, 3, '', null, null, '', 1, 0, 'F', '0', '0', 'business:bill:generate', '#', 'admin', sysdate(), '', null, '');

-- 角色 100（租户管理员）授权（幂等）
INSERT IGNORE INTO sys_role_menu(role_id, menu_id)
SELECT 100, menu_id FROM sys_menu WHERE menu_id BETWEEN 4000 AND 4499;

-- ============================================================
-- 演示数据：租户 1（中州养老院）
-- ============================================================
INSERT INTO biz_room(room_id, floor_no, room_no, room_type, bed_count, status, tenant_id, del_flag, create_by, create_time) VALUES
(101,1,'101','4',4,'0',1,'0','admin',sysdate()),
(102,1,'102','2',2,'0',1,'0','admin',sysdate()),
(103,1,'103','6',1,'0',1,'0','admin',sysdate()),
(201,2,'201','2',2,'0',1,'0','admin',sysdate()),
(202,2,'202','2',2,'0',1,'0','admin',sysdate()),
(203,2,'203','1',1,'0',1,'0','admin',sysdate()),
(301,3,'301','4',4,'0',1,'0','admin',sysdate()),
(302,3,'302','2',2,'0',1,'0','admin',sysdate()),
(303,3,'303','5',1,'0',1,'0','admin',sysdate());

INSERT INTO biz_bed(bed_id, room_id, bed_no, status, elder_id, tenant_id, del_flag, create_by, create_time) VALUES
(1011,101,'1011','1',1,1,'0','admin',sysdate()),
(1012,101,'1012','1',2,1,'0','admin',sysdate()),
(1013,101,'1013','0',null,1,'0','admin',sysdate()),
(1014,101,'1014','0',null,1,'0','admin',sysdate()),
(1021,102,'1021','1',3,1,'0','admin',sysdate()),
(1022,102,'1022','0',null,1,'0','admin',sysdate()),
(1031,103,'1031','1',4,1,'0','admin',sysdate()),
(2011,201,'2011','1',5,1,'0','admin',sysdate()),
(2012,201,'2012','0',null,1,'0','admin',sysdate()),
(2021,202,'2021','1',6,1,'0','admin',sysdate()),
(2022,202,'2022','0',null,1,'0','admin',sysdate()),
(2031,203,'2031','0',null,1,'0','admin',sysdate()),
(3011,301,'3011','1',7,1,'0','admin',sysdate()),
(3012,301,'3012','1',8,1,'0','admin',sysdate()),
(3013,301,'3013','0',null,1,'0','admin',sysdate()),
(3014,301,'3014','2',null,1,'0','admin',sysdate()),
(3021,302,'3021','0',null,1,'0','admin',sysdate()),
(3022,302,'3022','0',null,1,'0','admin',sysdate()),
(3031,303,'3031','0',null,1,'0','admin',sysdate());

INSERT INTO biz_nurse(nurse_id, nurse_name, phone, status, tenant_id, del_flag, create_by, create_time) VALUES
(1,'张护士','13900000001','0',1,'0','admin',sysdate()),
(2,'李护理','13900000002','0',1,'0','admin',sysdate()),
(3,'王护理','13900000003','0',1,'0','admin',sysdate()),
(4,'刘护理','13900000004','0',1,'0','admin',sysdate());

INSERT INTO biz_bed_nurse(bed_id, nurse_id, tenant_id, create_time) VALUES
(1011,1,1,sysdate()), (1011,2,1,sysdate()),
(1012,2,1,sysdate()),
(1021,3,1,sysdate()),
(1031,1,1,sysdate()), (1031,4,1,sysdate()),
(2011,2,1,sysdate()),
(2021,4,1,sysdate()),
(3011,3,1,sysdate()), (3011,4,1,sysdate()),
(3012,1,1,sysdate());

INSERT INTO biz_elder(elder_id, elder_name, gender, id_card, birthday, age, phone, address, care_level, checkin_date, status, family_name, family_relation, family_phone, tenant_id, del_flag, create_by, create_time) VALUES
(1,'张伟','0','230203197701291029','1937-01-29',89,'13811110001','哈尔滨市道里区','3','2026-09-01','0','张敏','女儿','13822220001',1,'0','admin',sysdate()),
(2,'李秀英','1','230203194301150021','1943-01-15',83,'13811110002','哈尔滨市南岗区','1','2026-09-05','0','李强','儿子','13822220002',1,'0','admin',sysdate()),
(3,'王建国','0','230203193305200011','1933-05-20',93,'13811110003','哈尔滨市香坊区','2','2026-09-10','0','王芳','女儿','13822220003',1,'0','admin',sysdate()),
(4,'刘桂芳','1','230203193702080022','1937-02-08',89,'13811110004','哈尔滨市道外区','4','2026-09-15','0','刘洋','儿子','13822220004',1,'0','admin',sysdate()),
(5,'陈志强','0','230203194006120013','1940-06-12',86,'13811110005','哈尔滨市松北区','3','2026-10-01','0','陈静','女儿','13822220005',1,'0','admin',sysdate()),
(6,'赵玉兰','1','230203194508300024','1945-08-30',81,'13811110006','哈尔滨市平房区','2','2026-10-05','0','赵磊','儿子','13822220006',1,'0','admin',sysdate()),
(7,'孙德明','0','230203193909180015','1939-09-18',87,'13811110007','哈尔滨市呼兰区','3','2026-10-08','0','孙丽','女儿','13822220007',1,'0','admin',sysdate()),
(8,'周凤英','1','230203194111250026','1941-11-25',85,'13811110008','哈尔滨市阿城区','1','2026-10-08','0','周涛','儿子','13822220008',1,'0','admin',sysdate()),
(9,'吴国强','0','230203193803100017','1938-03-10',90,'13811110009','哈尔滨市双城区','2',null,'2','吴红','女儿','13822220009',1,'0','admin',sysdate()),
(10,'郑美华','1','230203194204020028','1942-04-02',84,'13811110010','哈尔滨市宾县','3',null,'2','郑军','儿子','13822220010',1,'0','admin',sysdate());

INSERT INTO biz_checkin(checkin_id, apply_no, elder_name, gender, id_card, birthday, age, phone, address, family_name, family_relation, family_phone, apply_status, self_care, mental_state, disease, assessment_result, suggest_care_level, assessor, assess_time, care_level, room_id, bed_id, fee_standard, checkin_date, tenant_id, del_flag, create_by, create_time, remark) VALUES
(1,'ZD202611010001','吴国强','0','230203193803100017','1938-03-10',90,'13811110009','哈尔滨市双城区','吴红','女儿','13822220009','2','部分自理，可自行进食','意识清晰','高血压、关节炎','生活部分自理，建议半自理护理','2','王护理','2026-10-28 10:00:00','2',null,null,0.00,'2026-11-08',1,'0','admin',sysdate(),'待入住审核'),
(2,'ZD202611010002','郑美华','1','230203194204020028','1942-04-02',84,'13811110010','哈尔滨市宾县','郑军','儿子','13822220010','5','行动不便，需协助','意识清晰','糖尿病、骨质疏松','重度失能，建议特护','3','李护理','2026-10-29 14:00:00','3',301,3013,4800.00,'2026-11-15',1,'0','admin',sysdate(),'已配置待缴费'),
(3,'ZD202610080001','周凤英','1','230203194111250026','1941-11-25',85,'13811110008','哈尔滨市阿城区','周涛','儿子','13822220008','6','完全自理','意识清晰','无重大疾病','完全自理','1','张护士','2026-10-06 09:00:00','1',301,3012,2600.00,'2026-10-08',1,'0','admin',sysdate(),'已完成入住');

INSERT INTO biz_checkout(checkout_id, apply_no, elder_id, elder_name, checkout_date, reason, apply_status, audit1_result, audit1_opinion, audit1_by, audit1_time, terminate_date, tenant_id, del_flag, create_by, create_time, remark) VALUES
(1,'TZ202611010001',1,'张伟','2026-11-10','护理质量不高','3','0','同意退住','李护理','2026-11-01 10:00:00','2026-11-10',1,'0','admin',sysdate(),'待调整账单');

INSERT INTO biz_visit(visit_id, visit_type, visitor_name, visitor_phone, elder_id, elder_name, visit_date, visit_time, people_count, status, remark, tenant_id, del_flag, create_by, create_time) VALUES
(1,'0','王女士','13800000001',null,'','2026-11-05','09:30',2,'0','参观园区',1,'0','admin',sysdate()),
(2,'1','李强','13800000002',1,'张伟','2026-11-03','15:00',1,'1','探访父亲',1,'0','admin',sysdate()),
(3,'1','陈红','13800000003',5,'陈志强','2026-11-04','10:00',2,'2','探访父亲',1,'0','admin',sysdate()),
(4,'0','刘先生','13800000004',null,'','2026-11-06','14:00',3,'1','参观园区',1,'0','admin',sysdate()),
(5,'1','赵敏','13800000005',2,'李秀英','2026-11-07','16:00',1,'3','探访母亲，临时取消',1,'0','admin',sysdate());

INSERT INTO biz_service_item(item_id, item_name, price, unit, description, status, tenant_id, del_flag, create_by, create_time) VALUES
(1,'助浴',80.00,'次','协助老人沐浴、擦身','0',1,'0','admin',sysdate()),
(2,'助餐',30.00,'餐','辅助进食、营养配餐','0',1,'0','admin',sysdate()),
(3,'洗头',40.00,'次','床上洗头、吹干','0',1,'0','admin',sysdate()),
(4,'修剪指甲',20.00,'次','手部、脚部指甲修剪','0',1,'0','admin',sysdate()),
(5,'衣物清洁',25.00,'次','衣物清洗、烘干、整理','0',1,'0','admin',sysdate()),
(6,'谈心',30.00,'次','心理慰藉、陪伴聊天','0',1,'0','admin',sysdate()),
(7,'翻身拍背',35.00,'次','协助翻身、拍背排痰','0',1,'0','admin',sysdate()),
(8,'复健运动',50.00,'次','肢体康复训练','0',1,'0','admin',sysdate());

INSERT INTO biz_service_order(order_no, elder_id, elder_name, item_id, item_name, nurse_id, nurse_name, service_time, amount, status, tenant_id, del_flag, create_by, create_time) VALUES
('FW202611030001',1,'张伟',1,'助浴',1,'张护士','2026-11-03 09:00:00',80.00,'2',1,'0','admin',sysdate()),
('FW202611030002',2,'李秀英',2,'助餐',2,'李护理','2026-11-03 11:30:00',30.00,'1',1,'0','admin',sysdate()),
('FW202611040001',4,'刘桂芳',3,'洗头',3,'王护理','2026-11-04 10:00:00',40.00,'0',1,'0','admin',sysdate()),
('FW202611040002',6,'赵玉兰',4,'修剪指甲',4,'刘护理','2026-11-04 14:00:00',20.00,'0',1,'0','admin',sysdate()),
('FW202611050001',5,'陈志强',6,'谈心',1,'张护士','2026-11-05 15:00:00',30.00,'1',1,'0','admin',sysdate()),
('FW202611060001',7,'孙德明',8,'复健运动',2,'李护理','2026-11-06 09:30:00',50.00,'0',1,'0','admin',sysdate());

INSERT INTO biz_bill(bill_no, elder_id, elder_name, checkin_id, bill_month, period_start, period_end, amount, deposit, prepaid, paid_amount, pay_status, pay_time, pay_method, bill_type, tenant_id, del_flag, create_by, create_time) VALUES
('ZD20261001001',2,'李秀英',null,'2026-10','2026-10-01','2026-10-31',2200.00,1000.00,0.00,2200.00,'1','2026-10-06 15:00:00','2','1',1,'0','admin',sysdate()),
('ZD20261008001',8,'周凤英',3,'2026-10','2026-10-08','2026-11-07',50000.00,0.00,0.00,50000.00,'1','2026-10-08 16:00:00','0','0',1,'0','admin',sysdate()),
('ZD20261101001',4,'刘桂芳',null,'2026-11','2026-11-01','2026-11-30',3700.00,1000.00,700.00,0.00,'0',null,'','1',1,'0','admin',sysdate()),
('ZD20261101002',5,'陈志强',null,'2026-11','2026-11-01','2026-11-30',3000.00,1000.00,0.00,0.00,'0',null,'','1',1,'0','admin',sysdate()),
('ZD20261101003',1,'张伟',null,'2026-11','2026-11-01','2026-11-30',3099.00,1000.00,1000.00,3099.00,'1','2026-11-06 10:00:00','1','1',1,'0','admin',sysdate());

INSERT INTO biz_bill_item(bill_id, item_type, fee_name, service_content, amount, tenant_id, create_time) VALUES
(3,'1','护理费用','特护护理等级',2000.00,1,sysdate()),
(3,'1','床位费用','特护房',1700.00,1,sysdate()),
(3,'2','医保支付','医保报销',0.00,1,sysdate()),
(4,'1','护理费用','重度失能护理',1800.00,1,sysdate()),
(4,'1','床位费用','双人间',1200.00,1,sysdate()),
(5,'1','护理费用','重度失能护理',2000.00,1,sysdate()),
(5,'1','床位费用','四人间',1099.00,1,sysdate()),
(1,'1','护理费用','完全自理',1000.00,1,sysdate()),
(1,'1','床位费用','双人间',1200.00,1,sysdate());

-- ============================================================
-- 演示数据：租户 2（春晖颐养院）
-- ============================================================
INSERT INTO biz_room(room_id, floor_no, room_no, room_type, bed_count, status, tenant_id, del_flag, create_by, create_time) VALUES
(1001,1,'101','2',2,'0',2,'0','chunhui',sysdate());

INSERT INTO biz_bed(bed_id, room_id, bed_no, status, elder_id, tenant_id, del_flag, create_by, create_time) VALUES
(10011,1001,'10011','1',100,2,'0','chunhui',sysdate()),
(10012,1001,'10012','0',null,2,'0','chunhui',sysdate());

INSERT INTO biz_nurse(nurse_id, nurse_name, phone, status, tenant_id, del_flag, create_by, create_time) VALUES
(1001,'春晖护理员','13900001001','0',2,'0','chunhui',sysdate());

INSERT INTO biz_bed_nurse(bed_id, nurse_id, tenant_id, create_time) VALUES
(10011,1001,2,sysdate());

INSERT INTO biz_elder(elder_id, elder_name, gender, id_card, birthday, age, phone, address, care_level, checkin_date, status, family_name, family_relation, family_phone, tenant_id, del_flag, create_by, create_time) VALUES
(100,'钱国华','0','230203194510100019','1945-10-10',81,'13833330001','哈尔滨市依兰县','2','2026-10-01','0','钱小华','儿子','13844440001',2,'0','chunhui',sysdate());

INSERT INTO biz_checkin(checkin_id, apply_no, elder_name, gender, id_card, birthday, age, phone, address, family_name, family_relation, family_phone, apply_status, self_care, mental_state, disease, assessment_result, suggest_care_level, assessor, assess_time, care_level, room_id, bed_id, fee_standard, checkin_date, tenant_id, del_flag, create_by, create_time, remark) VALUES
(100,'CH202610010001','钱国华','0','230203194510100019','1945-10-10',81,'13833330001','哈尔滨市依兰县','钱小华','儿子','13844440001','6','部分自理','意识清晰','高血压','生活部分自理','2','春晖护理员','2026-09-28 10:00:00','2',1001,10011,2600.00,'2026-10-01',2,'0','chunhui',sysdate(),'已完成入住');

INSERT INTO biz_visit(visit_id, visit_type, visitor_name, visitor_phone, elder_id, elder_name, visit_date, visit_time, people_count, status, remark, tenant_id, del_flag, create_by, create_time) VALUES
(100,'1','钱小华','13844440001',100,'钱国华','2026-11-04','10:00',1,'1','探访父亲',2,'0','chunhui',sysdate());

INSERT INTO biz_service_item(item_id, item_name, price, unit, description, status, tenant_id, del_flag, create_by, create_time) VALUES
(101,'助浴',80.00,'次','协助老人沐浴、擦身','0',2,'0','chunhui',sysdate()),
(102,'助餐',30.00,'餐','辅助进食、营养配餐','0',2,'0','chunhui',sysdate());

INSERT INTO biz_service_order(order_no, elder_id, elder_name, item_id, item_name, nurse_id, nurse_name, service_time, amount, status, tenant_id, del_flag, create_by, create_time) VALUES
('CH202611010001',100,'钱国华',101,'助浴',1001,'春晖护理员','2026-11-02 09:00:00',80.00,'0',2,'0','chunhui',sysdate());

INSERT INTO biz_bill(bill_no, elder_id, elder_name, checkin_id, bill_month, period_start, period_end, amount, deposit, prepaid, paid_amount, pay_status, pay_time, pay_method, bill_type, tenant_id, del_flag, create_by, create_time) VALUES
('CH20261101001',100,'钱国华',100,'2026-11','2026-11-01','2026-11-30',2600.00,1000.00,0.00,0.00,'0',null,'','1',2,'0','chunhui',sysdate());

INSERT INTO biz_bill_item(bill_id, item_type, fee_name, service_content, amount, tenant_id, create_time) VALUES
(6,'1','护理费用','半自理护理',1400.00,2,sysdate()),
(6,'1','床位费用','双人间',1200.00,2,sysdate());
