package com.ruoyi.business.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import java.math.BigDecimal;
import java.util.Date;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.ruoyi.business.domain.Bill;
import com.ruoyi.business.domain.Checkin;
import com.ruoyi.business.mapper.BillItemMapper;
import com.ruoyi.business.mapper.BillMapper;
import com.ruoyi.business.mapper.CheckinBedMapper;
import com.ruoyi.business.mapper.CheckinMapper;
import com.ruoyi.common.exception.ServiceException;

/**
 * 入住办理服务：状态机、审核校验、床位抢占与首期账单生成
 */
public class CheckinServiceImplTest extends BusinessTestSupport
{
    private CheckinServiceImpl service;

    @Before
    public void setUp()
    {
        loginAs(2L, 1L, "admin");
        service = new CheckinServiceImpl();
    }

    @After
    public void tearDown()
    {
        logout();
    }

    /** 绑定四个依赖 Mapper 的桩实现 */
    private void bind(StubHandler checkinHandler, StubHandler bedHandler, StubHandler billHandler,
            StubHandler billItemHandler)
    {
        inject(service, "checkinMapper", stub(CheckinMapper.class, checkinHandler));
        inject(service, "checkinBedMapper", stub(CheckinBedMapper.class, bedHandler));
        inject(service, "billMapper", stub(BillMapper.class, billHandler));
        inject(service, "billItemMapper", stub(BillItemMapper.class, billItemHandler));
    }

    private void bindEmpty()
    {
        StubHandler empty = new StubHandler() {
            @Override
            public Object handle(String methodName, Object[] args)
            {
                return NOT_STUBBED;
            }
        };
        bind(empty, empty, empty, empty);
    }

    @Test
    public void submitStepRejectsInvalidStep()
    {
        bindEmpty();
        try
        {
            service.submitStep(1L, 7, new Checkin());
            fail("步骤参数错误时应当抛出异常");
        }
        catch (ServiceException e)
        {
            assertEquals("步骤参数错误", e.getMessage());
        }
    }

    @Test
    public void submitStepRejectsStatusChanged()
    {
        final Checkin current = new Checkin();
        current.setApplyStatus("1");
        StubHandler handler = new StubHandler() {
            @Override
            public Object handle(String methodName, Object[] args)
            {
                return "selectCheckinById".equals(methodName) ? current : NOT_STUBBED;
            }
        };
        bind(handler, handler, handler, handler);
        try
        {
            service.submitStep(1L, 1, new Checkin());
            fail("状态不一致时应当抛出异常");
        }
        catch (ServiceException e)
        {
            assertEquals("状态已变更，请刷新后重试", e.getMessage());
        }
    }

    @Test
    public void auditRejectsUnexpectedStatus()
    {
        final Checkin current = new Checkin();
        current.setApplyStatus("1");
        StubHandler handler = new StubHandler() {
            @Override
            public Object handle(String methodName, Object[] args)
            {
                return "selectCheckinById".equals(methodName) ? current : NOT_STUBBED;
            }
        };
        bind(handler, handler, handler, handler);
        Checkin request = new Checkin();
        request.setCheckinId(1L);
        request.setAuditResult("0");
        try
        {
            service.audit(request);
            fail("非待审核状态应当抛出异常");
        }
        catch (ServiceException e)
        {
            assertEquals("当前状态不允许审核", e.getMessage());
        }
    }

    @Test
    public void auditRejectsInvalidResult()
    {
        final Checkin current = new Checkin();
        current.setApplyStatus("2");
        StubHandler handler = new StubHandler() {
            @Override
            public Object handle(String methodName, Object[] args)
            {
                return "selectCheckinById".equals(methodName) ? current : NOT_STUBBED;
            }
        };
        bind(handler, handler, handler, handler);
        Checkin request = new Checkin();
        request.setCheckinId(1L);
        request.setAuditResult("9");
        try
        {
            service.audit(request);
            fail("审核结果非法时应当抛出异常");
        }
        catch (ServiceException e)
        {
            assertEquals("审核结果不正确", e.getMessage());
        }
    }

