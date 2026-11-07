package com.ruoyi.business.service;

import com.ruoyi.business.domain.FamilyToken;

/**
 * 家属端验证码与登录凭证存储
 *
 * @author ruoyi
 */
public interface IFamilyCodeStore
{
    /**
     * 保存手机号验证码
     */
    public void saveCode(String phone, String code, long seconds);

    /**
     * 读取手机号验证码（不存在返回 null）
     */
    public String getCode(String phone);

    /**
     * 删除手机号验证码
     */
    public void removeCode(String phone);

    /**
     * 保存登录凭证
     */
    public void saveToken(String token, FamilyToken familyToken, long seconds);

    /**
     * 读取登录凭证（不存在返回 null）
     */
    public FamilyToken getToken(String token);

    /**
     * 删除登录凭证
     */
    public void removeToken(String token);
}
