package com.ruoyi.business.service.impl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.ruoyi.business.config.BillOverdueProperties;
import com.ruoyi.business.config.WxMiniAppProperties;
import com.ruoyi.business.domain.Bill;
import com.ruoyi.business.domain.FamilyMember;
import com.ruoyi.business.domain.MessageLog;
import com.ruoyi.business.mapper.BillMapper;
import com.ruoyi.business.mapper.FamilyMapper;
import com.ruoyi.business.mapper.MessageLogMapper;
import com.ruoyi.business.service.IWeChatMessageService;
import com.ruoyi.business.wx.WxMiniAppClient;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

/**
 * 微信订阅消息服务实现
 *
 * 默认 Mock 模式：只写入 biz_message_log 发送记录，不真实调用微信接口；
 * 配置 wx.miniapp.enabled=true 且 mock=false 后走真实推送。
 */
@Service
public class WeChatMessageServiceImpl implements IWeChatMessageService
{
    private static final Logger log = LoggerFactory.getLogger(WeChatMessageServiceImpl.class);

    /** 逾期提醒去重 key 前缀（24 小时） */
    private static final String OVERDUE_NOTIFY_KEY = "wx:overdue:notify:";

    @Autowired
    private FamilyMapper familyMapper;

    @Autowired
    private BillMapper billMapper;

    @Autowired
    private MessageLogMapper messageLogMapper;

    @Autowired
    private WxMiniAppProperties wxProperties;

    @Autowired
    private WxMiniAppClient wxClient;

    @Autowired
    private BillOverdueProperties overdueProperties;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public int notifyBill(Long billId, String msgType)
    {
        if (billId == null)
        {
            throw new ServiceException("账单ID不能为空");
        }
        Bill bill = billMapper.selectBillById(billId);
        if (bill == null)
        {
            throw new ServiceException("账单不存在");
        }
        String type = StringUtils.isEmpty(msgType) ? "1" : msgType;
        return sendToFamilies(bill, type);
    }

    @Override
    public int notifyOverdueBills()
    {
        if (!overdueProperties.isEnabled())
        {
            return 0;
        }
        List<Bill> bills = billMapper.selectBillsForPenalty(overdueProperties.getGraceDays());
        int count = 0;
        for (Bill bill : bills)
        {
            String key = OVERDUE_NOTIFY_KEY + bill.getBillId();
            Boolean acquired = redisTemplate.opsForValue().setIfAbsent(key, "1", 24, TimeUnit.HOURS);
            if (acquired != null && acquired)
            {
                count += sendToFamilies(bill, "2");
            }
        }
        return count;
    }

    /**
     * 向账单所属老人的全部家属发送提醒并记录
     */
    private int sendToFamilies(Bill bill, String msgType)
    {
        if (bill.getTenantId() == null || bill.getElderId() == null)
        {
            return 0;
        }
        List<FamilyMember> families = familyMapper.selectFamiliesByElder(bill.getTenantId(), bill.getElderId());
        if (families == null || families.isEmpty())
        {
            log.debug("老人 {} 暂无绑定家属，跳过消息发送", bill.getElderName());
            return 0;
        }
        BigDecimal unpaid = nvl(bill.getAmount()).subtract(nvl(bill.getPrepaid())).subtract(nvl(bill.getPaidAmount()));
        if (unpaid.compareTo(BigDecimal.ZERO) < 0)
        {
            unpaid = BigDecimal.ZERO;
        }
        String title = "2".equals(msgType) ? "账单逾期提醒" : "账单缴费提醒";
        String content = "2".equals(msgType)
                ? String.format("%s 的 %s 账单已逾期，待缴 %s 元，请尽快缴纳。",
                        bill.getElderName(), bill.getBillMonth(), unpaid.setScale(2, BigDecimal.ROUND_HALF_UP))
                : String.format("%s 的 %s 账单待缴 %s 元，请及时缴费。",
                        bill.getElderName(), bill.getBillMonth(), unpaid.setScale(2, BigDecimal.ROUND_HALF_UP));

        int count = 0;
        for (FamilyMember family : families)
        {
            sendAndLog(bill, family, msgType, title, content, unpaid);
            count++;
        }
        return count;
    }

    private void sendAndLog(Bill bill, FamilyMember family, String msgType, String title,
                            String content, BigDecimal unpaid)
    {
        MessageLog record = new MessageLog();
        record.setTenantId(bill.getTenantId());
        record.setFamilyId(family.getFamilyId());
        record.setElderId(bill.getElderId());
        record.setElderName(bill.getElderName());
        record.setPhone(family.getPhone());
        record.setOpenid(family.getOpenid());
        record.setMsgType(msgType);
        record.setTitle(title);
        record.setContent(content);
        record.setErrorMsg("");

        try
        {
            if (!wxProperties.isEnabled() || wxProperties.isMock())
            {
                record.setSendStatus("0");
                record.setErrorMsg("Mock 模式：已记录，未实际调用微信接口");
            }
            else if (StringUtils.isEmpty(family.getOpenid()))
            {
                record.setSendStatus("2");
                record.setErrorMsg("家属未绑定微信 openid");
            }
            else
            {
                String templateId = "2".equals(msgType) ? wxProperties.getOverdueTemplate() : wxProperties.getBillDueTemplate();
                if (StringUtils.isEmpty(templateId))
                {
                    record.setSendStatus("2");
                    record.setErrorMsg("未配置消息模板ID");
                }
                else
                {
                    Map<String, String> data = new HashMap<String, String>();
                    data.put("thing1", StringUtils.substring(title, 0, 20));
                    data.put("thing2", StringUtils.substring(bill.getElderName() + " " + bill.getBillMonth(), 0, 20));
                    data.put("amount3", unpaid.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "元");
                    data.put("thing4", StringUtils.substring(content, 0, 20));
                    wxClient.sendSubscribeMessage(family.getOpenid(), templateId,
                            "pages/billDetail/index?billId=" + bill.getBillId(), data);
                    record.setSendStatus("0");
                    record.setErrorMsg("已发送");
                }
            }
        }
        catch (Exception e)
        {
            record.setSendStatus("1");
            record.setErrorMsg(StringUtils.substring(e.getMessage(), 0, 500));
            log.warn("订阅消息发送异常 elder={} phone={}", bill.getElderName(), family.getPhone(), e);
        }
        messageLogMapper.insertMessageLog(record);
    }

    private static BigDecimal nvl(BigDecimal value)
    {
        return value == null ? BigDecimal.ZERO : value;
    }
}
