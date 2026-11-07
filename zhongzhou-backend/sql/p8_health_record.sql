-- =====================================================
-- P8 老人体征数据录入
-- =====================================================

-- 1. 体征记录表（每天/每次测量一条，护士录入）
CREATE TABLE IF NOT EXISTS `biz_health_record` (
  `record_id`      bigint(20)    NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `elder_id`       bigint(20)    NOT NULL                COMMENT '老人ID',
  `record_date`    date          NOT NULL                COMMENT '测量日期',
  `record_time`    varchar(8)    DEFAULT ''              COMMENT '测量时间（HH:mm）',
  `blood_pressure_high`  int(5)    DEFAULT NULL          COMMENT '收缩压（高压）mmHg',
  `blood_pressure_low`   int(5)    DEFAULT NULL          COMMENT '舒张压（低压）mmHg',
  `heart_rate`     int(5)        DEFAULT NULL            COMMENT '心率 次/分',
  `body_temp`      decimal(4,1)  DEFAULT NULL            COMMENT '体温 ℃',
  `blood_sugar`    decimal(5,2)  DEFAULT NULL            COMMENT '血糖 mmol/L',
  `blood_oxygen`   decimal(4,1)  DEFAULT NULL            COMMENT '血氧饱和度 %',
  `weight`         decimal(5,1)  DEFAULT NULL            COMMENT '体重 kg',
  `steps`          int(8)        DEFAULT NULL            COMMENT '步数',
  `sleep_hours`    decimal(3,1)  DEFAULT NULL            COMMENT '睡眠时长 h',
  `is_abnormal`    char(1)       DEFAULT '0'             COMMENT '是否异常（0正常 1异常）',
  `abnormal_items` varchar(200)  DEFAULT ''              COMMENT '异常项（逗号分隔，如：收缩压高,血糖偏高）',
  `remark`         varchar(500)  DEFAULT ''              COMMENT '备注',
  `tenant_id`      bigint(20)    NOT NULL                COMMENT '机构ID',
  `del_flag`       char(1)       DEFAULT '0'             COMMENT '删除标志',
  `create_by`      varchar(64)   DEFAULT ''              COMMENT '创建者',
  `create_time`    datetime      DEFAULT NULL            COMMENT '创建时间',
  PRIMARY KEY (`record_id`),
  KEY `idx_elder_date` (`elder_id`, `record_date`),
  KEY `idx_abnormal` (`is_abnormal`, `tenant_id`, `record_date`),
  KEY `idx_tenant` (`tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='老人体征记录表';

-- 2. TenantSqlInterceptor TENANT_TABLES 追加：下次启动自动生效，这里手动：
INSERT IGNORE INTO sys_table_tenant (table_name) VALUES ('biz_health_record');
