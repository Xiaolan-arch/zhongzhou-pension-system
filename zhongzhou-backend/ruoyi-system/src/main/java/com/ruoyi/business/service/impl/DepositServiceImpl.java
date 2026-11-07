package com.ruoyi.business.service.impl;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.audit.BizAudit;
import com.ruoyi.business.domain.Deposit;
import com.ruoyi.business.mapper.DepositMapper;
import com.ruoyi.business.service.IDepositService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;

/**
 * 押金 服务层处理
 */
@Service
public class DepositServiceImpl implements IDepositService
{
    @Autowired
    private DepositMapper depositMapper;

    @Override
    public List<Deposit> selectDepositList(Deposit deposit)
    {
        return depositMapper.selectDepositList(deposit);
    }

    /** 收押金 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @BizAudit(value = "DEPOSIT_COLLECT", action = "收押金")
    public int collectDeposit(Deposit deposit)
    {
        validateCommon(deposit);
        deposit.setDepositType("1");
        fillOperator(deposit);
        return depositMapper.insertDeposit(deposit);
    }

    /** 退押金：金额不得超过当前可退余额 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @BizAudit(value = "DEPOSIT_REFUND", action = "退押金")
    public int refundDeposit(Deposit deposit)
    {
        validateCommon(deposit);
        BigDecimal balance = depositMapper.selectElderDepositBalance(deposit.getElderId());
        if (balance == null)
        {
            balance = BigDecimal.ZERO;
        }
        if (deposit.getAmount().compareTo(balance) > 0)
        {
            throw new ServiceException("退款金额（" + deposit.getAmount() + "）超过可退押金余额（" + balance + "）");
        }
        deposit.setDepositType("2");
        fillOperator(deposit);
        return depositMapper.insertDeposit(deposit);
    }

    @Override
    public BigDecimal selectElderDepositBalance(Long elderId)
    {
        if (elderId == null)
        {
            throw new ServiceException("老人ID不能为空");
        }
        BigDecimal balance = depositMapper.selectElderDepositBalance(elderId);
        return balance == null ? BigDecimal.ZERO : balance;
    }

    private void validateCommon(Deposit deposit)
    {
        if (deposit == null || deposit.getElderId() == null)
        {
            throw new ServiceException("请选择老人");
        }
        if (deposit.getAmount() == null || deposit.getAmount().compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("金额必须大于0");
        }
        if (deposit.getElderName() == null || deposit.getElderName().trim().isEmpty())
        {
            throw new ServiceException("老人姓名不能为空");
        }
    }

    private void fillOperator(Deposit deposit)
    {
        deposit.setTenantId(SecurityUtils.getTenantId());
        deposit.setOperatorId(SecurityUtils.getUserId());
        deposit.setOperatorName(SecurityUtils.getUsername());
        deposit.setCreateBy(SecurityUtils.getUsername());
        deposit.setElderName(deposit.getElderName().trim());
    }
}
