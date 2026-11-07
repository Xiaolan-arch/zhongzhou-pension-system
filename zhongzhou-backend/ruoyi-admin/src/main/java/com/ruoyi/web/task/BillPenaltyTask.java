package com.ruoyi.web.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.ruoyi.business.service.IBillService;
import com.ruoyi.business.service.IWeChatMessageService;

/**
 * 逾期账单滞纳金重算任务
 *
 * 每天凌晨 2:30 重新计算未结清逾期账单的滞纳金并落库。
 * 滞纳金 = 未缴金额 × 日费率 × 逾期天数（超过宽限期的天数）。
 *
 * @author ruoyi
 */
@Component
public class BillPenaltyTask
{
    private static final Logger log = LoggerFactory.getLogger(BillPenaltyTask.class);

    @Autowired
    private IBillService billService;

    @Autowired
    private IWeChatMessageService weChatMessageService;

    @Scheduled(cron = "0 30 2 * * ?")
    public void refreshPenalty()
    {
        try
        {
            int count = billService.refreshOverduePenalty();
            if (count > 0)
            {
                log.info("逾期账单滞纳金重算完成，共更新 {} 条账单", count);
            }
            // 重算后向逾期账单家属推送提醒（同一账单 24 小时内只推一次）
            int msgCount = weChatMessageService.notifyOverdueBills();
            if (msgCount > 0)
            {
                log.info("逾期账单微信提醒发送完成，共 {} 条", msgCount);
            }
        }
        catch (Exception e)
        {
            log.error("逾期账单滞纳金重算失败", e);
        }
    }
}
