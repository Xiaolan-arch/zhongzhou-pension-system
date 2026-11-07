package com.ruoyi.business.mapper;

import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.ServiceEvaluation;

/**
 * 服务评价数据层
 *
 * @author ruoyi
 */
public interface ServiceEvaluationMapper
{
    /**
     * 查询某位家属对指定服务订单的评价
     */
    public ServiceEvaluation selectByOrderAndFamily(@Param("orderId") Long orderId,
            @Param("familyId") Long familyId, @Param("tenantId") Long tenantId);

    /**
     * 新增服务评价
     */
    public int insertEvaluation(ServiceEvaluation evaluation);
}
