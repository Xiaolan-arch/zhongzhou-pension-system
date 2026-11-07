package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.SysTenant;
import com.ruoyi.system.mapper.SysTenantMapper;
import com.ruoyi.system.service.ISysTenantService;

/**
 * 机构(租户) 业务层处理
 */
@Service
public class SysTenantServiceImpl implements ISysTenantService
{
    @Autowired
    private SysTenantMapper tenantMapper;

    @Override
    public List<SysTenant> selectTenantList(SysTenant tenant)
    {
        return tenantMapper.selectTenantList(tenant);
    }

    @Override
    public SysTenant selectTenantById(Long tenantId)
    {
        return tenantMapper.selectTenantById(tenantId);
    }

    @Override
    public int insertTenant(SysTenant tenant)
    {
        if (StringUtils.isNull(tenant.getStatus()))
        {
            tenant.setStatus("0");
        }
        tenant.setCreateBy(SecurityUtils.getUsername());
        tenant.setCreateTime(DateUtils.getNowDate());
        return tenantMapper.insertTenant(tenant);
    }

    @Override
    public int updateTenant(SysTenant tenant)
    {
        tenant.setUpdateBy(SecurityUtils.getUsername());
        tenant.setUpdateTime(DateUtils.getNowDate());
        return tenantMapper.updateTenant(tenant);
    }

    @Override
    public int deleteTenantByIds(Long[] tenantIds)
    {
        return tenantMapper.deleteTenantByIds(tenantIds);
    }

    @Override
    public boolean checkTenantNameUnique(SysTenant tenant)
    {
        Long tenantId = StringUtils.isNull(tenant.getTenantId()) ? -1L : tenant.getTenantId();
        SysTenant info = tenantMapper.checkTenantNameUnique(tenant.getTenantName());
        if (StringUtils.isNotNull(info) && info.getTenantId().longValue() != tenantId.longValue())
        {
            return false;
        }
        return true;
    }
}
