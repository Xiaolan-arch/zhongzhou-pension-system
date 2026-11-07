package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.ServiceOrder;

/**
 * 服务订单 服务层
 *
 * @author ruoyi
 */
public interface IServiceOrderService
{
    public List<ServiceOrder> selectServiceOrderList(ServiceOrder serviceOrder);

    public ServiceOrder selectServiceOrderById(Long orderId);

    public int insertServiceOrder(ServiceOrder serviceOrder);

    public int updateServiceOrder(ServiceOrder serviceOrder);

    public int deleteServiceOrderByIds(Long[] orderIds);

    public int executeOrder(Long orderId);

    /** 完成订单，可带 serviceRemark 服务备注 */
    public int completeOrder(Long orderId, String serviceRemark);

    /** 护理员"我的待办"（admin 返回全部） */
    public List<ServiceOrder> selectMyOrders();
}
