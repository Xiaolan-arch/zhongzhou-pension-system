package com.ruoyi.business.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.Elder;
import com.ruoyi.business.domain.HealthRecord;
import com.ruoyi.business.mapper.ElderMapper;
import com.ruoyi.business.mapper.HealthRecordMapper;
import com.ruoyi.business.service.IHealthRecordService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;

/**
 * 老人体征记录 Service 实现
 */
@Service
public class HealthRecordServiceImpl implements IHealthRecordService
{
    @Autowired
    private HealthRecordMapper healthRecordMapper;

    @Autowired
    private ElderMapper elderMapper;

    @Override
    public List<HealthRecord> selectHealthRecordList(HealthRecord healthRecord)
    {
        return healthRecordMapper.selectHealthRecordList(healthRecord);
    }

    @Override
    public HealthRecord selectHealthRecordById(Long recordId)
    {
        return healthRecordMapper.selectHealthRecordById(recordId);
    }

    @Override
    public List<HealthRecord> selectRecentByElderId(Long elderId, int limit)
    {
        return healthRecordMapper.selectRecentByElderId(elderId, limit);
    }

    @Override
    public int insertHealthRecord(HealthRecord healthRecord)
    {
        if (healthRecord.getElderId() == null)
        {
            throw new ServiceException("老人ID不能为空");
        }
        Elder elder = elderMapper.selectElderById(healthRecord.getElderId());
        if (elder == null)
        {
            throw new ServiceException("老人不存在");
        }
        if (healthRecord.getRecordDate() == null)
        {
            healthRecord.setRecordDate(new java.util.Date());
        }
        if (StringUtils.isBlank(healthRecord.getRecordTime()))
        {
            healthRecord.setRecordTime(new java.text.SimpleDateFormat("HH:mm").format(new java.util.Date()));
        }
        healthRecord.setTenantId(SecurityUtils.getTenantId());
        healthRecord.setCreateBy(SecurityUtils.getUsername());
        return healthRecordMapper.insertHealthRecord(healthRecord);
    }

    @Override
    public int updateHealthRecord(HealthRecord healthRecord)
    {
        healthRecord.setUpdateBy(SecurityUtils.getUsername());
        return healthRecordMapper.updateHealthRecord(healthRecord);
    }

    @Override
    public int deleteHealthRecordByIds(Long[] recordIds)
    {
        return healthRecordMapper.deleteHealthRecordByIds(recordIds);
    }

    @Override
    public int deleteHealthRecordById(Long recordId)
    {
        return healthRecordMapper.deleteHealthRecordById(recordId);
    }

    @Override
    public Long countAbnormalHealth()
    {
        return healthRecordMapper.countAbnormalHealth();
    }
}
