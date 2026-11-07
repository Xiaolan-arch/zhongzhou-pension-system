package com.ruoyi.business.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.ruoyi.business.domain.Bill;
import com.ruoyi.business.domain.FamilyToken;
import com.ruoyi.business.domain.Visit;
import com.ruoyi.business.domain.vo.FamilyElderVO;
import com.ruoyi.business.mapper.FamilyMapper;
import com.ruoyi.business.mapper.VisitMapper;
import com.ruoyi.common.exception.ServiceException;

/**
 * 家属端业务：绑定关系校验、探访预约参数校验与取消
 */
public class FamilyPortalServiceImplTest extends BusinessTestSupport
{
    private FamilyPortalServiceImpl service;

    private FamilyToken family;

    private FamilyElderVO boundElder;

    private Visit inserted;

    private int cancelRows;

    @Before
    public void setUp()
    {
        service = new FamilyPortalServiceImpl();
        family = new FamilyToken();
        family.setFamilyId(1L);
        family.setFamilyName("张敏");
        family.setPhone("13822220001");
        family.setTenantId(1L);

        boundElder = new FamilyElderVO();
        boundElder.setElderId(1L);
        boundElder.setElderName("张伟");

        injectedVisit();
    }

    @After
    public void tearDown()
    {
        logout();
    }

    private void injectedVisit()
    {
        inserted = null;
        cancelRows = 0;
        inject(service, "familyMapper", stub(FamilyMapper.class, new StubHandler() {
            @Override
            public Object handle(String methodName, Object[] args)
            {
                if ("selectElderDetail".equals(methodName))
                {
                    Long elderId = (Long) args[2];
                    return elderId != null && elderId == 1L ? boundElder : null;
                }
                if ("selectBillById".equals(methodName))
                {
                    return null;
                }
                if ("cancelVisit".equals(methodName))
                {
                    return cancelRows;
                }
                return NOT_STUBBED;
            }
        }));
        inject(service, "visitMapper", stub(VisitMapper.class, new StubHandler() {
            @Override
            public Object handle(String methodName, Object[] args)
            {
                if ("insertVisit".equals(methodName))
                {
                    inserted = (Visit) args[0];
                    return 1;
                }
                return NOT_STUBBED;
            }
        }));
    }

    @Test
    public void testServicesRejectsUnboundElder()
    {
        try
        {
            service.selectServices(family, 999L);
            fail("非绑定老人应当拒绝访问");
        }
        catch (ServiceException e)
        {
            assertEquals("无权查看该老人信息", e.getMessage());
        }
    }

    @Test
    public void testBillDetailRejectsMissingBill()
    {
        try
        {
            service.selectBillDetail(family, 99L);
            fail("不存在的账单应当拒绝");
        }
        catch (ServiceException e)
        {
            assertEquals("账单不存在", e.getMessage());
        }
    }

    @Test
    public void testCreateVisitRejectsTooManyPeople()
    {
        Visit body = new Visit();
        body.setElderId(1L);
        body.setVisitDate(new Date());
        body.setVisitTime("10:00");
        body.setPeopleCount(9);
        try
        {
            service.createVisit(family, body);
            fail("超出人数上限应当拒绝");
        }
        catch (ServiceException e)
        {
            assertEquals("探访人数需在 1-5 人之间", e.getMessage());
        }
    }

    @Test
    public void testCreateVisitRejectsPastDate() throws Exception
    {
        Visit body = new Visit();
        body.setElderId(1L);
        body.setVisitDate(new SimpleDateFormat("yyyy-MM-dd").parse("2020-01-01"));
        body.setVisitTime("10:00");
        try
        {
            service.createVisit(family, body);
            fail("历史日期应当拒绝");
        }
        catch (ServiceException e)
        {
            assertEquals("探访日期不能早于今天", e.getMessage());
        }
    }

    @Test
    public void testCreateVisitFillsFamilyIdentity() throws Exception
    {
        Visit body = new Visit();
        body.setElderId(1L);
        body.setVisitDate(new SimpleDateFormat("yyyy-MM-dd").parse("2099-01-01"));
        body.setVisitTime(" 10:00 ");
        body.setPeopleCount(2);
        body.setRemark("探访父亲");

        Visit result = service.createVisit(family, body);
        assertNotNull(result);
        assertEquals("1", result.getVisitType());
        assertEquals("0", result.getStatus());
        assertEquals("13822220001", result.getVisitorPhone());
        assertEquals("张敏", result.getVisitorName());
        assertEquals("张伟", result.getElderName());
        assertEquals(1L, result.getTenantId().longValue());
        assertEquals("10:00", result.getVisitTime());
    }

    @Test
    public void testCancelVisitRejectsUnknownVisit()
    {
        cancelRows = 0;
        try
        {
            service.cancelVisit(family, 100L);
            fail("不可取消的预约应当拒绝");
        }
        catch (ServiceException e)
        {
            assertEquals("预约不存在或当前状态无法取消", e.getMessage());
        }
    }

    @Test
    public void testCancelVisitSuccess()
    {
        cancelRows = 1;
        assertEquals(1, service.cancelVisit(family, 100L));
    }
}
