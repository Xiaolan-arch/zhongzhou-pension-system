package com.ruoyi.business.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.ruoyi.business.config.WxMiniAppProperties;
import com.ruoyi.business.domain.FamilyMember;
import com.ruoyi.business.domain.FamilyToken;
import com.ruoyi.business.domain.vo.FamilyElderVO;
import com.ruoyi.business.mapper.FamilyMapper;
import com.ruoyi.business.service.IFamilyAuthService;
import com.ruoyi.business.service.IFamilyCodeStore;
import com.ruoyi.business.wx.WxMiniAppClient;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * 家属端登录服务实现
 *
 * @author ruoyi
 */
@Service
public class FamilyAuthServiceImpl implements IFamilyAuthService
{
    private static final Logger log = LoggerFactory.getLogger(FamilyAuthServiceImpl.class);

    /** 手机号格式 */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    /** 验证码有效期（秒） */
    private static final long CODE_EXPIRE_SECONDS = 300L;

    /** 登录凭证有效期（秒），180 天；同时写入签名令牌，Redis 重启也能免登录 */
    private static final long TOKEN_EXPIRE_SECONDS = 180L * 24 * 3600L;

    /** 演示环境验证码固定 6 位 */
    private static final int CODE_BOUND = 1000000;

    /** 手机号验证码发送限流 key 前缀 */
    private static final String CODE_LIMIT_KEY = "family:code:limit:";

    /** 手机号验证码发送限流窗口（秒），1 分钟内同一手机号只能发 1 次 */
    private static final long CODE_LIMIT_WINDOW_SECONDS = 60L;

    /** 限流阈值：距离过期大于该秒数视为仍在严格限流期 */
    private static final long CODE_LIMIT_GRACE_SECONDS = 10L;

    @Autowired
    private IFamilyCodeStore familyCodeStore;

    @Autowired
    private FamilyMapper familyMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private WxMiniAppProperties wxProperties;

    @Autowired
    private WxMiniAppClient wxClient;

    /** 令牌密钥（与后台令牌共用配置） */
    @Value("${token.secret}")
    private String tokenSecret;

    @Override
    public String sendCode(String phone)
    {
        checkPhone(phone);
        // 手机号维度限流：1 分钟内同一手机号最多发 1 次
        String limitKey = CODE_LIMIT_KEY + phone;
        Boolean acquired = stringRedisTemplate.opsForValue()
                .setIfAbsent(limitKey, "1", CODE_LIMIT_WINDOW_SECONDS, TimeUnit.SECONDS);
        if (acquired == null || !acquired)
        {
            Long ttl = stringRedisTemplate.getExpire(limitKey, TimeUnit.SECONDS);
            if (ttl != null && ttl > CODE_LIMIT_GRACE_SECONDS)
            {
                throw new ServiceException("发送太频繁，请稍后再试");
            }
            // 已超过宽限期，重新抢占锁
            stringRedisTemplate.opsForValue().set(limitKey, "1", CODE_LIMIT_WINDOW_SECONDS, TimeUnit.SECONDS);
        }
        List<FamilyMember> families = familyMapper.selectFamiliesByPhone(phone);
        if (families == null || families.isEmpty())
        {
            throw new ServiceException("该手机号未绑定老人，请联系养老机构登记家属信息");
        }
        String code = String.format("%06d", (int) (Math.random() * CODE_BOUND));
        familyCodeStore.saveCode(phone, code, CODE_EXPIRE_SECONDS);
        return code;
    }

    @Override
    public Map<String, Object> login(String phone, String code)
    {
        return login(phone, code, null);
    }

