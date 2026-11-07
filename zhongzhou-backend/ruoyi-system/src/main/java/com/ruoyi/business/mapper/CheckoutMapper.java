package com.ruoyi.business.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.Checkout;

/**
 * 退住申请 数据层
 *
 * @author ruoyi
 */
public interface CheckoutMapper
{
    /**
     * 查询退住申请列表
     *
     * @param checkout 退住申请
     * @return 退住申请集合
     */
    public List<Checkout> selectCheckoutList(Checkout checkout);

    /**
     * 通过退住申请ID查询退住申请
     *
     * @param checkoutId 退住申请ID
     * @return 退住申请
     */
    public Checkout selectCheckoutById(Long checkoutId);

    /**
     * 新增退住申请
     *
     * @param checkout 退住申请
     * @return 结果
     */
    public int insertCheckout(Checkout checkout);

    /**
     * 保存当前步骤表单字段
     *
     * @param checkout 退住申请
     * @return 结果
     */
    public int updateCheckout(Checkout checkout);

    /**
     * 条件更新流程状态
     *
     * @param checkoutId 退住申请ID
     * @param oldStatus 期望当前状态
     * @param newStatus 目标状态
     * @param updateBy 更新者
     * @return 结果
     */
    public int updateCheckoutStatus(@Param("checkoutId") Long checkoutId, @Param("oldStatus") String oldStatus,
            @Param("newStatus") String newStatus, @Param("updateBy") String updateBy);

    /**
     * 完成退住（状态 6 -> 7，写入结算信息）
     *
     * @param checkout 退住申请
     * @return 结果
     */
    public int updateCheckoutComplete(Checkout checkout);

    /**
     * 批量逻辑删除退住申请
     *
     * @param checkoutIds 退住申请ID数组
     * @return 结果
     */
    public int deleteCheckoutByIds(Long[] checkoutIds);

    /**
     * 查询老人姓名
     *
     * @param elderId 老人ID
     * @param tenantId 机构ID
     * @return 老人姓名
     */
    public String selectElderNameById(@Param("elderId") Long elderId, @Param("tenantId") Long tenantId);
}
