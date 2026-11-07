package com.ruoyi.business.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.ruoyi.business.domain.FamilyMember;
import com.ruoyi.business.domain.FamilyToken;
import com.ruoyi.business.mapper.FamilyMapper;
import com.ruoyi.business.service.IFamilyCodeStore;
import com.ruoyi.common.exception.ServiceException;

/**
 * 家属端登录：手机号校验、验证码签发与校验、凭证生命周期
 */
public class FamilyAuthServiceImplTest extends BusinessTestSupport
{
    private FamilyAuthServiceImpl service;

    private final Map<String, String> codeStore = new HashMap<String, String>();

    private final Map<String, FamilyToken> tokenStore = new HashMap<String, FamilyToken>();

    private List<FamilyMember> boundFamilies;

    @Before
    public void setUp()
    {
        service = new FamilyAuthServiceImpl();
        codeStore.clear();
        tokenStore.clear();

        FamilyMember member = new FamilyMember();
        member.setFamilyId(1L);
        member.setFamilyName("张敏");
        member.setPhone("13822220001");
        member.setRelation("女儿");
        member.setTenantId(1L);
        boundFamilies = new ArrayList<FamilyMember>();
        boundFamilies.add(member);

        IFamilyCodeStore store = stub(IFamilyCodeStore.class, new StubHandler() {
            @Override
            public Object handle(String methodName, Object[] args)
            {
                if ("saveCode".equals(methodName))
                {
                    codeStore.put((String) args[0], (String) args[1]);
                    return NOT_STUBBED;
                }
                if ("getCode".equals(methodName))
                {
                    return codeStore.get(args[0]);
                }
                if ("removeCode".equals(methodName))
                {
                    codeStore.remove(args[0]);
                    return NOT_STUBBED;
                }
                if ("saveToken".equals(methodName))
                {
                    tokenStore.put((String) args[0], (FamilyToken) args[1]);
                    return NOT_STUBBED;
                }
                if ("getToken".equals(methodName))
                {
                    return tokenStore.get(args[0]);
                }
                if ("removeToken".equals(methodName))
                {
                    tokenStore.remove(args[0]);
                    return NOT_STUBBED;
                }
                return NOT_STUBBED;
            }
        });
        inject(service, "familyCodeStore", store);

        inject(service, "familyMapper", stub(FamilyMapper.class, new StubHandler() {
            @Override
            public Object handle(String methodName, Object[] args)
            {
                if ("selectFamiliesByPhone".equals(methodName))
                {
                    return boundFamilies;
                }
                return NOT_STUBBED;
            }
        }));
    }

    @After
    public void tearDown()
    {
        logout();
    }

    @Test
    public void testSendCodeRejectsInvalidPhone()
    {
        try
        {
            service.sendCode("12345");
            fail("应当拒绝非法手机号");
        }
        catch (ServiceException e)
        {
            assertEquals("请输入正确的手机号", e.getMessage());
        }
    }

    @Test
    public void testSendCodeRejectsUnboundPhone()
    {
        boundFamilies = new ArrayList<FamilyMember>();
        try
        {
            service.sendCode("13800009999");
            fail("未绑定手机号应当拒绝发送");
        }
        catch (ServiceException e)
        {
            assertEquals("该手机号未绑定老人，请联系养老机构登记家属信息", e.getMessage());
        }
    }

    @Test
    public void testSendCodeReturnsSixDigits()
    {
        String code = service.sendCode("13822220001");
        assertEquals(6, code.length());
        assertEquals(code, codeStore.get("13822220001"));
    }

    @Test
    public void testLoginWithoutCode()
    {
        try
        {
            service.login("13822220001", "112233");
            fail("未获取验证码时应拒绝登录");
        }
        catch (ServiceException e)
        {
            assertEquals("验证码已过期，请重新获取", e.getMessage());
        }
    }

    @Test
    public void testLoginWithWrongCode()
    {
        service.sendCode("13822220001");
        try
        {
            service.login("13822220001", "000000");
            fail("验证码错误应拒绝登录");
        }
        catch (ServiceException e)
        {
            assertEquals("验证码错误", e.getMessage());
        }
    }

    @Test
    public void testLoginSuccessIssuesTokenAndConsumesCode()
    {
        String code = service.sendCode("13822220001");
        Map<String, Object> result = service.login("13822220001", code);
        assertNotNull(result.get("token"));
        assertEquals(1L, ((FamilyToken) result.get("family")).getTenantId().longValue());
        assertEquals("张敏", ((FamilyToken) result.get("family")).getFamilyName());
        assertNull(codeStore.get("13822220001"));
        assertNotNull(tokenStore.get((String) result.get("token")));
    }

    @Test
    public void testAuthenticateRejectsMissingOrExpiredToken()
    {
        try
        {
            service.authenticate(null);
            fail("缺少凭证应当拒绝");
        }
        catch (ServiceException e)
        {
            assertEquals(401, e.getCode().intValue());
        }
        try
        {
            service.authenticate("not-exists");
            fail("无效凭证应当拒绝");
        }
        catch (ServiceException e)
        {
            assertEquals(401, e.getCode().intValue());
        }
    }

    @Test
    public void testLogoutRemovesToken()
    {
        FamilyToken tokenInfo = new FamilyToken();
        tokenInfo.setPhone("13822220001");
        tokenInfo.setTenantId(1L);
        tokenStore.put("abc", tokenInfo);
        FamilyToken resolved = service.authenticate("abc");
        assertEquals("13822220001", resolved.getPhone());
        service.logout("abc");
        assertNull(tokenStore.get("abc"));
    }
}
