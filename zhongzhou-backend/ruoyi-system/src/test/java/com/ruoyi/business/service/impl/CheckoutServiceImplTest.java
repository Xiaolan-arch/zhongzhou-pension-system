package com.ruoyi.business.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import java.math.BigDecimal;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.ruoyi.business.domain.Checkout;
import com.ruoyi.business.mapper.BillMapper;
import com.ruoyi.business.mapper.CheckinBedMapper;
import com.ruoyi.business.mapper.CheckoutMapper;
import com.ruoyi.common.exception.ServiceException;

/**
 * 退住办理服务：账单结清校验与床位释放
 */
public class CheckoutServiceImplTest extends BusinessTestSupport
{
    private CheckoutServiceImpl service;

    @Before
    public void setUp()
    {
        loginAs(2L, 1L, "admin");
        service = new CheckoutServiceImpl();
    }

    @After
    public void tearDown()
    {
        logout();
    }

    private void bind(final Checkout current, final int unpaid, final Checkout[] completed, final int updateRows)
    {
        StubHandler checkoutHandler = new StubHandler() {
            @Override
            public Object handle(String methodName, Object[] args)
            {
                if ("selectCheckoutById".equals(methodName))
                {
                    return current;
                }
                if ("updateCheckoutComplete".equals(methodName))
                {
                    completed[0] = (Checkout) args[0];
                    return updateRows;
                }
                return NOT_STUBBED;
            }
        };
        StubHandler bedHandler = new StubHandler() {
            @Override
            public Object handle(String methodName, Object[] args)
            {
                return "releaseBedByElder".equals(methodName) || "updateElderStatus".equals(methodName) ? 1
                        : NOT_STUBBED;
            }
        };
        StubHandler billHandler = new StubHandler() {
            @Override
            public Object handle(String methodName, Object[] args)
            {
                return "selectUnpaidCountByElder".equals(methodName) ? unpaid : NOT_STUBBED;
            }
        };
        inject(service, "checkoutMapper", stub(CheckoutMapper.class, checkoutHandler));
        inject(service, "checkinBedMapper", stub(CheckinBedMapper.class, bedHandler));
        inject(service, "billMapper", stub(BillMapper.class, billHandler));
    }

    @Test
    public void completeRejectsUnpaidBills()
    {
        Checkout current = new Checkout();
        current.setApplyStatus("6");
        current.setElderId(101L);
        current.setTenantId(1L);
        bind(current, 2, new Checkout[1], 1);
        try
        {
            service.complete(1L, new BigDecimal("500.00"), "CASH");
            fail("存在未结清账单时应当抛出异常");
        }
        catch (ServiceException e)
        {
            assertEquals("该老人存在未结清账单，请先完成缴费", e.getMessage());
        }
    }

    @Test
    public void completeReleasesBedAndSettles()
    {
        Checkout current = new Checkout();
        current.setApplyStatus("6");
        current.setElderId(101L);
        current.setTenantId(1L);
        final Checkout[] completed = new Checkout[1];
        bind(current, 0, completed, 1);

        int rows = service.complete(1L, new BigDecimal("500.00"), "CASH");

        assertEquals(1, rows);
        assertEquals(new BigDecimal("500.00"), completed[0].getSettleAmount());
        assertEquals("CASH", completed[0].getRefundMethod());
    }

    @Test
    public void completeRejectsUnexpectedStatus()
    {
        Checkout current = new Checkout();
        current.setApplyStatus("3");
        current.setElderId(101L);
        current.setTenantId(1L);
        bind(current, 0, new Checkout[1], 1);
        try
        {
            service.complete(1L, BigDecimal.ZERO, "CASH");
            fail("非待清算状态应当抛出异常");
        }
        catch (ServiceException e)
        {
            assertEquals("当前状态不允许完成退住", e.getMessage());
        }
    }
}
