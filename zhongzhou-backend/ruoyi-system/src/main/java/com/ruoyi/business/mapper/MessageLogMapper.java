package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.MessageLog;

/**
 * 微信订阅消息发送记录 数据层
 */
public interface MessageLogMapper
{
    /** 查询消息记录列表 */
    public List<MessageLog> selectMessageLogList(MessageLog log);

    /** 新增消息记录 */
    public int insertMessageLog(MessageLog log);
}
