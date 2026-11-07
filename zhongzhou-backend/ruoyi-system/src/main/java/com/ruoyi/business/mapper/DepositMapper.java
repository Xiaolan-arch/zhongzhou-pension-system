package com.ruoyi.business.mapper;

import java.math.BigDecimal;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.Deposit;

/**
 * 押金记录 数据层
 */
public interface DepositMapper
{
    /** 查询押金记录列表 */
    public List<Deposit> selectDepositList(Deposit deposit);

    /** 新增押金记录 */
    public int insertDeposit(Deposit deposit);

    /** 查询老人在该机构的押金余额（收押金累计 - 退押金累计） */
    public BigDecimal selectElderDepositBalance(@Param("elderId") Long elderId);
}
