package com.ruoyi.business.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.ai.ChatClient;
import com.ruoyi.business.ai.ChatMessageVO;
import com.ruoyi.business.domain.Bill;
import com.ruoyi.business.domain.ChatMessage;
import com.ruoyi.business.domain.ChatSession;
import com.ruoyi.business.domain.FamilyToken;
import com.ruoyi.business.domain.ServiceOrder;
import com.ruoyi.business.domain.vo.FamilyElderVO;
import com.ruoyi.business.mapper.ChatMapper;
import com.ruoyi.business.service.IChatService;
import com.ruoyi.business.service.IFamilyPortalService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

/**
 * AI 智能客服服务实现
 *
 * @author ruoyi
 */
@Service
public class ChatServiceImpl implements IChatService
{
    /** 单次消息长度上限 */
    private static final int MAX_CONTENT_LENGTH = 500;

    /** 会话标题长度上限 */
    private static final int MAX_TITLE_LENGTH = 20;

    /** 注入上下文的历史消息条数上限 */
    private static final int HISTORY_LIMIT = 10;

    /** 注入上下文的账单/服务条数上限 */
    private static final int BIZ_LIMIT = 5;

    @Autowired
    private ChatMapper chatMapper;

    @Autowired
    private IFamilyPortalService familyPortalService;

    @Autowired
    private ChatClient chatClient;

    @Override
    @Transactional
    public Map<String, Object> chat(FamilyToken family, Long sessionId, Long elderId, String content)
    {
        if (StringUtils.isEmpty(content) || StringUtils.isEmpty(content.trim()))
        {
            throw new ServiceException("消息内容不能为空");
        }
        content = content.trim();
        if (content.length() > MAX_CONTENT_LENGTH)
        {
            throw new ServiceException("消息内容过长，请控制在 " + MAX_CONTENT_LENGTH + " 字以内");
        }

        // 1. 获取或新建会话（带归属校验）
        ChatSession session = resolveSession(family, sessionId, elderId, content);

        // 2. 查询业务上下文（复用家属端查询，自带归属校验）
        String systemPrompt = buildSystemPrompt(family, elderId);

        // 3. 组装历史消息（最近 N 条）
        List<ChatMessage> historyEntities = chatMapper.selectMessageList(session.getSessionId(), null);
        List<ChatMessageVO> history = new ArrayList<ChatMessageVO>();
        int from = Math.max(0, historyEntities.size() - HISTORY_LIMIT);
        for (ChatMessage item : historyEntities.subList(from, historyEntities.size()))
        {
            history.add(new ChatMessageVO(item.getRole(), item.getContent()));
        }

        // 4. 调用大模型
        String reply = chatClient.chat(systemPrompt, history, content);

        // 5. 落库 user + assistant 两条消息
        ChatMessage userMsg = new ChatMessage();
        userMsg.setSessionId(session.getSessionId());
        userMsg.setRole("user");
        userMsg.setContent(content);
        chatMapper.insertMessage(userMsg);

        ChatMessage assistantMsg = new ChatMessage();
        assistantMsg.setSessionId(session.getSessionId());
        assistantMsg.setRole("assistant");
        assistantMsg.setContent(reply);
        chatMapper.insertMessage(assistantMsg);

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("sessionId", session.getSessionId());
        result.put("reply", reply);
        return result;
    }

    @Override
    public List<ChatSession> listSessions(FamilyToken family)
    {
        ChatSession query = new ChatSession();
        query.setTenantId(family.getTenantId());
        query.setFamilyId(family.getFamilyId());
        return chatMapper.selectSessionList(query);
    }

    @Override
    public List<ChatMessage> listMessages(FamilyToken family, Long sessionId)
    {
        if (sessionId == null)
        {
            throw new ServiceException("会话ID不能为空");
        }
        checkSessionOwnership(family, sessionId);
        List<ChatMessage> list = chatMapper.selectMessageList(sessionId, null);
        return list == null ? Collections.emptyList() : list;
    }

    /**
     * 获取已有会话（校验归属）或新建会话
     */
    private ChatSession resolveSession(FamilyToken family, Long sessionId, Long elderId, String content)
    {
        if (sessionId != null)
        {
            ChatSession session = checkSessionOwnership(family, sessionId);
            // 切换咨询老人时同步更新会话
            if (elderId != null && !elderId.equals(session.getElderId()))
            {
                ChatSession update = new ChatSession();
                update.setSessionId(sessionId);
                update.setElderId(elderId);
                chatMapper.updateSession(update);
                session.setElderId(elderId);
            }
            return session;
        }
        ChatSession session = new ChatSession();
        session.setTenantId(family.getTenantId());
        session.setFamilyId(family.getFamilyId());
        session.setElderId(elderId);
        session.setTitle(content.length() <= MAX_TITLE_LENGTH ? content : content.substring(0, MAX_TITLE_LENGTH));
        session.setCreateBy(family.getFamilyName());
        chatMapper.insertSession(session);
        return session;
    }

    /**
     * 校验会话归属当前家属
     */
    private ChatSession checkSessionOwnership(FamilyToken family, Long sessionId)
    {
        ChatSession session = chatMapper.selectSessionById(sessionId);
        if (session == null || !session.getFamilyId().equals(family.getFamilyId())
                || !session.getTenantId().equals(family.getTenantId()))
        {
            throw new ServiceException("会话不存在或无权访问");
        }
        return session;
    }

