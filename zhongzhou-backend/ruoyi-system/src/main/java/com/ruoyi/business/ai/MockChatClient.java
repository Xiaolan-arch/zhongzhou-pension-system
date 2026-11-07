package com.ruoyi.business.ai;

import java.util.List;
import com.ruoyi.common.utils.StringUtils;

/**
 * 本地规则模拟的智能客服客户端，用于无大模型凭据时保证可演示。
 * 按关键词从 systemPrompt 中注入的业务上下文段落提取信息回答。
 *
 * @author ruoyi
 */
public class MockChatClient implements ChatClient
{
    /** 上下文段落标题（与 ChatServiceImpl 拼装保持一致） */
    private static final String SECTION_ELDER = "【老人信息】";
    private static final String SECTION_BILL = "【近期账单】";
    private static final String SECTION_SERVICE = "【近期服务】";

    @Override
    public String chat(String systemPrompt, List<ChatMessageVO> history, String userContent)
    {
        String question = userContent == null ? "" : userContent.trim();
        String elderSection = extractSection(systemPrompt, SECTION_ELDER);
        String billSection = extractSection(systemPrompt, SECTION_BILL);
        String serviceSection = extractSection(systemPrompt, SECTION_SERVICE);

        if (StringUtils.isEmpty(question))
        {
            return "您好，我是中州颐养 AI 客服，可以为您解答老人照护、费用账单、服务记录等问题，请问有什么可以帮您？";
        }
        if (matches(question, "账单", "费用", "缴费", "欠费", "多少钱", "收费"))
        {
            return replyWithSection(billSection,
                    "以下是系统中查到的近期账单信息：\n",
                    "暂未查询到相关账单记录。如需核实费用明细，可拨打护理站电话进一步咨询。");
        }
        if (matches(question, "服务", "护理", "照护", "订单", "项目"))
        {
            return replyWithSection(serviceSection,
                    "以下是系统中查到的近期服务记录：\n",
                    "暂未查询到近期服务记录。如需安排服务，可在小程序「服务」页面预约，或联系护理站。");
        }
        if (matches(question, "房间", "床位", "住哪", "住在", "楼层", "几号"))
        {
            return replyWithSection(elderSection,
                    "以下是老人的在住信息：\n",
                    "暂未查询到床位安排信息，请联系护理站确认。");
        }
        if (matches(question, "探访", "探视", "看望", "参观"))
        {
            return "您可以在小程序首页点击「探访预约」提交申请，选择到访日期与人数即可，护理站会尽快确认。"
                    + "到院时请出示预约码供工作人员核销。";
        }
        if (matches(question, "请假", "外出", "回家"))
        {
            return "您可以在小程序「我的-请假外出」中提交申请，填写外出时间与事由，院方审批通过后老人即可外出。"
                    + "返回后请及时办理销假手续。";
        }
        if (matches(question, "发票", "票据", "报销"))
        {
            return "已缴清的账单可以在小程序「我的-申请发票」中在线申请开票，填写发票抬头与接收邮箱即可，"
                    + "院方处理完成后会发送电子发票。";
        }
        if (matches(question, "身体", "健康", "血压", "血糖", "体征", "心率"))
        {
            return "您可以在小程序「体征查询」中查看老人最近一段时间的血压、心率、血糖等体征记录，"
                    + "「健康报告」中还有 AI 生成的周期性照护评估。具体诊疗问题请咨询医护人员。";
        }
        if (matches(question, "你好", "您好", "在吗", "hi", "hello"))
        {
            return "您好，我是中州颐养 AI 客服，可以为您解答老人照护、费用账单、服务记录、探访预约等问题，请问有什么可以帮您？";
        }
        return "抱歉，这个问题我暂时无法准确回答。您可以尝试询问老人照护、费用账单、服务记录、探访预约等问题，"
                + "或直接拨打护理站电话咨询。";
    }

    private boolean matches(String question, String... keywords)
    {
        for (String keyword : keywords)
        {
            if (question.contains(keyword))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * 从 systemPrompt 中提取指定段落（标题行到下一个标题或结尾）。
     * 使用 lastIndexOf 跳过约束文本中可能出现的同名引用，定位到真正的数据段落。
     */
    private String extractSection(String systemPrompt, String sectionTitle)
    {
        if (StringUtils.isEmpty(systemPrompt))
        {
            return "";
        }
        int start = systemPrompt.lastIndexOf(sectionTitle);
        if (start < 0)
        {
            return "";
        }
        int end = systemPrompt.indexOf("【", start + sectionTitle.length());
        String section = end < 0 ? systemPrompt.substring(start) : systemPrompt.substring(start, end);
        return section.trim();
    }

    private String replyWithSection(String section, String prefix, String emptyHint)
    {
        if (StringUtils.isEmpty(section) || section.contains("暂无"))
        {
            return emptyHint;
        }
        return prefix + section;
    }
}
