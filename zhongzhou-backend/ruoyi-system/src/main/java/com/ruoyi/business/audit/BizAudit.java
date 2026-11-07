package com.ruoyi.business.audit;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 业务关键操作审计注解
 *
 * 标注在业务关键方法（收款、退款、入住、退住等）上，
 * 方法正常返回后由 BizAuditAspect 写入一条 biz_audit_log 审计记录。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BizAudit
{
    /** 业务类型（如 BILL_PAY / BILL_REFUND / CHECKIN / CHECKOUT） */
    String value() default "";

    /** 动作描述（如 账单收款 / 账单退款） */
    String action() default "";
}
