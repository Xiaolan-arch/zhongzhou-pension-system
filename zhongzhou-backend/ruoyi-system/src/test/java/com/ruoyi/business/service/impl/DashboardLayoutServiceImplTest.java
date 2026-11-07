package com.ruoyi.business.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.After;
import org.junit.Test;
import com.ruoyi.business.domain.DashboardLayout;
import com.ruoyi.business.mapper.DashboardLayoutMapper;
import com.ruoyi.common.exception.ServiceException;

/**
 * 工作台个性化布局 服务单元测试
 */
public class DashboardLayoutServiceImplTest extends BusinessTestSupport
{
    @After
    public void tearDown()
    {
        logout();
    }

    private DashboardLayoutServiceImpl newService(DashboardLayoutMapper mapper)
    {
        DashboardLayoutServiceImpl service = new DashboardLayoutServiceImpl();
        inject(service, "layoutMapper", mapper);
        return service;
    }

    @Test
    public void testGetLayoutWithoutConfigReturnsNull()
    {
        loginAs(5L, 1L, "operator");
        DashboardLayoutMapper mapper = stub(DashboardLayoutMapper.class, new StubHandler()
        {
            @Override
            public Object handle(String methodName, Object[] args)
            {
                return NOT_STUBBED;
            }
        });
        assertNull(newService(mapper).getLayout());
    }

    @Test
    public void testGetLayoutReturnsStoredJson()
    {
        loginAs(5L, 1L, "operator");
        DashboardLayout stored = new DashboardLayout();
        stored.setLayoutJson("[{\"key\":\"kpi\",\"visible\":true}]");
        DashboardLayoutMapper mapper = stub(DashboardLayoutMapper.class, new StubHandler()
        {
            @Override
            public Object handle(String methodName, Object[] args)
            {
                return stored;
            }
        });
        assertEquals(stored.getLayoutJson(), newService(mapper).getLayout());
    }

    @Test
    public void testSaveLayoutRejectsEmpty()
    {
        loginAs(5L, 1L, "operator");
        assertSaveRejected(null);
        assertSaveRejected("   ");
    }

    @Test
    public void testSaveLayoutRejectsNonArrayJson()
    {
        loginAs(5L, 1L, "operator");
        assertSaveRejected("{\"key\":\"kpi\"}");
        assertSaveRejected("[{\"key\":\"kpi\"}");
    }

    @Test
    public void testSaveLayoutRejectsTooLong()
    {
        loginAs(5L, 1L, "operator");
        StringBuilder sb = new StringBuilder("[");
        while (sb.length() <= 2000)
        {
            sb.append("0");
        }
        sb.append("]");
        assertSaveRejected(sb.toString());
    }

    @Test
    public void testSaveLayoutWritesUserAndTenant()
    {
        loginAs(5L, 2L, "operator");
        final Object[] captured = new Object[1];
        DashboardLayoutMapper mapper = stub(DashboardLayoutMapper.class, new StubHandler()
        {
            @Override
            public Object handle(String methodName, Object[] args)
            {
                captured[0] = args[0];
                return 1;
            }
        });

        newService(mapper).saveLayout("[{\"key\":\"kpi\",\"visible\":true}]");

        DashboardLayout saved = (DashboardLayout) captured[0];
        assertEquals(Long.valueOf(5L), saved.getUserId());
        assertEquals(Long.valueOf(2L), saved.getTenantId());
        assertTrue(saved.getLayoutJson().startsWith("["));
    }

    private void assertSaveRejected(String json)
    {
        DashboardLayoutMapper mapper = stub(DashboardLayoutMapper.class, new StubHandler()
        {
            @Override
            public Object handle(String methodName, Object[] args)
            {
                fail("非法布局不应写入数据库");
                return NOT_STUBBED;
            }
        });
        try
        {
            newService(mapper).saveLayout(json);
            fail("非法布局应抛出 ServiceException");
        }
        catch (ServiceException expected)
        {
            // 预期异常
        }
    }
}
