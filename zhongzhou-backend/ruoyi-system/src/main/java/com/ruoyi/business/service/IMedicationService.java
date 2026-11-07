package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.FamilyToken;
import com.ruoyi.business.domain.Medication;

/**
 * 用药提醒 服务层
 *
 * @author ruoyi
 */
public interface IMedicationService
{
    public List<Medication> selectMedicationList(Medication medication);

    public Medication selectMedicationById(Long medicationId);

    public int insertMedication(Medication medication);

    public int updateMedication(Medication medication);

    public int deleteMedicationByIds(Long[] medicationIds);

    // ====================== 家属端 ======================

    public List<Medication> selectFamilyMedications(FamilyToken family, Long elderId);
}
