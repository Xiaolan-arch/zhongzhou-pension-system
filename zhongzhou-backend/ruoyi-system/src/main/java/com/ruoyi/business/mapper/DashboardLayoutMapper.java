package com.ruoyi.business.mapper;

import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.DashboardLayout;

/**
 * 工作台个性化布局 Mapper
 *
 * @author ruoyi
 */
public interface DashboardLayoutMapper
{
    /**
     * 查询指定用户的布局配置
     */
    DashboardLayout selectByUserId(@Param("userId") Long userId);

    /**
     * 新增或更新用户布局（user_id 唯一）
     */
    int upsert(DashboardLayout layout);
}
