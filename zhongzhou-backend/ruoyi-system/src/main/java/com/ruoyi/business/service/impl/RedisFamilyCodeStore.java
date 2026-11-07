package com.ruoyi.business.service.impl;

import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.business.domain.FamilyToken;
import com.ruoyi.business.service.IFamilyCodeStore;
import com.ruoyi.common.core.redis.RedisCache;

/**
 * 基于 Redis 的家属端验证码与凭证存储
 *
 * @author ruoyi
 */
@Component
public class RedisFamilyCodeStore implements IFamilyCodeStore
{
    private static final String CODE_KEY = "family:code:";

    private static final String TOKEN_KEY = "family:token:";

    @Autowired
    private RedisCache redisCache;

    @Override
    public void saveCode(String phone, String code, long seconds)
    {
        redisCache.setCacheObject(CODE_KEY + phone, code, (int) seconds, TimeUnit.SECONDS);
    }

    @Override
    public String getCode(String phone)
    {
        return redisCache.getCacheObject(CODE_KEY + phone);
    }

    @Override
    public void removeCode(String phone)
    {
        redisCache.deleteObject(CODE_KEY + phone);
    }

    @Override
    public void saveToken(String token, FamilyToken familyToken, long seconds)
    {
        redisCache.setCacheObject(TOKEN_KEY + token, familyToken, (int) seconds, TimeUnit.SECONDS);
    }

    @Override
    public FamilyToken getToken(String token)
    {
        return redisCache.getCacheObject(TOKEN_KEY + token);
    }

    @Override
    public void removeToken(String token)
    {
        redisCache.deleteObject(TOKEN_KEY + token);
    }
}
