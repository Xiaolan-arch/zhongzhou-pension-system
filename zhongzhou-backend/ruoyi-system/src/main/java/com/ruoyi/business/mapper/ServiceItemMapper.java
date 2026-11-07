package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.ServiceItem;

/**
 * 服务项目 数据层
 *
 * @author ruoyi
 */
public interface ServiceItemMapper
{
    public List<ServiceItem> selectServiceItemList(ServiceItem serviceItem);

    public ServiceItem selectServiceItemById(Long itemId);

    public int insertServiceItem(ServiceItem serviceItem);

    public int updateServiceItem(ServiceItem serviceItem);

    public int deleteServiceItemByIds(Long[] itemIds);
}
