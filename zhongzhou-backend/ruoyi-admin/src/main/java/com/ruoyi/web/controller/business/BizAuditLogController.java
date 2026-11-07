package com.ruoyi.web.controller.business;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.audit.BizAuditLog;
import com.ruoyi.business.audit.BizAuditLogMapper;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 业务审计日志 Controller（只读）
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/business/auditLog")
public class BizAuditLogController extends BaseController
{
    @Autowired
    private BizAuditLogMapper bizAuditLogMapper;

    /** 查询业务审计日志列表 */
    @PreAuthorize("@ss.hasPermi('business:auditLog:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizAuditLog bizAuditLog)
    {
        startPage();
        List<BizAuditLog> list = bizAuditLogMapper.selectBizAuditLogList(bizAuditLog);
        return getDataTable(list);
    }
}
