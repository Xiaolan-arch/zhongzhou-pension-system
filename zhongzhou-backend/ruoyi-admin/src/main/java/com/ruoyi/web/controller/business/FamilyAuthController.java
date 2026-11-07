package com.ruoyi.web.controller.business;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.domain.FamilyToken;
import com.ruoyi.business.service.IFamilyAuthService;
import com.ruoyi.common.annotation.RateLimiter;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.LimitType;

/**
 * 家属端登录接口（小程序匿名访问，凭证通过 X-Family-Token 传递）
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/family")
public class FamilyAuthController
{
    @Autowired
    private IFamilyAuthService familyAuthService;

    /**
     * 发送登录验证码（演示环境返回验证码明文）
     */
    @PostMapping("/auth/sendCode")
    @RateLimiter(time = 60, count = 1, limitType = LimitType.IP)
    public AjaxResult sendCode(@RequestBody Map<String, String> body)
    {
        String phone = body == null ? null : body.get("phone");
        String code = familyAuthService.sendCode(phone);
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("demoCode", code);
        data.put("expireSeconds", 300);
        return AjaxResult.success("验证码已发送", data);
    }

    /**
     * 手机号 + 验证码登录
     */
    @PostMapping("/auth/login")
    public AjaxResult login(@RequestBody Map<String, String> body)
    {
        String phone = body == null ? null : body.get("phone");
        String code = body == null ? null : body.get("code");
        String wxCode = body == null ? null : body.get("wxCode");
        return AjaxResult.success(familyAuthService.login(phone, code, wxCode));
    }

    /**
     * 当前家属信息
     */
    @GetMapping("/profile")
    public AjaxResult profile(@RequestHeader(value = "X-Family-Token", required = false) String token)
    {
        FamilyToken familyToken = familyAuthService.authenticate(token);
        return AjaxResult.success(familyToken);
    }

    /**
     * 退出登录
     */
    @PostMapping("/auth/logout")
    public AjaxResult logout(@RequestHeader(value = "X-Family-Token", required = false) String token)
    {
        familyAuthService.logout(token);
        return AjaxResult.success();
    }
}
