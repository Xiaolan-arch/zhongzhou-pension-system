package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.SysTenant;

/**
 * 机构(租户) 业务层
 */
public interface ISysTenantService
{
    /**
     * 查询机构列表
     */
    public List<SysTenant> selectTenantList(SysTenant tenant);

    /**
     * 通过机构ID查询机构
     */
    public SysTenant selectTenantById(Long tenantId);

    /**
     * 新增机构
     */
    public int insertTenant(SysTenant tenant);

    /**
     * 修改机构
     */
    public int updateTenant(SysTenant tenant);

    /**
     * 批量删除机构
     */
    public int deleteTenantByIds(Long[] tenantIds);

    /**
     * 校验机构名称是否唯一
     */
    public boolean checkTenantNameUnique(SysTenant tenant);
}
