package com.ruoyi.business.wx;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.business.config.WxMiniAppProperties;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

/**
 * 微信小程序服务端接口客户端（code2session / access_token / 订阅消息发送）
 * 仅在 wx.miniapp.enabled=true 且 mock=false 时真实调用
 */
@Service
public class WxMiniAppClient
{
    private static final Logger log = LoggerFactory.getLogger(WxMiniAppClient.class);

    private static final String TOKEN_CACHE_KEY = "wx:miniapp:access_token";

    @Autowired
    private WxMiniAppProperties properties;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final RestTemplate restTemplate;

    public WxMiniAppClient()
    {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);
        this.restTemplate = new RestTemplate(factory);
    }

    /**
     * 用 wx.login 的 code 换取 openid
     */
    public String code2Session(String jsCode)
    {
        checkConfig();
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + properties.getAppId()
                + "&secret=" + properties.getAppSecret()
                + "&js_code=" + jsCode + "&grant_type=authorization_code";
        String body = restTemplate.getForObject(url, String.class);
        JSONObject json = JSON.parseObject(body);
        if (json == null || json.getIntValue("errcode", 0) != 0)
        {
            throw new ServiceException("微信登录失败：" + (json == null ? "无响应" : json.getString("errmsg")));
        }
        return json.getString("openid");
    }

    /**
     * 获取接口调用凭证（Redis 缓存，提前 5 分钟过期）
     */
    public String getAccessToken()
    {
        String cached = redisTemplate.opsForValue().get(TOKEN_CACHE_KEY);
        if (StringUtils.isNotEmpty(cached))
        {
            return cached;
        }
        checkConfig();
        String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="
                + properties.getAppId() + "&secret=" + properties.getAppSecret();
        String body = restTemplate.getForObject(url, String.class);
        JSONObject json = JSON.parseObject(body);
        String token = json == null ? null : json.getString("access_token");
        if (StringUtils.isEmpty(token))
        {
            throw new ServiceException("获取微信 access_token 失败：" + (json == null ? "无响应" : json.getString("errmsg")));
        }
        int expiresIn = json.getIntValue("expires_in", 7200);
        redisTemplate.opsForValue().set(TOKEN_CACHE_KEY, token, Math.max(60, expiresIn - 300), TimeUnit.SECONDS);
        return token;
    }

    /**
     * 发送订阅消息
     */
    public void sendSubscribeMessage(String openid, String templateId, String page, Map<String, String> data)
    {
        String token = getAccessToken();
        String url = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + token;
        JSONObject payload = new JSONObject();
        payload.put("touser", openid);
        payload.put("template_id", templateId);
        payload.put("page", page == null ? "" : page);
        JSONObject dataJson = new JSONObject();
        if (data != null)
        {
            for (Map.Entry<String, String> entry : data.entrySet())
            {
                JSONObject item = new JSONObject();
                item.put("value", entry.getValue());
                dataJson.put(entry.getKey(), item);
            }
        }
        payload.put("data", dataJson);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = restTemplate.postForObject(url, new HttpEntity<String>(payload.toJSONString(), headers), String.class);
        JSONObject result = JSON.parseObject(body);
        if (result == null || result.getIntValue("errcode", 0) != 0)
        {
            String errmsg = result == null ? "无响应" : result.getString("errmsg");
            log.warn("订阅消息发送失败 openid={} errmsg={}", openid, errmsg);
            throw new ServiceException("订阅消息发送失败：" + errmsg);
        }
    }

    private void checkConfig()
    {
        if (!properties.isEnabled() || properties.isMock())
        {
            throw new ServiceException("微信小程序接口未启用");
        }
        if (StringUtils.isEmpty(properties.getAppId()) || StringUtils.isEmpty(properties.getAppSecret()))
        {
            throw new ServiceException("未配置微信小程序 AppID/AppSecret");
        }
    }
}
