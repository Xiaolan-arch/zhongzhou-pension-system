package com.ruoyi.business.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.Leave;

/**
 * 请假外出 数据层
 *
 * @author ruoyi
 */
public interface LeaveMapper
{
    public List<Leave> selectLeaveList(Leave leave);

    public Leave selectLeaveById(Long leaveId);

    public int insertLeave(Leave leave);

    public int updateLeave(Leave leave);

    public int deleteLeaveByIds(Long[] leaveIds);

    public int auditLeave(@Param("leaveId") Long leaveId, @Param("status") String status,
            @Param("auditOpinion") String auditOpinion, @Param("auditBy") String auditBy);

    public int confirmBack(@Param("leaveId") Long leaveId, @Param("updateBy") String updateBy);

    public int updateBedStatusByElder(@Param("elderId") Long elderId, @Param("status") String status,
            @Param("tenantId") Long tenantId);
}
