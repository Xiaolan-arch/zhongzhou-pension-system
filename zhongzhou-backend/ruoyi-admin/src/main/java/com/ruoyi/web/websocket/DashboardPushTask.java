package com.ruoyi.web.websocket;

import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 工作台实时刷新任务：先清理 Redis 缓存再向在线客户端广播刷新通知
 *
 * 缓存策略：DashboardServiceImpl.summary/trend/distribution/todo 四个核心接口
 * 会把聚合结果预写入 Redis（key 前缀 dashboard:cache:{scope}:{method}），
 * TTL 90 秒。定时任务每 30 秒清空 dashboard:cache:* 让下次请求重新走 DB，
 * 然后广播通知前端 reload。广播只做触发，不推送数据本身。
 *
 * @author ruoyi
 */
@Component
public class DashboardPushTask
{
    private static final Logger log = LoggerFactory.getLogger(DashboardPushTask.class);

    /** 缓存 key 前缀，与 DashboardServiceImpl.CACHE_PREFIX 保持一致 */
    private static final String CACHE_PREFIX = "dashboard:cache:";

    @Autowired
    private DashboardWebSocketHandler dashboardWebSocketHandler;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 每 30 秒：清理 dashboard 缓存 → 广播刷新通知（有在线连接时才推送）
     */
    @Scheduled(fixedDelay = 30000, initialDelay = 30000)
    public void pushRefresh()
    {
        evictAllDashboardCaches();

        int online = dashboardWebSocketHandler.onlineCount();
        if (online == 0)
        {
            return;
        }
        int sent = dashboardWebSocketHandler.broadcast(buildRefreshMessage());
        log.info("工作台缓存刷新 + 广播，在线 {} 发送 {}", online, sent);
    }

    /** 清空所有 scope 的 dashboard 缓存（不依赖登录上下文） */
    private void evictAllDashboardCaches()
    {
        try
        {
            Collection<String> keys = stringRedisTemplate.keys(CACHE_PREFIX + "*");
            if (keys != null && !keys.isEmpty())
            {
                stringRedisTemplate.delete(keys);
            }
        }
        catch (Exception e)
        {
            log.warn("Dashboard 缓存清理失败：{}", e.getMessage());
        }
    }

    /**
     * 刷新通知报文
     */
    public static String buildRefreshMessage()
    {
        return "{\"type\":\"dashboard-refresh\",\"timestamp\":" + System.currentTimeMillis() + "}";
    }
}
