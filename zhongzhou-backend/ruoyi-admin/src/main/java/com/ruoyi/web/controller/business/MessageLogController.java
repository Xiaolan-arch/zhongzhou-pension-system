package com.ruoyi.web.controller.business;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.domain.MessageLog;
import com.ruoyi.business.mapper.MessageLogMapper;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 微信订阅消息发送记录 控制器
 */
@RestController
@RequestMapping("/business/message")
public class MessageLogController extends BaseController
{
    @Autowired
    private MessageLogMapper messageLogMapper;

    /** 查询消息通知记录列表 */
    @PreAuthorize("@ss.hasPermi('business:message:list')")
    @GetMapping("/list")
    public TableDataInfo list(MessageLog log)
    {
        startPage();
        List<MessageLog> list = messageLogMapper.selectMessageLogList(log);
        return getDataTable(list);
    }
}
