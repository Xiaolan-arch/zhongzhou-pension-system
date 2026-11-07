package com.ruoyi.business.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.PaymentRecord;

/**
 * 收款记录 Mapper
 */
public interface PaymentRecordMapper
{
    /** 按ID查询 */
    PaymentRecord selectPaymentRecordById(Long recordId);

    /** 按账单ID查询收款记录列表（时间倒序） */
    List<PaymentRecord> selectPaymentRecordsByBillId(Long billId);

    /** 按账单ID + 机构ID查询收款记录（家属端无登录上下文，需显式限定机构） */
    List<PaymentRecord> selectPaymentRecordsByBillIdAndTenant(@Param("billId") Long billId, @Param("tenantId") Long tenantId);

    /** 按老人ID查询收款记录列表 */
    List<PaymentRecord> selectPaymentRecordsByElderId(Long elderId);

    /** 新增收款记录 */
    int insertPaymentRecord(PaymentRecord record);
}
