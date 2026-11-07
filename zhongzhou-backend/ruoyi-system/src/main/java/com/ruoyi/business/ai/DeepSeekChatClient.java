package com.ruoyi.business.ai;

import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

/**
 * DeepSeek（OpenAI 兼容）智能客服客户端
 *
 * @author ruoyi
 */
public class DeepSeekChatClient implements ChatClient
{
    private final AiChatProperties.DeepSeek config;

    private final RestTemplate restTemplate;

    public DeepSeekChatClient(AiChatProperties properties)
    {
        this.config = properties.getDeepseek();
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(config.getTimeoutSeconds() * 1000);
        factory.setReadTimeout(config.getTimeoutSeconds() * 1000);
        this.restTemplate = new RestTemplate(factory);
    }

    @Override
    public String chat(String systemPrompt, List<ChatMessageVO> history, String userContent)
    {
        if (StringUtils.isEmpty(config.getApiKey()))
        {
            throw new ServiceException("DeepSeek API Key 未配置");
        }
        JSONObject body = new JSONObject();
        body.put("model", config.getModel());
        List<JSONObject> messages = new ArrayList<JSONObject>();
        messages.add(message("system", systemPrompt));
        if (history != null)
        {
            for (ChatMessageVO item : history)
            {
                messages.add(message(item.getRole(), item.getContent()));
            }
        }
        messages.add(message("user", userContent));
        body.put("messages", messages);
        body.put("temperature", config.getTemperature());
        body.put("max_tokens", config.getMaxTokens());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(config.getApiKey());
        HttpEntity<String> entity = new HttpEntity<String>(body.toJSONString(), headers);

        try
        {
            ResponseEntity<String> response = restTemplate.exchange(config.getEndpoint(), HttpMethod.POST, entity,
                    String.class);
            return parseContent(response.getBody());
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ServiceException("DeepSeek 接口调用失败：" + truncate(e.getMessage(), 200));
        }
    }

    private JSONObject message(String role, String content)
    {
        JSONObject message = new JSONObject();
        message.put("role", role);
        message.put("content", content);
        return message;
    }

    private String parseContent(String responseBody)
    {
        if (StringUtils.isEmpty(responseBody))
        {
            throw new ServiceException("DeepSeek 接口返回内容为空");
        }
        JSONObject result = JSON.parseObject(responseBody);
        JSONArray choices = result.getJSONArray("choices");
        if (choices == null || choices.isEmpty())
        {
            throw new ServiceException("DeepSeek 接口返回格式异常");
        }
        JSONObject first = choices.getJSONObject(0);
        JSONObject message = first.getJSONObject("message");
        String content = message == null ? null : message.getString("content");
        if (StringUtils.isEmpty(content))
        {
            throw new ServiceException("DeepSeek 接口未返回回复内容");
        }
        return content;
    }

    private String truncate(String text, int maxLength)
    {
        if (text == null)
        {
            return "";
        }
        return text.length() <= maxLength ? text : text.substring(0, maxLength) + "...";
    }
}
