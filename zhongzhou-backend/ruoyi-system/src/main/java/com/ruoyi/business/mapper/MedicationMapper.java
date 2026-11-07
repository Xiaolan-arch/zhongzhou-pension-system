package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.Medication;

/**
 * 用药提醒 数据层
 *
 * @author ruoyi
 */
public interface MedicationMapper
{
    public List<Medication> selectMedicationList(Medication medication);

    public Medication selectMedicationById(Long medicationId);

    public int insertMedication(Medication medication);

    public int updateMedication(Medication medication);

    public int deleteMedicationByIds(Long[] medicationIds);
}
