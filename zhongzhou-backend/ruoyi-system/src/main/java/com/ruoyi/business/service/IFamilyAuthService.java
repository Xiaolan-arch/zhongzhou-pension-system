package com.ruoyi.business.service;

import java.util.Map;
import com.ruoyi.business.domain.FamilyToken;

/**
 * 家属端登录服务
 *
 * @author ruoyi
 */
public interface IFamilyAuthService
{
    /**
     * 发送登录验证码（演示环境直接返回验证码）
     */
    public String sendCode(String phone);

    /**
     * 手机号 + 验证码登录
     */
    public Map<String, Object> login(String phone, String code);

    /**
     * 手机号 + 验证码登录（附带小程序 wx.login 的 code，用于绑定 openid）
     */
    public Map<String, Object> login(String phone, String code, String wxCode);

    /**
     * 校验凭证，无效抛出 401 异常
     */
    public FamilyToken authenticate(String token);

    /**
     * 退出登录
     */
    public void logout(String token);
}
