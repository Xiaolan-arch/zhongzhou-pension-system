package com.ruoyi.business.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.ChatMessage;
import com.ruoyi.business.domain.ChatSession;

/**
 * AI客服聊天 数据层
 *
 * @author ruoyi
 */
public interface ChatMapper
{
    /** 查询家属的会话列表 */
    public List<ChatSession> selectSessionList(ChatSession session);

    /** 按ID查询会话（归属校验用） */
    public ChatSession selectSessionById(Long sessionId);

    /** 新增会话 */
    public int insertSession(ChatSession session);

    /** 更新会话标题/咨询老人 */
    public int updateSession(ChatSession session);

    /** 查询会话的消息列表（按时间正序） */
    public List<ChatMessage> selectMessageList(@Param("sessionId") Long sessionId, @Param("limit") Integer limit);

    /** 新增消息 */
    public int insertMessage(ChatMessage message);
}
