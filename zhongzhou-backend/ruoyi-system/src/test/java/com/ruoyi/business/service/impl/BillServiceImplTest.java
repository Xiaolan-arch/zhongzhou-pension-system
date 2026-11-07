package com.ruoyi.business.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.ruoyi.business.domain.Bill;
import com.ruoyi.business.mapper.BillItemMapper;
import com.ruoyi.business.mapper.BillMapper;
import com.ruoyi.common.exception.ServiceException;

/**
 * 账单服务：月度账单参数校验与缴费状态校验
 */
public class BillServiceImplTest extends BusinessTestSupport
{
    private BillServiceImpl service;

    @Before
    public void setUp()
    {
        loginAs(2L, 1L, "admin");
        service = new BillServiceImpl();
        StubHandler empty = new StubHandler() {
            @Override
            public Object handle(String methodName, Object[] args)
            {
                return NOT_STUBBED;
            }
        };
        inject(service, "billMapper", stub(BillMapper.class, empty));
        inject(service, "billItemMapper", stub(BillItemMapper.class, empty));
    }

    @After
    public void tearDown()
    {
        logout();
    }

    @Test
    public void generateBillRejectsInvalidMonth()
    {
        try
        {
            service.generateBill("2026-13");
            fail("非法月份应当抛出异常");
        }
        catch (ServiceException e)
        {
            assertEquals("账单月份格式不正确", e.getMessage());
        }
    }

    @Test
    public void payBillRejectsPaidBill()
    {
        final Bill current = new Bill();
        current.setBillId(3L);
        current.setPayStatus("1");
        StubHandler handler = new StubHandler() {
            @Override
            public Object handle(String methodName, Object[] args)
            {
                return "selectBillById".equals(methodName) ? current : NOT_STUBBED;
            }
        };
        inject(service, "billMapper", stub(BillMapper.class, handler));

        Bill request = new Bill();
        request.setBillId(3L);
        try
        {
            service.payBill(request);
            fail("已支付账单不应重复支付");
        }
        catch (ServiceException e)
        {
            assertEquals("账单已支付，请勿重复操作", e.getMessage());
        }
    }

    @Test
    public void payBillRejectsConcurrentStatusChange()
    {
        final Bill current = new Bill();
        current.setBillId(3L);
        current.setPayStatus("0");
        StubHandler handler = new StubHandler() {
            @Override
            public Object handle(String methodName, Object[] args)
            {
                if ("selectBillById".equals(methodName))
                {
                    return current;
                }
                if ("updateBillPay".equals(methodName))
                {
                    return 0;
                }
                return NOT_STUBBED;
            }
        };
        inject(service, "billMapper", stub(BillMapper.class, handler));

        Bill request = new Bill();
        request.setBillId(3L);
        try
        {
            service.payBill(request);
            fail("条件更新失败时应当抛出异常");
        }
        catch (ServiceException e)
        {
            assertEquals("账单状态已变更，请刷新后重试", e.getMessage());
        }
    }
}
