package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.Nurse;

/**
 * 护理员 数据层
 *
 * @author ruoyi
 */
public interface NurseMapper
{
    public List<Nurse> selectNurseList(Nurse nurse);

    public Nurse selectNurseById(Long nurseId);

    public int insertNurse(Nurse nurse);

    public int updateNurse(Nurse nurse);

    public int deleteNurseByIds(Long[] nurseIds);
}
