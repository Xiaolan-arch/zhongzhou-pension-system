package com.ruoyi.business.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.Elder;
import com.ruoyi.business.domain.FamilyToken;
import com.ruoyi.business.domain.Medication;
import com.ruoyi.business.domain.vo.FamilyElderVO;
import com.ruoyi.business.mapper.ElderMapper;
import com.ruoyi.business.mapper.FamilyMapper;
import com.ruoyi.business.mapper.MedicationMapper;
import com.ruoyi.business.service.IMedicationService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;

/**
 * 用药提醒 服务层处理
 *
 * @author ruoyi
 */
@Service
public class MedicationServiceImpl implements IMedicationService
{
    @Autowired
    private MedicationMapper medicationMapper;

    @Autowired
    private ElderMapper elderMapper;

    @Autowired
    private FamilyMapper familyMapper;

    @Override
    public List<Medication> selectMedicationList(Medication medication)
    {
        return medicationMapper.selectMedicationList(medication);
    }

    @Override
    public Medication selectMedicationById(Long medicationId)
    {
        return medicationMapper.selectMedicationById(medicationId);
    }

    @Override
    public int insertMedication(Medication medication)
    {
        fillElderName(medication);
        if (StringUtils.isNull(medication.getTenantId()))
        {
            medication.setTenantId(SecurityUtils.getTenantId());
        }
        if (StringUtils.isEmpty(medication.getStatus()))
        {
            medication.setStatus("0");
        }
        medication.setCreateBy(SecurityUtils.getUsername());
        medication.setCreateTime(DateUtils.getNowDate());
        return medicationMapper.insertMedication(medication);
    }

    @Override
    public int updateMedication(Medication medication)
    {
        fillElderName(medication);
        medication.setUpdateBy(SecurityUtils.getUsername());
        return medicationMapper.updateMedication(medication);
    }

    @Override
    public int deleteMedicationByIds(Long[] medicationIds)
    {
        return medicationMapper.deleteMedicationByIds(medicationIds);
    }

    @Override
    public List<Medication> selectFamilyMedications(FamilyToken family, Long elderId)
    {
        checkElderOwnership(family, elderId);
        Medication query = new Medication();
        query.setElderId(elderId);
        query.setTenantId(family.getTenantId());
        return medicationMapper.selectMedicationList(query);
    }

    private void fillElderName(Medication medication)
    {
        if (medication.getElderId() != null)
        {
            Elder elder = elderMapper.selectElderById(medication.getElderId());
            if (elder != null)
            {
                medication.setElderName(elder.getElderName());
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
