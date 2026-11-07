package com.ruoyi.business.mapper;

import com.ruoyi.business.domain.BillItem;

/**
 * 账单明细 数据层
 *
 * @author ruoyi
 */
public interface BillItemMapper
{
    /**
     * 新增账单明细
     *
     * @param billItem 账单明细
     * @return 结果
     */
    public int insertBillItem(BillItem billItem);
}
