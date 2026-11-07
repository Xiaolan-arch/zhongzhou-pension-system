package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.ServiceItem;

/**
 * 服务项目 服务层
 *
 * @author ruoyi
 */
public interface IServiceItemService
{
    public List<ServiceItem> selectServiceItemList(ServiceItem serviceItem);

    public ServiceItem selectServiceItemById(Long itemId);

    public int insertServiceItem(ServiceItem serviceItem);

    public int updateServiceItem(ServiceItem serviceItem);

    public int deleteServiceItemByIds(Long[] itemIds);
}
