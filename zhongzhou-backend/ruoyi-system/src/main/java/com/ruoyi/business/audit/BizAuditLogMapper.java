package com.ruoyi.business.audit;

import java.util.List;

/**
 * 业务审计日志 Mapper
 */
public interface BizAuditLogMapper
{
    /** 新增业务审计日志 */
    int insertBizAuditLog(BizAuditLog auditLog);

    /** 查询业务审计日志列表（只读，审计记录不可修改删除） */
    List<BizAuditLog> selectBizAuditLogList(BizAuditLog auditLog);
}
