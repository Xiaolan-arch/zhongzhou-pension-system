package com.ruoyi.business.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.domain.FamilyToken;
import com.ruoyi.business.domain.Leave;
import com.ruoyi.business.domain.vo.FamilyElderVO;
import com.ruoyi.business.mapper.FamilyMapper;
import com.ruoyi.business.mapper.LeaveMapper;
import com.ruoyi.business.service.ILeaveService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;

/**
 * 请假外出 服务层处理
 *
 * @author ruoyi
 */
@Service
public class LeaveServiceImpl implements ILeaveService
{
    /** 床位状态：外出 */
    private static final String BED_STATUS_OUT = "2";

    /** 床位状态：在住 */
    private static final String BED_STATUS_IN = "1";

    @Autowired
    private LeaveMapper leaveMapper;

    @Autowired
    private FamilyMapper familyMapper;

    @Override
    public List<Leave> selectLeaveList(Leave leave)
    {
        return leaveMapper.selectLeaveList(leave);
    }

    @Override
    public Leave selectLeaveById(Long leaveId)
    {
        return leaveMapper.selectLeaveById(leaveId);
    }

    @Override
    public int insertLeave(Leave leave)
    {
        if (StringUtils.isNull(leave.getTenantId()))
        {
            leave.setTenantId(SecurityUtils.getTenantId());
        }
        if (StringUtils.isEmpty(leave.getCreateBy()))
        {
            leave.setCreateBy(SecurityUtils.getUsername());
        }
        if (StringUtils.isEmpty(leave.getStatus()))
        {
            leave.setStatus("0");
        }
        leave.setCreateTime(DateUtils.getNowDate());
        return leaveMapper.insertLeave(leave);
    }

    @Override
    public int updateLeave(Leave leave)
    {
        leave.setUpdateBy(SecurityUtils.getUsername());
        return leaveMapper.updateLeave(leave);
    }

    @Override
    public int deleteLeaveByIds(Long[] leaveIds)
    {
        return leaveMapper.deleteLeaveByIds(leaveIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int auditLeave(Long leaveId, String status, String auditOpinion)
    {
        Leave leave = leaveMapper.selectLeaveById(leaveId);
        if (leave == null)
        {
            throw new ServiceException("请假记录不存在");
        }
        if (!"1".equals(status) && !"2".equals(status))
        {
            throw new ServiceException("审批结果不正确");
        }
        int rows = leaveMapper.auditLeave(leaveId, status, auditOpinion, SecurityUtils.getUsername());
        if (rows == 0)
        {
            throw new ServiceException("该申请已处理，请刷新后重试");
        }
        // 审批通过后床位标记为外出，驳回不影响床位
        if ("1".equals(status))
        {
            leaveMapper.updateBedStatusByElder(leave.getElderId(), BED_STATUS_OUT, leave.getTenantId());
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int confirmBack(Long leaveId)
    {
        Leave leave = leaveMapper.selectLeaveById(leaveId);
        if (leave == null)
        {
            throw new ServiceException("请假记录不存在");
        }
        int rows = leaveMapper.confirmBack(leaveId, SecurityUtils.getUsername());
        if (rows == 0)
        {
            throw new ServiceException("仅已通过的请假可以销假，请刷新后重试");
        }
        leaveMapper.updateBedStatusByElder(leave.getElderId(), BED_STATUS_IN, leave.getTenantId());
        return rows;
    }

    // ====================== 家属端 ======================

    @Override
    public List<Leave> selectFamilyLeaves(FamilyToken family, Long elderId)
    {
        checkElderOwnership(family, elderId);
        Leave query = new Leave();
        query.setElderId(elderId);
        query.setTenantId(family.getTenantId());
        return leaveMapper.selectLeaveList(query);
    }

    @Override
    public Leave createFamilyLeave(FamilyToken family, Leave leave)
    {
        if (leave == null || leave.getElderId() == null)
        {
            throw new ServiceException("请选择老人");
        }
        FamilyElderVO elder = checkElderOwnership(family, leave.getElderId());
        if (leave.getStartTime() == null || leave.getEndTime() == null)
        {
            throw new ServiceException("请选择外出与预计返回时间");
        }
        if (!leave.getEndTime().after(leave.getStartTime()))
        {
            throw new ServiceException("预计返回时间必须晚于外出时间");
        }
        if (StringUtils.isEmpty(leave.getReason()))
        {
            throw new ServiceException("请填写请假事由");
        }
        if (StringUtils.isEmpty(leave.getLeaveType()))
        {
            leave.setLeaveType("0");
        }
        if (StringUtils.isEmpty(leave.getContactPhone()))
        {
            leave.setContactPhone(family.getPhone());
        }
        leave.setElderName(elder.getElderName());
        leave.setTenantId(family.getTenantId());
        leave.setStatus("0");
        leave.setCreateBy(family.getPhone());
        leaveMapper.insertLeave(leave);
        return leave;
    }

    @Override
    public int cancelFamilyLeave(FamilyToken family, Long leaveId)
    {
        Leave leave = leaveMapper.selectLeaveById(leaveId);
        if (leave == null)
        {
            throw new ServiceException("请假记录不存在");
        }
        checkElderOwnership(family, leave.getElderId());
        if (!"0".equals(leave.getStatus()))
        {
            throw new ServiceException("仅待审批的申请可以撤销");
        }
        Leave update = new Leave();
        update.setLeaveId(leaveId);
        update.setStatus("4");
        update.setUpdateBy(family.getPhone());
        return leaveMapper.updateLeave(update);
    }

    private FamilyElderVO checkElderOwnership(FamilyToken family, Long elderId)
    {
        if (elderId == null)
        {
            throw new ServiceException("请选择老人");
        }
        FamilyElderVO elder = familyMapper.selectElderDetail(family.getPhone(), family.getTenantId(), elderId);
        if (elder == null)
        {
            throw new ServiceException("无权查看该老人信息");
        }
        return elder;
    }
}
