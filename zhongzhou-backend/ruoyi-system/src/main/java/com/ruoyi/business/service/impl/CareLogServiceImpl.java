package com.ruoyi.business.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.CareLog;
import com.ruoyi.business.domain.Elder;
import com.ruoyi.business.domain.FamilyToken;
import com.ruoyi.business.domain.vo.FamilyElderVO;
import com.ruoyi.business.mapper.CareLogMapper;
import com.ruoyi.business.mapper.ElderMapper;
import com.ruoyi.business.mapper.FamilyMapper;
import com.ruoyi.business.service.ICareLogService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;

/**
 * 护理日志 服务层处理
 *
 * @author ruoyi
 */
@Service
public class CareLogServiceImpl implements ICareLogService
{
    @Autowired
    private CareLogMapper careLogMapper;

    @Autowired
    private ElderMapper elderMapper;

    @Autowired
    private FamilyMapper familyMapper;

    @Override
    public List<CareLog> selectCareLogList(CareLog careLog)
    {
        return careLogMapper.selectCareLogList(careLog);
    }

    @Override
    public CareLog selectCareLogById(Long logId)
    {
        return careLogMapper.selectCareLogById(logId);
    }

    @Override
    public int insertCareLog(CareLog careLog)
    {
        fillElderName(careLog);
        if (StringUtils.isNull(careLog.getTenantId()))
        {
            careLog.setTenantId(SecurityUtils.getTenantId());
        }
        if (careLog.getLogDate() == null)
        {
            careLog.setLogDate(DateUtils.getNowDate());
        }
        careLog.setCreateBy(SecurityUtils.getUsername());
        careLog.setCreateTime(DateUtils.getNowDate());
        return careLogMapper.insertCareLog(careLog);
    }

    @Override
    public int updateCareLog(CareLog careLog)
    {
        fillElderName(careLog);
        careLog.setUpdateBy(SecurityUtils.getUsername());
        return careLogMapper.updateCareLog(careLog);
    }

    @Override
    public int deleteCareLogByIds(Long[] logIds)
    {
        return careLogMapper.deleteCareLogByIds(logIds);
    }

    @Override
    public List<CareLog> selectFamilyCareLogs(FamilyToken family, Long elderId)
    {
        checkElderOwnership(family, elderId);
        CareLog query = new CareLog();
        query.setElderId(elderId);
        query.setTenantId(family.getTenantId());
        return careLogMapper.selectCareLogList(query);
    }

    private void fillElderName(CareLog careLog)
    {
        if (careLog.getElderId() != null)
        {
            Elder elder = elderMapper.selectElderById(careLog.getElderId());
            if (elder != null)
            {
                careLog.setElderName(elder.getElderName());
            }
        }
    }

    private FamilyElderVO checkElderOwnership(FamilyToken family, Long elderId)
    {
        if (elderId == null)
        {
            throw new ServiceException("请选择老人");
        }
        FamilyElderVO elder = familyMapper.selectElderDetail(family.getPhone(), family.getTenantId(), elderId);
        if (elder == null)
        {
            throw new ServiceException("无权查看该老人信息");
        }
        return elder;
    }
}
