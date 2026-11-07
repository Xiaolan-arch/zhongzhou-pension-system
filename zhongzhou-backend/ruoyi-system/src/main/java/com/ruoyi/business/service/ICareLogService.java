package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.CareLog;
import com.ruoyi.business.domain.FamilyToken;

/**
 * 护理日志 服务层
 *
 * @author ruoyi
 */
public interface ICareLogService
{
    public List<CareLog> selectCareLogList(CareLog careLog);

    public CareLog selectCareLogById(Long logId);

    public int insertCareLog(CareLog careLog);

    public int updateCareLog(CareLog careLog);

    public int deleteCareLogByIds(Long[] logIds);

    // ====================== 家属端 ======================

    public List<CareLog> selectFamilyCareLogs(FamilyToken family, Long elderId);
}
