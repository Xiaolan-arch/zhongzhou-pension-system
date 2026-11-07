package com.ruoyi.business.service;

import java.math.BigDecimal;
import java.util.List;
import com.ruoyi.business.domain.Checkout;

/**
 * 退住申请 服务层
 *
 * @author ruoyi
 */
public interface ICheckoutService
{
    /**
     * 查询退住申请列表
     *
     * @param checkout 退住申请
     * @return 退住申请集合
     */
    public List<Checkout> selectCheckoutList(Checkout checkout);

    /**
     * 查询退住申请详情
     *
     * @param checkoutId 退住申请ID
     * @return 退住申请
     */
    public Checkout selectCheckoutById(Long checkoutId);

    /**
     * 创建退住申请
     *
     * @param checkout 退住申请
     * @return 结果
     */
    public int insertCheckout(Checkout checkout);

    /**
     * 保存当前步骤表单字段（不改变流程状态）
     *
     * @param checkout 退住申请
     * @return 结果
     */
    public int updateCheckout(Checkout checkout);

    /**
     * 提交向导步骤
     *
     * @param checkoutId 退住申请ID
     * @param step 步骤（1-6）
     * @param checkout 表单数据
     * @return 结果
     */
    public int submitStep(Long checkoutId, Integer step, Checkout checkout);

    /**
     * 完成退住
     *
     * @param checkoutId 退住申请ID
     * @param settleAmount 结算金额
     * @param refundMethod 退款方式
     * @return 结果
     */
    public int complete(Long checkoutId, BigDecimal settleAmount, String refundMethod);

    /**
     * 批量删除退住申请
     *
     * @param checkoutIds 退住申请ID数组
     * @return 结果
     */
    public int deleteCheckoutByIds(Long[] checkoutIds);
}
