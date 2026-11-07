package com.ruoyi.business.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.Bed;
import com.ruoyi.business.domain.BedNurse;
import com.ruoyi.business.domain.Nurse;
import com.ruoyi.business.domain.ServiceTask;
import com.ruoyi.business.domain.vo.FloorVO;
import com.ruoyi.business.domain.vo.RoomVO;
import com.ruoyi.business.mapper.BedNurseMapper;
import com.ruoyi.business.mapper.ServiceTaskMapper;
import com.ruoyi.business.service.IResidenceService;
import com.ruoyi.business.service.IServiceTaskService;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;

/**
 * 护理任务 服务层处理
 *
 * @author ruoyi
 */
@Service
public class ServiceTaskServiceImpl implements IServiceTaskService
{
    @Autowired
    private IResidenceService residenceService;

    @Autowired
    private BedNurseMapper bedNurseMapper;

    @Autowired
    private ServiceTaskMapper serviceTaskMapper;

    /**
     * 新增护理任务：补充机构、创建人、默认待执行状态
     */
    @Override
    public int insertServiceTask(ServiceTask serviceTask)
    {
        if (StringUtils.isNull(serviceTask.getTenantId()))
        {
            serviceTask.setTenantId(SecurityUtils.getTenantId());
        }
        if (StringUtils.isEmpty(serviceTask.getCreateBy()))
        {
            serviceTask.setCreateBy(SecurityUtils.getUsername());
        }
        if (StringUtils.isEmpty(serviceTask.getTaskStatus()))
        {
            serviceTask.setTaskStatus("0");
        }
        return serviceTaskMapper.insertServiceTask(serviceTask);
    }

    /**
     * 查询护理任务列表
     */
    @Override
    public List<ServiceTask> selectServiceTaskList(ServiceTask serviceTask)
    {
        return serviceTaskMapper.selectServiceTaskList(serviceTask);
    }

    /**
     * 更新任务状态
     */
    @Override
    public int updateTaskStatus(Long taskId, String taskStatus, String feedback)
    {
        return serviceTaskMapper.updateTaskStatus(taskId, taskStatus, feedback, SecurityUtils.getUsername());
    }

    /**
     * 查询指定服务日期内、已过计划时间且仍待执行的任务
     */
    @Override
    public List<ServiceTask> selectOverdueTasks(Date serviceDate, Date now)
    {
        return serviceTaskMapper.selectOverdueTasks(serviceDate, now);
    }

    /**
     * 标记任务为逾期（待执行 → 已逾期）
     */
    @Override
    public int markTaskOverdue(Long taskId)
    {
        return serviceTaskMapper.updateTaskOverdue(taskId);
    }

    /**
     * 在住聚合视图，每个床位附加护理员列表
     */
    @Override
    public List<FloorVO> selectOverview(Integer floorNo)
    {
        List<FloorVO> floors = residenceService.selectOverview(floorNo);
        List<Long> bedIds = new ArrayList<Long>();
        for (FloorVO floor : floors)
        {
            for (RoomVO room : floor.getRooms())
            {
                for (Bed bed : room.getBeds())
                {
                    bedIds.add(bed.getBedId());
                }
            }
        }
        if (bedIds.isEmpty())
        {
            return floors;
        }

        List<BedNurse> bedNurses = bedNurseMapper.selectBedNurseListByBedIds(bedIds);
        Map<Long, List<Nurse>> nursesByBed = new LinkedHashMap<Long, List<Nurse>>();
        for (BedNurse bedNurse : bedNurses)
        {
            List<Nurse> nurses = nursesByBed.computeIfAbsent(bedNurse.getBedId(), key -> new ArrayList<Nurse>());
            Nurse nurse = new Nurse();
            nurse.setNurseId(bedNurse.getNurseId());
            nurse.setNurseName(bedNurse.getNurseName());
            nurses.add(nurse);
        }

        for (FloorVO floor : floors)
        {
            for (RoomVO room : floor.getRooms())
            {
                for (Bed bed : room.getBeds())
                {
                    bed.setNurses(nursesByBed.getOrDefault(bed.getBedId(), new ArrayList<Nurse>()));
                }
            }
        }
        return floors;
    }
}