    @Override
    public Map<String, Object> login(String phone, String code, String wxCode)
    {
        checkPhone(phone);
        if (code == null || code.trim().length() == 0)
        {
            throw new ServiceException("请输入验证码");
        }
        String cached = familyCodeStore.getCode(phone);
        if (cached == null)
        {
            throw new ServiceException("验证码已过期，请重新获取");
        }
        if (!cached.equals(code.trim()))
        {
            throw new ServiceException("验证码错误");
        }

        List<FamilyMember> families = familyMapper.selectFamiliesByPhone(phone);
        if (families == null || families.isEmpty())
        {
            throw new ServiceException("该手机号未绑定老人，请联系养老机构登记家属信息");
        }

        FamilyMember first = families.get(0);

        // 绑定微信 openid（Mock 模式直接生成演示 openid，便于订阅消息演示）
        String openid = resolveOpenid(phone, wxCode);
        if (StringUtils.isNotEmpty(openid))
        {
            familyMapper.updateOpenidByPhone(phone, openid);
        }

        FamilyToken tokenInfo = new FamilyToken();
        tokenInfo.setFamilyId(first.getFamilyId());
        tokenInfo.setPhone(first.getPhone());
        tokenInfo.setFamilyName(first.getFamilyName());
        tokenInfo.setRelation(first.getRelation());
        tokenInfo.setTenantId(first.getTenantId());
        tokenInfo.setOpenid(openid);

        String token = createSignedToken(tokenInfo);
        familyCodeStore.saveToken(token, tokenInfo, TOKEN_EXPIRE_SECONDS);
        familyCodeStore.removeCode(phone);

        List<FamilyElderVO> elders = familyMapper.selectEldersByPhone(phone, first.getTenantId());
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("token", token);
        result.put("family", tokenInfo);
        result.put("elders", elders);
        return result;
    }

    @Override
    public FamilyToken authenticate(String token)
    {
        if (token == null || token.trim().length() == 0)
        {
            throw new ServiceException("请先登录", 401);
        }
        FamilyToken familyToken = familyCodeStore.getToken(token.trim());
        if (familyToken == null)
        {
            // Redis 被清空/重启时，用签名令牌恢复登录态，实现长期免登录
            familyToken = parseSignedToken(token.trim());
        }
        if (familyToken == null)
        {
            throw new ServiceException("登录已过期，请重新登录", 401);
        }
        return familyToken;
    }

    /** 生成带过期时间的签名令牌（不依赖 Redis） */
    private String createSignedToken(FamilyToken info)
    {
        Map<String, Object> claims = new HashMap<String, Object>();
        claims.put("familyId", info.getFamilyId());
        claims.put("phone", info.getPhone());
        claims.put("familyName", info.getFamilyName());
        claims.put("relation", info.getRelation());
        claims.put("tenantId", info.getTenantId());
        claims.put("openid", info.getOpenid());
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + TOKEN_EXPIRE_SECONDS * 1000L))
                .signWith(SignatureAlgorithm.HS512, tokenSecret)
                .compact();
    }

    /** 校验签名令牌，失败返回 null */
    private FamilyToken parseSignedToken(String token)
    {
        try
        {
            Claims claims = Jwts.parser().setSigningKey(tokenSecret).parseClaimsJws(token).getBody();
            FamilyToken info = new FamilyToken();
            Object familyId = claims.get("familyId");
            Object tenantId = claims.get("tenantId");
            info.setFamilyId(familyId == null ? null : Long.valueOf(familyId.toString()));
            info.setTenantId(tenantId == null ? null : Long.valueOf(tenantId.toString()));
            info.setPhone((String) claims.get("phone"));
            info.setFamilyName((String) claims.get("familyName"));
            info.setRelation((String) claims.get("relation"));
            info.setOpenid((String) claims.get("openid"));
            return info;
        }
        catch (Exception e)
        {
            log.debug("[FamilyAuth] 签名令牌校验失败: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public void logout(String token)
    {
        if (token != null && token.trim().length() > 0)
        {
            familyCodeStore.removeToken(token.trim());
        }
    }

    /**
     * 解析并绑定微信 openid
     * 真实模式：用 wx.login 的 code 调用 jscode2session；
     * Mock 模式：生成演示 openid，便于演示订阅消息发送记录。
     */
    private String resolveOpenid(String phone, String wxCode)
    {
        if (wxProperties.isEnabled() && !wxProperties.isMock())
        {
            if (StringUtils.isEmpty(wxCode))
            {
                return null;
            }
            try
            {
                return wxClient.code2Session(wxCode.trim());
            }
            catch (Exception e)
            {
                log.warn("微信登录换取 openid 失败，phone={}", phone, e);
                return null;
            }
        }
        return "mock_" + phone;
    }

    private void checkPhone(String phone)
    {
        if (phone == null || !PHONE_PATTERN.matcher(phone.trim()).matches())
        {
            throw new ServiceException("请输入正确的手机号");
        }
    }
}
