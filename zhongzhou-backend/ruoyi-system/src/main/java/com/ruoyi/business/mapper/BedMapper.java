package com.ruoyi.business.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.Bed;

/**
 * 床位 数据层
 *
 * @author ruoyi
 */
public interface BedMapper
{
    public List<Bed> selectBedList(Bed bed);

    public Bed selectBedById(Long bedId);

    public int insertBed(Bed bed);

    public int updateBed(Bed bed);

    public int deleteBedByIds(Long[] bedIds);

    public int deleteBedByRoomId(Long roomId);

    public List<Bed> selectBedListByRoomIds(@Param("roomIds") List<Long> roomIds);

    public List<Bed> selectAvailableBeds();
}
