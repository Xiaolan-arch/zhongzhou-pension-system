package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.FamilyToken;
import com.ruoyi.business.domain.Leave;

/**
 * 请假外出 服务层
 *
 * @author ruoyi
 */
public interface ILeaveService
{
    public List<Leave> selectLeaveList(Leave leave);

    public Leave selectLeaveById(Long leaveId);

    public int insertLeave(Leave leave);

    public int updateLeave(Leave leave);

    public int deleteLeaveByIds(Long[] leaveIds);

    /**
     * 审批（status=1 通过并联动床位外出，status=2 驳回）
     */
    public int auditLeave(Long leaveId, String status, String auditOpinion);

    /**
     * 销假（恢复床位在住状态）
     */
    public int confirmBack(Long leaveId);

    // ====================== 家属端 ======================

    public List<Leave> selectFamilyLeaves(FamilyToken family, Long elderId);

    public Leave createFamilyLeave(FamilyToken family, Leave leave);

    public int cancelFamilyLeave(FamilyToken family, Long leaveId);
}
