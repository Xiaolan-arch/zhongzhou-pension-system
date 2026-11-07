package com.ruoyi.business.service.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.domain.Checkout;
import com.ruoyi.business.mapper.BillMapper;
import com.ruoyi.business.mapper.CheckinBedMapper;
import com.ruoyi.business.mapper.CheckoutMapper;
import com.ruoyi.business.service.ICheckoutService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;

/**
 * 退住申请 服务层处理
 *
 * @author ruoyi
 */
@Service
public class CheckoutServiceImpl implements ICheckoutService
{
    @Autowired
    private CheckoutMapper checkoutMapper;

    @Autowired
    private CheckinBedMapper checkinBedMapper;

    @Autowired
    private BillMapper billMapper;

    @Override
    public List<Checkout> selectCheckoutList(Checkout checkout)
    {
        return checkoutMapper.selectCheckoutList(checkout);
    }

    @Override
    public Checkout selectCheckoutById(Long checkoutId)
    {
        return checkoutMapper.selectCheckoutById(checkoutId);
    }

    @Override
    public int insertCheckout(Checkout checkout)
    {
        if (checkout == null || checkout.getElderId() == null)
        {
            throw new ServiceException("老人ID不能为空");
        }
        Long tenantId = SecurityUtils.getTenantId();
        String elderName = checkoutMapper.selectElderNameById(checkout.getElderId(), tenantId);
        if (elderName == null)
        {
            throw new ServiceException("老人不存在");
        }
        checkout.setApplyNo("TZ" + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()));
        checkout.setApplyStatus("0");
        checkout.setElderName(elderName);
        checkout.setTenantId(tenantId);
        checkout.setCreateBy(SecurityUtils.getUsername());
        return checkoutMapper.insertCheckout(checkout);
    }

    @Override
    public int updateCheckout(Checkout checkout)
    {
        checkout.setUpdateBy(SecurityUtils.getUsername());
        return checkoutMapper.updateCheckout(checkout);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int submitStep(Long checkoutId, Integer step, Checkout checkout)
    {
        if (step == null || step < 1 || step > 6)
        {
            throw new ServiceException("步骤参数错误");
        }
        Checkout current = checkoutMapper.selectCheckoutById(checkoutId);
        if (current == null)
        {
            throw new ServiceException("退住申请不存在");
        }
        String expectStatus = String.valueOf(step - 1);
        if (!expectStatus.equals(current.getApplyStatus()))
        {
            throw new ServiceException("状态已变更，请刷新后重试");
        }
        String username = SecurityUtils.getUsername();
        Date now = new Date();
        checkout.setCheckoutId(checkoutId);
        checkout.setUpdateBy(username);
        if ("1".equals(checkout.getAudit1Result()))
        {
            checkout.setAudit1By(username);
            checkout.setAudit1Time(now);
        }
        if ("1".equals(checkout.getAudit2Result()))
        {
            checkout.setAudit2By(username);
            checkout.setAudit2Time(now);
        }
        if ("1".equals(checkout.getAudit3Result()))
        {
            checkout.setAudit3By(username);
            checkout.setAudit3Time(now);
        }
        checkoutMapper.updateCheckout(checkout);

        boolean rejected = "1".equals(checkout.getAudit1Result())
                || "1".equals(checkout.getAudit2Result())
                || "1".equals(checkout.getAudit3Result());
        String newStatus = rejected ? "9" : String.valueOf(step);
        int rows = checkoutMapper.updateCheckoutStatus(checkoutId, current.getApplyStatus(), newStatus, username);
        if (rows == 0)
        {
            throw new ServiceException("状态已变更，请刷新后重试");
        }
        return 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int complete(Long checkoutId, BigDecimal settleAmount, String refundMethod)
    {
        Checkout checkout = checkoutMapper.selectCheckoutById(checkoutId);
        if (checkout == null)
        {
            throw new ServiceException("退住申请不存在");
        }
        if (!"6".equals(checkout.getApplyStatus()))
        {
            throw new ServiceException("当前状态不允许完成退住");
        }
        String username = SecurityUtils.getUsername();
        int unpaid = billMapper.selectUnpaidCountByElder(checkout.getElderId(), checkout.getTenantId());
        if (unpaid > 0)
        {
            throw new ServiceException("该老人存在未结清账单，请先完成缴费");
        }
        checkinBedMapper.releaseBedByElder(checkout.getElderId(), checkout.getTenantId());
        checkinBedMapper.updateElderStatus(checkout.getElderId(), "1", checkout.getTenantId());

        Checkout update = new Checkout();
        update.setCheckoutId(checkoutId);
        update.setSettleAmount(settleAmount == null ? BigDecimal.ZERO : settleAmount);
        update.setRefundMethod(refundMethod);
        update.setUpdateBy(username);
        int rows = checkoutMapper.updateCheckoutComplete(update);
        if (rows == 0)
        {
            throw new ServiceException("状态已变更，请刷新后重试");
        }
        return 1;
    }

    @Override
    public int deleteCheckoutByIds(Long[] checkoutIds)
    {
        return checkoutMapper.deleteCheckoutByIds(checkoutIds);
    }
}
