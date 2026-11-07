package com.ruoyi.business.service;

import java.math.BigDecimal;
import java.util.List;
import com.ruoyi.business.domain.Deposit;

/**
 * 押金 服务层
 */
public interface IDepositService
{
    /** 查询押金记录列表 */
    public List<Deposit> selectDepositList(Deposit deposit);

    /** 收押金 */
    public int collectDeposit(Deposit deposit);

    /** 退押金（校验可退余额） */
    public int refundDeposit(Deposit deposit);

    /** 查询老人的押金可退余额 */
    public BigDecimal selectElderDepositBalance(Long elderId);
}
