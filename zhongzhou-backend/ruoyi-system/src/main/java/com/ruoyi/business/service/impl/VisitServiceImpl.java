package com.ruoyi.business.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.Visit;
import com.ruoyi.business.mapper.VisitMapper;
import com.ruoyi.business.service.IVisitService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;

/**
 * 来访预约 服务层处理
 *
 * @author ruoyi
 */
@Service
public class VisitServiceImpl implements IVisitService
{
    @Autowired
    private VisitMapper visitMapper;

    @Override
    public List<Visit> selectVisitList(Visit visit)
    {
        return visitMapper.selectVisitList(visit);
    }

    @Override
    public Visit selectVisitById(Long visitId)
    {
        return visitMapper.selectVisitById(visitId);
    }

    @Override
    public int insertVisit(Visit visit)
    {
        if (StringUtils.isNull(visit.getTenantId()))
        {
            visit.setTenantId(SecurityUtils.getTenantId());
        }
        if (StringUtils.isEmpty(visit.getCreateBy()))
        {
            visit.setCreateBy(SecurityUtils.getUsername());
        }
        if (StringUtils.isEmpty(visit.getVisitCode()))
        {
            visit.setVisitCode(generateVisitCode());
        }
        visit.setCreateTime(DateUtils.getNowDate());
        return visitMapper.insertVisit(visit);
    }

    @Override
    public int updateVisit(Visit visit)
    {
        visit.setUpdateBy(SecurityUtils.getUsername());
        visit.setUpdateTime(DateUtils.getNowDate());
        return visitMapper.updateVisit(visit);
    }

    @Override
    public int deleteVisitByIds(Long[] visitIds)
    {
        return visitMapper.deleteVisitByIds(visitIds);
    }

    @Override
    public int confirmVisit(Long visitId)
    {
        int rows = visitMapper.confirmVisitById(visitId, SecurityUtils.getUsername());
        if (rows == 0)
        {
            throw new ServiceException("仅待确认状态的来访可以确认，请刷新后重试");
        }
        return rows;
    }

    @Override
    public int cancelVisit(Long visitId)
    {
        int rows = visitMapper.cancelVisitById(visitId, SecurityUtils.getUsername());
        if (rows == 0)
        {
            throw new ServiceException("该来访已取消，请刷新后重试");
        }
        return rows;
    }

    @Override
    public int checkinVisit(Long visitId)
    {
        int rows = visitMapper.checkinVisitById(visitId, SecurityUtils.getUsername());
        if (rows == 0)
        {
            throw new ServiceException("该来访已核销或已取消，请刷新后重试");
        }
        return rows;
    }

    /** 探访预约码：V + 日期 + 4 位序号 */
    private String generateVisitCode()
    {
        return "V" + new java.text.SimpleDateFormat("yyMMdd").format(DateUtils.getNowDate())
                + String.format("%04d", (int) (Math.random() * 10000));
    }
}
