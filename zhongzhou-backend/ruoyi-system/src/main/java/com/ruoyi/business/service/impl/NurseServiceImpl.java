package com.ruoyi.business.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.domain.Bed;
import com.ruoyi.business.domain.BedNurse;
import com.ruoyi.business.domain.Nurse;
import com.ruoyi.business.domain.Room;
import com.ruoyi.business.mapper.BedMapper;
import com.ruoyi.business.mapper.BedNurseMapper;
import com.ruoyi.business.mapper.NurseMapper;
import com.ruoyi.business.mapper.RoomMapper;
import com.ruoyi.business.service.INurseService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;

/**
 * 护理员 服务层处理
 *
 * @author ruoyi
 */
@Service
public class NurseServiceImpl implements INurseService
{
    @Autowired
    private NurseMapper nurseMapper;

    @Autowired
    private BedMapper bedMapper;

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private BedNurseMapper bedNurseMapper;

    @Override
    public List<Nurse> selectNurseList(Nurse nurse)
    {
        return nurseMapper.selectNurseList(nurse);
    }

    @Override
    public Nurse selectNurseById(Long nurseId)
    {
        return nurseMapper.selectNurseById(nurseId);
    }

    @Override
    public int insertNurse(Nurse nurse)
    {
        if (StringUtils.isNull(nurse.getTenantId()))
        {
            nurse.setTenantId(SecurityUtils.getTenantId());
        }
        if (StringUtils.isEmpty(nurse.getCreateBy()))
        {
            nurse.setCreateBy(SecurityUtils.getUsername());
        }
        nurse.setCreateTime(DateUtils.getNowDate());
        return nurseMapper.insertNurse(nurse);
    }

    @Override
    public int updateNurse(Nurse nurse)
    {
        nurse.setUpdateBy(SecurityUtils.getUsername());
        nurse.setUpdateTime(DateUtils.getNowDate());
        return nurseMapper.updateNurse(nurse);
    }

    @Override
    public int deleteNurseByIds(Long[] nurseIds)
    {
        return nurseMapper.deleteNurseByIds(nurseIds);
    }

    /**
     * 单床护理员分配：先删除该床关联再插入
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assign(Long bedId, List<Long> nurseIds)
    {
        if (bedId == null)
        {
            throw new ServiceException("床位ID不能为空");
        }
        Bed bed = bedMapper.selectBedById(bedId);
        if (bed == null)
        {
            throw new ServiceException("床位不存在");
        }
        bedNurseMapper.deleteBedNurseByBedId(bedId);

        Long tenantId = SecurityUtils.getTenantId();
        if (tenantId == null)
        {
            tenantId = bed.getTenantId();
        }
        if (nurseIds == null)
        {
            return;
        }
        Set<Long> uniqueNurseIds = new LinkedHashSet<Long>(nurseIds);
        for (Long nurseId : uniqueNurseIds)
        {
            if (nurseId == null)
            {
                continue;
            }
            BedNurse bedNurse = new BedNurse();
            bedNurse.setBedId(bedId);
            bedNurse.setNurseId(nurseId);
            bedNurse.setTenantId(tenantId);
            bedNurseMapper.insertBedNurse(bedNurse);
        }
    }

    /**
     * 整层批量替换护理员：对该楼层所有已入住床位先删后插
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchAssign(Integer floorNo, List<Long> nurseIds)
    {
        if (floorNo == null)
        {
            throw new ServiceException("楼层不能为空");
        }
        Room query = new Room();
        query.setFloorNo(floorNo);
        List<Room> rooms = roomMapper.selectRoomList(query);
        if (rooms.isEmpty())
        {
            throw new ServiceException("该楼层没有房间");
        }
        List<Long> roomIds = new ArrayList<Long>();
        for (Room room : rooms)
        {
            roomIds.add(room.getRoomId());
        }
        List<Bed> beds = bedMapper.selectBedListByRoomIds(roomIds);
        List<Long> occupiedBedIds = new ArrayList<Long>();
        for (Bed bed : beds)
        {
            if ("1".equals(bed.getStatus()))
            {
                occupiedBedIds.add(bed.getBedId());
            }
        }
        if (occupiedBedIds.isEmpty())
        {
            throw new ServiceException("该楼层没有已入住床位");
        }

        bedNurseMapper.deleteBedNurseByBedIds(occupiedBedIds);
        Long tenantId = SecurityUtils.getTenantId();
        if (nurseIds == null)
        {
            return;
        }
        Set<Long> uniqueNurseIds = new LinkedHashSet<Long>(nurseIds);
        for (Long bedId : occupiedBedIds)
        {
            for (Long nurseId : uniqueNurseIds)
            {
                if (nurseId == null)
                {
                    continue;
                }
                BedNurse bedNurse = new BedNurse();
                bedNurse.setBedId(bedId);
                bedNurse.setNurseId(nurseId);
                bedNurse.setTenantId(tenantId);
                bedNurseMapper.insertBedNurse(bedNurse);
            }
        }
    }

    @Override
    public List<Nurse> selectNursesByBedId(Long bedId)
    {
        List<Nurse> nurses = new ArrayList<Nurse>();
        if (bedId == null)
        {
            return nurses;
        }
        List<BedNurse> bedNurses = bedNurseMapper.selectBedNurseListByBedIds(Collections.singletonList(bedId));
        for (BedNurse bedNurse : bedNurses)
        {
            Nurse nurse = new Nurse();
            nurse.setNurseId(bedNurse.getNurseId());
            nurse.setNurseName(bedNurse.getNurseName());
            nurses.add(nurse);
        }
        return nurses;
    }
}
