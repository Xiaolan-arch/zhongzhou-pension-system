package com.ruoyi.business.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.ServiceItem;
import com.ruoyi.business.mapper.ServiceItemMapper;
import com.ruoyi.business.service.IServiceItemService;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;

/**
 * 服务项目 服务层处理
 *
 * @author ruoyi
 */
@Service
public class ServiceItemServiceImpl implements IServiceItemService
{
    @Autowired
    private ServiceItemMapper serviceItemMapper;

    @Override
    public List<ServiceItem> selectServiceItemList(ServiceItem serviceItem)
    {
        return serviceItemMapper.selectServiceItemList(serviceItem);
    }

    @Override
    public ServiceItem selectServiceItemById(Long itemId)
    {
        return serviceItemMapper.selectServiceItemById(itemId);
    }

    @Override
    public int insertServiceItem(ServiceItem serviceItem)
    {
        if (StringUtils.isNull(serviceItem.getTenantId()))
        {
            serviceItem.setTenantId(SecurityUtils.getTenantId());
        }
        if (StringUtils.isEmpty(serviceItem.getCreateBy()))
        {
            serviceItem.setCreateBy(SecurityUtils.getUsername());
        }
        serviceItem.setCreateTime(DateUtils.getNowDate());
        return serviceItemMapper.insertServiceItem(serviceItem);
    }

    @Override
    public int updateServiceItem(ServiceItem serviceItem)
    {
        serviceItem.setUpdateBy(SecurityUtils.getUsername());
        serviceItem.setUpdateTime(DateUtils.getNowDate());
        return serviceItemMapper.updateServiceItem(serviceItem);
    }

    @Override
    public int deleteServiceItemByIds(Long[] itemIds)
    {
        return serviceItemMapper.deleteServiceItemByIds(itemIds);
    }
}
