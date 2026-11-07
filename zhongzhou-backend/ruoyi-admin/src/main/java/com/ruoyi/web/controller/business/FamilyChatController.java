package com.ruoyi.web.controller.business;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.domain.ChatMessage;
import com.ruoyi.business.domain.ChatSession;
import com.ruoyi.business.domain.FamilyToken;
import com.ruoyi.business.service.IChatService;
import com.ruoyi.business.service.IFamilyAuthService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;

/**
 * 家属端 AI 智能客服
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/family/chat")
public class FamilyChatController extends BaseController
{
    @Autowired
    private IFamilyAuthService familyAuthService;

    @Autowired
    private IChatService chatService;

    private FamilyToken auth(String token)
    {
        return familyAuthService.authenticate(token);
    }

    /**
     * 会话列表
     */
    @GetMapping("/sessions")
    public AjaxResult sessions(@RequestHeader(value = "X-Family-Token", required = false) String token)
    {
        List<ChatSession> list = chatService.listSessions(auth(token));
        return AjaxResult.success(list);
    }

    /**
     * 会话消息历史
     */
    @GetMapping("/sessions/{sessionId}/messages")
    public AjaxResult messages(@RequestHeader(value = "X-Family-Token", required = false) String token,
            @PathVariable Long sessionId)
    {
        List<ChatMessage> list = chatService.listMessages(auth(token), sessionId);
        return AjaxResult.success(list);
    }

    /**
     * 发送消息并获取 AI 回复
     */
    @PostMapping("/send")
    public AjaxResult send(@RequestHeader(value = "X-Family-Token", required = false) String token,
            @RequestBody Map<String, Object> body)
    {
        Long sessionId = body.get("sessionId") == null ? null : Long.valueOf(body.get("sessionId").toString());
        Long elderId = body.get("elderId") == null ? null : Long.valueOf(body.get("elderId").toString());
        String content = body.get("content") == null ? null : body.get("content").toString();
        return AjaxResult.success(chatService.chat(auth(token), sessionId, elderId, content));
    }
}
