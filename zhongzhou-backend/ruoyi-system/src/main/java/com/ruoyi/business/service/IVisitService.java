package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.Visit;

/**
 * 来访预约 服务层
 *
 * @author ruoyi
 */
public interface IVisitService
{
    public List<Visit> selectVisitList(Visit visit);

    public Visit selectVisitById(Long visitId);

    public int insertVisit(Visit visit);

    public int updateVisit(Visit visit);

    public int deleteVisitByIds(Long[] visitIds);

    public int confirmVisit(Long visitId);

    public int cancelVisit(Long visitId);

    /**
     * 到访核销（待确认/已确认 → 已完成）
     */
    public int checkinVisit(Long visitId);
}
