package com.ruoyi.business.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.ServiceOrder;

/**
 * 服务订单 数据层
 *
 * @author ruoyi
 */
public interface ServiceOrderMapper
{
    public List<ServiceOrder> selectServiceOrderList(ServiceOrder serviceOrder);

    public ServiceOrder selectServiceOrderById(Long orderId);

    public int insertServiceOrder(ServiceOrder serviceOrder);

    public int updateServiceOrder(ServiceOrder serviceOrder);

    public int deleteServiceOrderByIds(Long[] orderIds);

    public int executeOrder(@Param("orderId") Long orderId,
                            @Param("operator") String operator,
                            @Param("updateBy") String updateBy);

    public int completeOrder(@Param("orderId") Long orderId,
                             @Param("operator") String operator,
                             @Param("updateBy") String updateBy,
                             @Param("serviceRemark") String serviceRemark);

    /** 护理员视角"我的待办" */
    public List<ServiceOrder> selectMyOrders(@Param("nurseId") Long nurseId);
}
