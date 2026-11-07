package com.ruoyi.business.service.impl;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;

/**
 * 业务服务单元测试基础支撑：登录上下文、字段注入、Mapper 桩实现
 */
abstract class BusinessTestSupport
{
    /** 桩方法未定义时的标记 */
    protected static final Object NOT_STUBBED = new Object();

    /** 桩处理接口 */
    protected interface StubHandler
    {
        Object handle(String methodName, Object[] args);
    }

    /** 设置登录用户上下文（tenantId 为机构ID） */
    protected static void loginAs(long userId, Long tenantId, String userName)
    {
        SysUser sysUser = new SysUser();
        sysUser.setUserId(userId);
        sysUser.setUserName(userName);
        sysUser.setTenantId(tenantId);
        LoginUser loginUser = new LoginUser(userId, null, sysUser, Collections.emptySet());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities()));
    }

    /** 清理登录上下文 */
    protected static void logout()
    {
        SecurityContextHolder.clearContext();
    }

    /** 反射注入私有依赖字段 */
    protected static void inject(Object target, String fieldName, Object value)
    {
        try
        {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        }
        catch (Exception e)
        {
            throw new IllegalStateException("注入字段失败: " + fieldName, e);
        }
    }

    /** 为 Mapper 接口生成桩实现，未桩方法返回类型默认值 */
    @SuppressWarnings("unchecked")
    protected static <T> T stub(Class<T> type, final StubHandler handler)
    {
        InvocationHandler invocationHandler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
            {
                Object result = handler.handle(method.getName(), args);
                return result == NOT_STUBBED ? defaultValue(method.getReturnType()) : result;
            }
        };
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type }, invocationHandler);
    }

    private static Object defaultValue(Class<?> returnType)
    {
        if (returnType == int.class || returnType == Integer.class)
        {
            return 0;
        }
        if (returnType == long.class || returnType == Long.class)
        {
            return 0L;
        }
        if (returnType == boolean.class || returnType == Boolean.class)
        {
            return Boolean.FALSE;
        }
        if (returnType == List.class)
        {
            return new ArrayList<Object>();
        }
        return null;
    }
}
