-- =====================================================
-- P18 AI 智能客服
-- 家属端聊天会话与消息记录
-- =====================================================

-- 1. 聊天会话
CREATE TABLE IF NOT EXISTS `biz_chat_session` (
  `session_id`  bigint       NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `tenant_id`   bigint       NOT NULL DEFAULT 1 COMMENT '机构ID',
  `family_id`   bigint       NOT NULL COMMENT '家属ID',
  `elder_id`    bigint       DEFAULT NULL COMMENT '当前咨询老人ID',
  `title`       varchar(50)  DEFAULT NULL COMMENT '会话标题（首条消息截断）',
  `del_flag`    char(1)      DEFAULT '0' COMMENT '删除标志',
  `create_by`   varchar(64)  DEFAULT NULL,
  `create_time` datetime     DEFAULT NULL,
  `update_by`   varchar(64)  DEFAULT NULL,
  `update_time` datetime     DEFAULT NULL,
  PRIMARY KEY (`session_id`),
  KEY `idx_chat_session_family` (`tenant_id`, `family_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI客服聊天会话';

-- 2. 聊天消息
CREATE TABLE IF NOT EXISTS `biz_chat_message` (
  `msg_id`      bigint       NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `session_id`  bigint       NOT NULL COMMENT '会话ID',
  `role`        varchar(20)  NOT NULL COMMENT '角色（user/assistant/system）',
  `content`     text         COMMENT '消息内容',
  `tokens`      int          DEFAULT NULL COMMENT '消耗token数',
  `create_time` datetime     DEFAULT NULL COMMENT '发送时间',
  PRIMARY KEY (`msg_id`),
  KEY `idx_chat_message_session` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI客服聊天消息';
