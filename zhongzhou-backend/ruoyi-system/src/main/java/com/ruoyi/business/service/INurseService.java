package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.Nurse;

/**
 * 护理员 服务层
 *
 * @author ruoyi
 */
public interface INurseService
{
    public List<Nurse> selectNurseList(Nurse nurse);

    public Nurse selectNurseById(Long nurseId);

    public int insertNurse(Nurse nurse);

    public int updateNurse(Nurse nurse);

    public int deleteNurseByIds(Long[] nurseIds);

    public void assign(Long bedId, List<Long> nurseIds);

    public void batchAssign(Integer floorNo, List<Long> nurseIds);

    public List<Nurse> selectNursesByBedId(Long bedId);
}