    @Test
    public void completeRejectsOccupiedBed()
    {
        final Checkin current = new Checkin();
        current.setApplyStatus("5");
        current.setPayStatus("1");
        current.setBedId(1013L);
        current.setTenantId(1L);
        current.setIdCard("110101195001019999");
        current.setElderName("测试老人");
        StubHandler checkinHandler = new StubHandler() {
            @Override
            public Object handle(String methodName, Object[] args)
            {
                return "selectCheckinById".equals(methodName) ? current : NOT_STUBBED;
            }
        };
        StubHandler bedHandler = new StubHandler() {
            @Override
            public Object handle(String methodName, Object[] args)
            {
                if ("selectElderIdByIdCard".equals(methodName))
                {
                    return 101L;
                }
                if ("occupyBed".equals(methodName))
                {
                    return 0;
                }
                return NOT_STUBBED;
            }
        };
        bind(checkinHandler, bedHandler, checkinHandler, checkinHandler);
        try
        {
            service.complete(1L);
            fail("床位被占用时应当抛出异常");
        }
        catch (ServiceException e)
        {
            assertEquals("床位已被占用", e.getMessage());
        }
    }

    @Test
    public void submitStep6CreatesFirstBill()
    {
        final Checkin current = new Checkin();
        current.setCheckinId(1L);
        current.setApplyStatus("5");
        current.setTenantId(1L);
        current.setElderName("测试老人");
        current.setIdCard("110101195001019999");
        current.setCareLevel("L1");
        current.setFeeStandard(new BigDecimal("2600.00"));
        current.setCheckinDate(new Date());
        final int[] itemCount = new int[1];
        final Bill[] inserted = new Bill[1];
        StubHandler checkinHandler = new StubHandler() {
            @Override
            public Object handle(String methodName, Object[] args)
            {
                if ("selectCheckinById".equals(methodName))
                {
                    return current;
                }
                if ("updateCheckin".equals(methodName) || "updateCheckinFirstPay".equals(methodName))
                {
                    return 1;
                }
                return NOT_STUBBED;
            }
        };
        StubHandler bedHandler = new StubHandler() {
            @Override
            public Object handle(String methodName, Object[] args)
            {
                return "selectElderIdByIdCard".equals(methodName) ? 101L : NOT_STUBBED;
            }
        };
        StubHandler billHandler = new StubHandler() {
            @Override
            public Object handle(String methodName, Object[] args)
            {
                if ("selectFirstBillByCheckinId".equals(methodName))
                {
                    return null;
                }
                if ("insertBill".equals(methodName))
                {
                    Bill bill = (Bill) args[0];
                    bill.setBillId(77L);
                    inserted[0] = bill;
                    return 1;
                }
                return NOT_STUBBED;
            }
        };
        StubHandler billItemHandler = new StubHandler() {
            @Override
            public Object handle(String methodName, Object[] args)
            {
                if ("insertBillItem".equals(methodName))
                {
                    itemCount[0]++;
                    return 1;
                }
                return NOT_STUBBED;
            }
        };
        bind(checkinHandler, bedHandler, billHandler, billItemHandler);

        Checkin request = new Checkin();
        int rows = service.submitStep(1L, 6, request);

        assertEquals(1, rows);
        assertNotNull("首期账单应已写入", inserted[0]);
        assertEquals("首期账单金额应为护理费加押金", new BigDecimal("3600.00"), inserted[0].getAmount());
        assertEquals("首期账单应标记已支付", "1", inserted[0].getPayStatus());
        assertEquals("首期账单类型应为 0", "0", inserted[0].getBillType());
        assertEquals("首期账单应写入两条明细", 2, itemCount[0]);
        assertEquals("首期账单ID应回写入住申请", Long.valueOf(77L), current.getFirstBillId());
    }
}
