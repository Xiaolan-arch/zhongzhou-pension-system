package com.ruoyi.business.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.BedNurse;

/**
 * 床位护理员关联 数据层
 *
 * @author ruoyi
 */
public interface BedNurseMapper
{
    public int insertBedNurse(BedNurse bedNurse);

    public int deleteBedNurseByBedId(@Param("bedId") Long bedId);

    public int deleteBedNurseByBedIds(@Param("bedIds") List<Long> bedIds);

    public List<BedNurse> selectBedNurseListByBedIds(@Param("bedIds") List<Long> bedIds);
}
