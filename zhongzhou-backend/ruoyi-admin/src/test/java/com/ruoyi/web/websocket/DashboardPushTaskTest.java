package com.ruoyi.web.websocket;

import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * 工作台实时推送报文单元测试
 */
public class DashboardPushTaskTest
{
    @Test
    public void refreshMessageContainsTypeAndTimestamp()
    {
        String message = DashboardPushTask.buildRefreshMessage();
        assertTrue(message.contains("\"type\":\"dashboard-refresh\""));
        assertTrue(message.contains("\"timestamp\":"));
        assertTrue(message.startsWith("{") && message.endsWith("}"));
    }
}
