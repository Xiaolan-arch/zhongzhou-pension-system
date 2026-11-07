package com.ruoyi.business.service;

/**
 * 微信订阅消息服务
 */
public interface IWeChatMessageService
{
    /**
     * 向账单对应老人的全部家属发送提醒
     *
     * @param billId 账单ID
     * @param msgType 消息类型（1账单到期 2逾期提醒）
     * @return 发送记录条数
     */
    public int notifyBill(Long billId, String msgType);

    /**
     * 对所有逾期未结清账单发送逾期提醒（同一账单 24 小时内只发一次）
     *
     * @return 处理的账单数
     */
    public int notifyOverdueBills();
}
