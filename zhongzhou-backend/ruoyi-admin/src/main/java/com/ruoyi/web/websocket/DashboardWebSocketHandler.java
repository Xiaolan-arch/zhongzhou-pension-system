package com.ruoyi.web.websocket;

import java.io.IOException;
import java.net.URI;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.web.service.TokenService;

/**
 * 工作台实时推送：客户端通过 ?token=xxx 建立连接，服务端广播刷新通知
 *
 * @author ruoyi
 */
@Component
public class DashboardWebSocketHandler extends TextWebSocketHandler
{
    private static final Logger log = LoggerFactory.getLogger(DashboardWebSocketHandler.class);

    /** 在线连接（同一用户可能多标签页，按连接维度保存） */
    private final Set<WebSocketSession> sessions = new CopyOnWriteArraySet<>();

    @Autowired
    private TokenService tokenService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception
    {
        LoginUser loginUser = tokenService.getLoginUserByToken(resolveToken(session));
        if (loginUser == null)
        {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("token invalid"));
            return;
        }
        sessions.add(session);
        log.info("工作台实时推送已连接，用户 {}，在线连接 {}", loginUser.getUsername(), sessions.size());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception
    {
        sessions.remove(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception
    {
        log.warn("工作台实时推送连接异常: {}", exception.getMessage());
        sessions.remove(session);
        if (session.isOpen())
        {
            session.close();
        }
    }

    /**
     * 向所有在线客户端广播消息
     *
     * @return 实际发送的连接数
     */
    public int broadcast(String message)
    {
        int sent = 0;
        for (WebSocketSession session : sessions)
        {
            if (!session.isOpen())
            {
                sessions.remove(session);
                continue;
            }
            try
            {
                synchronized (session)
                {
                    session.sendMessage(new TextMessage(message));
                }
                sent++;
            }
            catch (IOException e)
            {
                log.warn("工作台推送失败: {}", e.getMessage());
                sessions.remove(session);
            }
        }
        return sent;
    }

    /**
     * 当前在线连接数
     */
    public int onlineCount()
    {
        return sessions.size();
    }

    private String resolveToken(WebSocketSession session)
    {
        URI uri = session.getUri();
        if (uri == null || StringUtils.isEmpty(uri.getQuery()))
        {
            return null;
        }
        for (String pair : uri.getQuery().split("&"))
        {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2 && "token".equals(kv[0]))
            {
                return kv[1];
            }
        }
        return null;
    }
}
