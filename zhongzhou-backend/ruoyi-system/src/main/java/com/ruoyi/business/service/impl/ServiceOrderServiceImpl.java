package com.ruoyi.business.service.impl;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.domain.ServiceOrder;
import com.ruoyi.business.domain.ServiceTask;
import com.ruoyi.business.mapper.ServiceOrderMapper;
import com.ruoyi.business.service.IServiceOrderService;
import com.ruoyi.business.service.IServiceTaskService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;

/**
 * 服务订单 服务层处理
 *
 * @author ruoyi
 */
@Service
public class ServiceOrderServiceImpl implements IServiceOrderService
{
    @Autowired
    private ServiceOrderMapper serviceOrderMapper;

    @Autowired
    private IServiceTaskService serviceTaskService;

    @Override
    public List<ServiceOrder> selectServiceOrderList(ServiceOrder serviceOrder)
    {
        return serviceOrderMapper.selectServiceOrderList(serviceOrder);
    }

    @Override
    public ServiceOrder selectServiceOrderById(Long orderId)
    {
        return serviceOrderMapper.selectServiceOrderById(orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertServiceOrder(ServiceOrder serviceOrder)
    {
        if (StringUtils.isNull(serviceOrder.getTenantId()))
        {
            serviceOrder.setTenantId(SecurityUtils.getTenantId());
        }
        if (StringUtils.isEmpty(serviceOrder.getCreateBy()))
        {
            serviceOrder.setCreateBy(SecurityUtils.getUsername());
        }
        serviceOrder.setCreateTime(DateUtils.getNowDate());
        if (StringUtils.isEmpty(serviceOrder.getOrderNo()))
        {
            serviceOrder.setOrderNo("FW" + DateUtils.dateTimeNow()
                    + String.format("%03d", ThreadLocalRandom.current().nextInt(1000)));
        }
        int rows = serviceOrderMapper.insertServiceOrder(serviceOrder);
        // 订单创建成功后，同一事务内自动派发护理任务；已完成/已取消订单不派发
        if (rows > 0 && !"2".equals(serviceOrder.getStatus()) && !"3".equals(serviceOrder.getStatus()))
        {
            createServiceTask(serviceOrder);
        }
        return rows;
    }

    /**
     * 根据订单信息自动生成护理任务（状态=待执行）
     *
     * 订单有服务时间：按服务时间派发，服务日期取服务时间当天；
     * 订单无服务时间：生成一条服务日期为当天的通用任务（不设计划时间，避免被误判逾期）。
     */
    private void createServiceTask(ServiceOrder order)
    {
        ServiceTask task = new ServiceTask();
        task.setOrderId(order.getOrderId());
        task.setOrderNo(order.getOrderNo());
        task.setElderId(order.getElderId());
        task.setElderName(order.getElderName());
        task.setItemId(order.getItemId());
        task.setItemName(order.getItemName());
        task.setNurseId(order.getNurseId());
        task.setNurseName(order.getNurseName());
        Date planTime = order.getServiceTime();
        if (planTime != null)
        {
            task.setPlanTime(planTime);
            task.setServiceDate(DateUtils.parseDate(DateUtils.parseDateToStr("yyyy-MM-dd", planTime)));
        }
        else
        {
            task.setServiceDate(DateUtils.parseDate(DateUtils.getDate()));
        }
        task.setTaskStatus("0");
        task.setTenantId(order.getTenantId());
        task.setCreateBy(order.getCreateBy());
        task.setRemark(order.getRemark());
        serviceTaskService.insertServiceTask(task);
    }

    @Override
    public int updateServiceOrder(ServiceOrder serviceOrder)
    {
        serviceOrder.setUpdateBy(SecurityUtils.getUsername());
        serviceOrder.setUpdateTime(DateUtils.getNowDate());
        return serviceOrderMapper.updateServiceOrder(serviceOrder);
    }

    @Override
    public int deleteServiceOrderByIds(Long[] orderIds)
    {
        return serviceOrderMapper.deleteServiceOrderByIds(orderIds);
    }

    /**
     * 执行订单（签到打卡）：待执行(0) → 已执行(1)
     *
     * 自动填 checkin_time / checkin_operator
     * 只有被指派的护理员本人或管理员可以签到
     */
    @Override
    public int executeOrder(Long orderId)
    {
        ServiceOrder order = serviceOrderMapper.selectServiceOrderById(orderId);
        if (order == null)
        {
            throw new ServiceException("服务订单不存在");
        }
        if (!"0".equals(order.getStatus()))
        {
            throw new ServiceException("仅【待执行】订单可以签到（当前状态：" + order.getStatus() + "）");
        }
        String username = SecurityUtils.getUsername();
        Long userId = SecurityUtils.getUserId();
        // 护理员身份校验：如果有指派 nurseId，当前登录用户不是 nurseId 也不是管理员 → 拒绝
        if (order.getNurseId() != null && !order.getNurseId().equals(userId))
        {
            // 放行条件：有 admin 或 system 角色
            List<com.ruoyi.common.core.domain.entity.SysRole> roles = SecurityUtils.getLoginUser().getUser().getRoles();
            boolean isAdmin = roles.stream().anyMatch(r -> "admin".equals(r.getRoleKey()) || "system".equals(r.getRoleKey()));
            if (!isAdmin)
            {
                throw new ServiceException("该订单指派给其他护理员，不能代签");
            }
        }
        int rows = serviceOrderMapper.executeOrder(orderId, username, username);
        if (rows == 0)
        {
            throw new ServiceException("状态已变更，请刷新后重试");
        }
        return rows;
    }

    /**
     * 完成订单（提交服务记录）：已执行(1) → 已完成(2)
     *
     * 自动填 finish_time / finish_operator，可带 serviceRemark
     */
    @Override
    public int completeOrder(Long orderId, String serviceRemark)
    {
        ServiceOrder order = serviceOrderMapper.selectServiceOrderById(orderId);
        if (order == null)
        {
            throw new ServiceException("服务订单不存在");
        }
        if (!"1".equals(order.getStatus()))
        {
            throw new ServiceException("仅【已执行】订单可以完成（当前状态：" + order.getStatus() + "）");
        }
        String username = SecurityUtils.getUsername();
        int rows = serviceOrderMapper.completeOrder(orderId, username, username, serviceRemark);
        if (rows == 0)
        {
            throw new ServiceException("状态已变更，请刷新后重试");
        }
        return rows;
    }

    /**
     * 护理员"我的待办"：当前登录用户作为 nurse 且 status in (0,1) 的订单
     * 管理员角色：返回全部（便于管理端查看）
     */
    @Override
    public List<ServiceOrder> selectMyOrders()
    {
        String username = SecurityUtils.getUsername();
        Long userId = SecurityUtils.getUserId();
        List<com.ruoyi.common.core.domain.entity.SysRole> roles = SecurityUtils.getLoginUser().getUser().getRoles();
        boolean isAdmin = roles.stream().anyMatch(r -> "admin".equals(r.getRoleKey()) || "system".equals(r.getRoleKey()));
        if (isAdmin)
        {
            return serviceOrderMapper.selectServiceOrderList(new ServiceOrder());
        }
        return serviceOrderMapper.selectMyOrders(userId);
    }
}
