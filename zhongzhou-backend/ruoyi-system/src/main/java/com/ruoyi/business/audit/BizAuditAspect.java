package com.ruoyi.business.audit;

import java.util.Date;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.business.domain.Bill;
import com.ruoyi.common.utils.SecurityUtils;

/**
 * 业务关键操作审计切面
 *
 * 拦截标注 @BizAudit 的方法，在方法正常返回后写入一条 biz_audit_log 审计记录。
 * 审计写入失败不影响业务主流程（异常仅记录日志）。
 */
@Aspect
@Component
public class BizAuditAspect
{
    private static final Logger log = LoggerFactory.getLogger(BizAuditAspect.class);

    /** JSON 序列化工具（线程安全，可复用） */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private BizAuditLogMapper bizAuditLogMapper;

    @AfterReturning(pointcut = "@annotation(bizAudit)")
    public void doAfterReturning(JoinPoint joinPoint, BizAudit bizAudit)
    {
        try
        {
            BizAuditLog auditLog = new BizAuditLog();
            auditLog.setBizType(bizAudit.value());
            auditLog.setAction(bizAudit.action());
            auditLog.setBizId(resolveBizId(joinPoint.getArgs()));
            // 变更前快照暂不采集，保留为空
            auditLog.setBeforeJson(null);
            // 变更后快照：序列化方法入参
            auditLog.setAfterJson(toJson(joinPoint.getArgs()));
            auditLog.setOperatorId(resolveOperatorId());
            auditLog.setOperatorName(resolveOperatorName());
            // 未登录上下文（如家属端）取不到机构时记为 0（未知机构），避免审计记录丢失
            Long tenantId = SecurityUtils.getTenantId();
            auditLog.setTenantId(tenantId == null ? 0L : tenantId);
            auditLog.setCreateTime(new Date());
            bizAuditLogMapper.insertBizAuditLog(auditLog);
        }
        catch (Exception e)
        {
            log.warn("业务审计日志写入失败: {}", e.getMessage());
        }
    }

    /**
     * 尽量从方法参数中获取业务主键：优先取第一个参数 Bill 的 billId，
     * 其次取第一个参数为数值类型（如退款方法的账单ID），取不到记 0。
     */
    private Long resolveBizId(Object[] args)
    {
        if (args == null || args.length == 0)
        {
            return 0L;
        }
        Object first = args[0];
        if (first instanceof Bill && ((Bill) first).getBillId() != null)
        {
            return ((Bill) first).getBillId();
        }
        if (first instanceof Long)
        {
            return (Long) first;
        }
        return 0L;
    }

    /**
     * 序列化方法入参，异常时忽略（返回 null）
     */
    private String toJson(Object[] args)
    {
        try
        {
            return OBJECT_MAPPER.writeValueAsString(args);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /**
     * 获取操作人ID（家属端等无登录上下文时兜底返回 null）
     */
    private Long resolveOperatorId()
    {
        try
        {
            return SecurityUtils.getUserId();
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /**
     * 获取操作人姓名（家属端等无登录上下文时兜底返回 null）
     */
    private String resolveOperatorName()
    {
        try
        {
            return SecurityUtils.getUsername();
        }
        catch (Exception e)
        {
            return null;
        }
    }
}
