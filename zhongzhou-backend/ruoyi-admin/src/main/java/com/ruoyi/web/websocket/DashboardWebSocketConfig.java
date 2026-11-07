package com.ruoyi.web.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * 工作台 WebSocket 配置
 *
 * @author ruoyi
 */
@Configuration
@EnableWebSocket
@EnableScheduling
public class DashboardWebSocketConfig implements WebSocketConfigurer
{
    @Autowired
    private DashboardWebSocketHandler dashboardWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry)
    {
        registry.addHandler(dashboardWebSocketHandler, "/ws/dashboard").setAllowedOriginPatterns("*");
    }
}