    /**
     * 组装系统提示词（角色约束 + 业务上下文）
     */
    private String buildSystemPrompt(FamilyToken family, Long elderId)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("你是中州颐养养老院的 AI 智能客服「小颐」，服务对象是在住老人的家属。\n\n");
        sb.append("【回答约束】\n");
        sb.append("1. 仅依据下方提供的老人、账单、服务等业务数据回答，不得编造数据中没有的信息。\n");
        sb.append("2. 数据中没有的内容，回复「该信息暂未在系统中查到」，并引导家属拨打护理站电话咨询。\n");
        sb.append("3. 不做医疗诊断，涉及诊疗建议的问题请引导家属联系医护人员。\n");
        sb.append("4. 回答简洁口语化，每次不超过 200 字，可适当换行分点。\n\n");

        sb.append("【老人信息】\n");
        FamilyElderVO elder = null;
        if (elderId != null)
        {
            // selectElderDetail 自带归属校验，越权会抛异常
            elder = familyPortalService.selectElderDetail(family, elderId);
        }
        if (elder == null)
        {
            List<FamilyElderVO> elders = familyPortalService.selectElders(family);
            if (elders == null || elders.isEmpty())
            {
                sb.append("该家属暂未绑定老人档案。\n\n");
                return sb.toString();
            }
            StringBuilder names = new StringBuilder();
            for (FamilyElderVO item : elders)
            {
                if (names.length() > 0)
                {
                    names.append("、");
                }
                names.append(item.getElderName());
            }
            sb.append("家属未指定具体咨询的老人。其绑定的老人有：").append(names).append("。\n\n");
            return sb.toString();
        }

        sb.append("- 姓名：").append(elder.getElderName());
        sb.append("（").append("1".equals(elder.getGender()) ? "女" : "男");
        if (elder.getAge() != null)
        {
            sb.append("，").append(elder.getAge()).append(" 岁");
        }
        sb.append("）\n");
        sb.append("- 护理等级：").append(careLevelName(elder.getCareLevel())).append("\n");
        if (elder.getCheckinDate() != null)
        {
            sb.append("- 入住日期：").append(new SimpleDateFormat("yyyy-MM-dd").format(elder.getCheckinDate())).append("\n");
        }
        if (StringUtils.isNotEmpty(elder.getRoomNo()))
        {
            sb.append("- 床位：");
            if (elder.getFloorNo() != null)
            {
                sb.append(elder.getFloorNo()).append("层 ");
            }
            sb.append(elder.getRoomNo()).append("房");
            if (StringUtils.isNotEmpty(elder.getBedNo()))
            {
                sb.append(" ").append(elder.getBedNo()).append("床");
            }
            sb.append("\n");
        }
        if (StringUtils.isNotEmpty(elder.getNurseNames()))
        {
            sb.append("- 护理员：").append(elder.getNurseNames()).append("\n");
        }
        sb.append("\n");

        sb.append("【近期账单】\n");
        List<Bill> bills = familyPortalService.selectBills(family, elderId);
        if (bills == null || bills.isEmpty())
        {
            sb.append("暂无\n\n");
        }
        else
        {
            int count = 0;
            for (Bill bill : bills)
            {
                if (++count > BIZ_LIMIT)
                {
                    break;
                }
                sb.append("- ").append(bill.getBillMonth() == null ? "账单" : bill.getBillMonth() + "账单");
                sb.append("：金额 ").append(bill.getAmount() == null ? "0.00" : bill.getAmount().toPlainString()).append(" 元");
                sb.append("，").append(payStatusName(bill.getPayStatus()));
                if ("0".equals(bill.getPayStatus()) && bill.getPaidAmount() != null
                        && bill.getPaidAmount().signum() > 0)
                {
                    sb.append("（已付 ").append(bill.getPaidAmount().toPlainString()).append(" 元）");
                }
                sb.append("\n");
            }
            sb.append("\n");
        }

        sb.append("【近期服务】\n");
        List<ServiceOrder> orders = familyPortalService.selectServices(family, elderId);
        if (orders == null || orders.isEmpty())
        {
            sb.append("暂无\n");
        }
        else
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            int count = 0;
            for (ServiceOrder order : orders)
            {
                if (++count > BIZ_LIMIT)
                {
                    break;
                }
                sb.append("- ");
                if (order.getServiceTime() != null)
                {
                    sb.append(sdf.format(order.getServiceTime())).append(" ");
                }
                sb.append(order.getItemName() == null ? "护理服务" : order.getItemName());
                sb.append("（").append(orderStatusName(order.getStatus()));
                if (StringUtils.isNotEmpty(order.getNurseName()))
                {
                    sb.append("，护理员：").append(order.getNurseName());
                }
                sb.append("）\n");
            }
        }
        return sb.toString();
    }

    private static String careLevelName(String careLevel)
    {
        if ("1".equals(careLevel)) return "一级护理（自理）";
        if ("2".equals(careLevel)) return "二级护理（半自理）";
        if ("3".equals(careLevel)) return "三级护理（全护理）";
        if ("4".equals(careLevel)) return "特级护理";
        return "未评级";
    }

    private static String payStatusName(String payStatus)
    {
        if ("1".equals(payStatus)) return "已支付";
        if ("2".equals(payStatus)) return "已退款";
        return "待支付";
    }

    private static String orderStatusName(String status)
    {
        if ("0".equals(status)) return "待服务";
        if ("1".equals(status)) return "服务中";
        if ("2".equals(status)) return "已完成";
        if ("3".equals(status)) return "已取消";
        return "未知状态";
    }
}
