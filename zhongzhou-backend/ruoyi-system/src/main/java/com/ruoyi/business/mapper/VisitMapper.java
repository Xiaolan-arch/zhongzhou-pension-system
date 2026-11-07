package com.ruoyi.business.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.Visit;

/**
 * 来访预约 数据层
 *
 * @author ruoyi
 */
public interface VisitMapper
{
    public List<Visit> selectVisitList(Visit visit);

    public Visit selectVisitById(Long visitId);

    public int insertVisit(Visit visit);

    public int updateVisit(Visit visit);

    public int deleteVisitByIds(Long[] visitIds);

    public int confirmVisitById(@Param("visitId") Long visitId, @Param("updateBy") String updateBy);

    public int cancelVisitById(@Param("visitId") Long visitId, @Param("updateBy") String updateBy);

    public int checkinVisitById(@Param("visitId") Long visitId, @Param("updateBy") String updateBy);
}
