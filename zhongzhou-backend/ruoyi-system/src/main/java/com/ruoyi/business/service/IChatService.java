package com.ruoyi.business.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.business.domain.ChatMessage;
import com.ruoyi.business.domain.ChatSession;
import com.ruoyi.business.domain.FamilyToken;

/**
 * AI 智能客服服务
 *
 * @author ruoyi
 */
public interface IChatService
{
    /**
     * 发送消息并获取 AI 回复（sessionId 为空时自动新建会话）
     *
     * @param family    登录家属
     * @param sessionId 会话ID（可空）
     * @param elderId   当前咨询老人ID（可空，须为该家属绑定的老人）
     * @param content   用户消息
     * @return {sessionId, reply}
     */
    public Map<String, Object> chat(FamilyToken family, Long sessionId, Long elderId, String content);

    /**
     * 家属的会话列表
     */
    public List<ChatSession> listSessions(FamilyToken family);

    /**
     * 会话的消息历史（校验归属，按时间正序）
     */
    public List<ChatMessage> listMessages(FamilyToken family, Long sessionId);
}
