package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.CareLog;

/**
 * 护理日志 数据层
 *
 * @author ruoyi
 */
public interface CareLogMapper
{
    public List<CareLog> selectCareLogList(CareLog careLog);

    public CareLog selectCareLogById(Long logId);

    public int insertCareLog(CareLog careLog);

    public int updateCareLog(CareLog careLog);

    public int deleteCareLogByIds(Long[] logIds);
}
