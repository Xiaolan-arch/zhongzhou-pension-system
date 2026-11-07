package com.ruoyi.business.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.DashboardLayout;
import com.ruoyi.business.mapper.DashboardLayoutMapper;
import com.ruoyi.business.service.IDashboardLayoutService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;

/**
 * 工作台个性化布局 服务实现
 *
 * @author ruoyi
 */
@Service
public class DashboardLayoutServiceImpl implements IDashboardLayoutService
{
    /** 布局JSON最大长度 */
    private static final int MAX_JSON_LENGTH = 2000;

    @Autowired
    private DashboardLayoutMapper layoutMapper;

    @Override
    public String getLayout()
    {
        DashboardLayout layout = layoutMapper.selectByUserId(SecurityUtils.getUserId());
        return layout == null ? null : layout.getLayoutJson();
    }

    @Override
    public void saveLayout(String layoutJson)
    {
        String json = layoutJson == null ? "" : layoutJson.trim();
        if (json.isEmpty())
        {
            throw new ServiceException("布局数据不能为空");
        }
        if (json.length() > MAX_JSON_LENGTH)
        {
            throw new ServiceException("布局数据超出长度限制");
        }
        if (!json.startsWith("[") || !json.endsWith("]"))
        {
            throw new ServiceException("布局数据格式不正确");
        }
        DashboardLayout layout = new DashboardLayout();
        layout.setUserId(SecurityUtils.getUserId());
        layout.setTenantId(SecurityUtils.getTenantId());
        layout.setLayoutJson(json);
        layoutMapper.upsert(layout);
    }
}
