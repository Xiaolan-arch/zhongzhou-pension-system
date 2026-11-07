package com.ruoyi.business.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.Bed;
import com.ruoyi.business.domain.Elder;
import com.ruoyi.business.domain.Room;
import com.ruoyi.business.domain.vo.FloorVO;
import com.ruoyi.business.domain.vo.RoomDetailVO;
import com.ruoyi.business.domain.vo.RoomVO;
import com.ruoyi.business.mapper.BedMapper;
import com.ruoyi.business.mapper.ElderMapper;
import com.ruoyi.business.mapper.RoomMapper;
import com.ruoyi.business.service.IResidenceService;
import com.ruoyi.common.exception.ServiceException;

/**
 * 在住管理 服务层处理
 *
 * @author ruoyi
 */
@Service
public class ResidenceServiceImpl implements IResidenceService
{
    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private BedMapper bedMapper;

    @Autowired
    private ElderMapper elderMapper;

    /**
     * 楼层/房间/床位聚合视图，按楼层、房间号排序
     */
    @Override
    public List<FloorVO> selectOverview(Integer floorNo)
    {
        Room query = new Room();
        query.setFloorNo(floorNo);
        List<Room> rooms = roomMapper.selectRoomList(query);
        if (rooms.isEmpty())
        {
            return new ArrayList<FloorVO>();
        }

        List<Long> roomIds = rooms.stream().map(Room::getRoomId).collect(Collectors.toList());
        List<Bed> beds = bedMapper.selectBedListByRoomIds(roomIds);
        fillElderInfo(beds);

        Map<Long, List<Bed>> bedsByRoom = beds.stream()
                .collect(Collectors.groupingBy(Bed::getRoomId, LinkedHashMap::new, Collectors.toList()));

        Map<Integer, List<RoomVO>> roomsByFloor = new LinkedHashMap<Integer, List<RoomVO>>();
        for (Room room : rooms)
        {
            RoomVO roomVO = new RoomVO();
            roomVO.setRoomId(room.getRoomId());
            roomVO.setRoomNo(room.getRoomNo());
            roomVO.setRoomType(room.getRoomType());
            roomVO.setBedCount(room.getBedCount());
            roomVO.setBeds(bedsByRoom.getOrDefault(room.getRoomId(), new ArrayList<Bed>()));
            roomsByFloor.computeIfAbsent(room.getFloorNo(), key -> new ArrayList<RoomVO>()).add(roomVO);
        }

        List<FloorVO> floors = new ArrayList<FloorVO>();
        for (Map.Entry<Integer, List<RoomVO>> entry : roomsByFloor.entrySet())
        {
            floors.add(new FloorVO(entry.getKey(), entry.getValue()));
        }
        return floors;
    }

    /**
     * 房间详情：房间信息 + 床位老人列表 + 入住率
     */
    @Override
    public RoomDetailVO selectRoomDetail(Long roomId)
    {
        Room room = roomMapper.selectRoomById(roomId);
        if (room == null)
        {
            throw new ServiceException("房间不存在");
        }
        List<Bed> beds = bedMapper.selectBedListByRoomIds(Collections.singletonList(roomId));
        fillElderInfo(beds);

        int occupied = 0;
        for (Bed bed : beds)
        {
            if ("1".equals(bed.getStatus()))
            {
                occupied++;
            }
        }
        int bedCount = room.getBedCount() == null ? 0 : room.getBedCount();
        double rate = bedCount > 0 ? (occupied * 100.0) / bedCount : 0.0;
        rate = Math.round(rate * 100) / 100.0;

        RoomDetailVO detail = new RoomDetailVO();
        detail.setRoomId(room.getRoomId());
        detail.setFloorNo(room.getFloorNo());
        detail.setRoomNo(room.getRoomNo());
        detail.setRoomType(room.getRoomType());
        detail.setBedCount(room.getBedCount());
        detail.setStatus(room.getStatus());
        detail.setOccupiedBeds(occupied);
        detail.setOccupancyRate(rate);
        detail.setBeds(beds);
        return detail;
    }

    @Override
    public List<Bed> selectAvailableBeds()
    {
        return bedMapper.selectAvailableBeds();
    }

    /**
     * 填充床位上老人的展示字段
     */
    private void fillElderInfo(List<Bed> beds)
    {
        if (beds == null || beds.isEmpty())
        {
            return;
        }
        List<Elder> elders = elderMapper.selectElderList(new Elder());
        Map<Long, Elder> elderMap = new LinkedHashMap<Long, Elder>();
        for (Elder elder : elders)
        {
            elderMap.put(elder.getElderId(), elder);
        }
        for (Bed bed : beds)
        {
            if (bed.getElderId() == null)
            {
                continue;
            }
            Elder elder = elderMap.get(bed.getElderId());
            if (elder == null)
            {
                continue;
            }
            bed.setElderName(elder.getElderName());
            bed.setGender(elder.getGender());
            bed.setAge(elder.getAge());
            bed.setCareLevel(elder.getCareLevel());
            bed.setCheckinDate(elder.getCheckinDate());
        }
    }
}
