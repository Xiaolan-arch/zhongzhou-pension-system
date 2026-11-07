package com.ruoyi.business.mapper;

import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.ServiceTask;

/**
 * 护理任务 数据层
 *
 * @author ruoyi
 */
public interface ServiceTaskMapper
{
    /** 新增护理任务 */
    public int insertServiceTask(ServiceTask serviceTask);

    /** 查询护理任务列表（支持按老人、护理员、状态、服务日期筛选） */
    public List<ServiceTask> selectServiceTaskList(ServiceTask serviceTask);

    /** 更新任务状态（0待执行 1已执行 2已完成 3已取消 4已逾期），可同时写入执行反馈 */
    public int updateTaskStatus(@Param("taskId") Long taskId, @Param("taskStatus") String taskStatus,
                                @Param("feedback") String feedback, @Param("updateBy") String updateBy);

    /** 查询指定服务日期内、已过计划时间且仍待执行的任务 */
    public List<ServiceTask> selectOverdueTasks(@Param("serviceDate") Date serviceDate,
                                                @Param("now") Date now);

    /** 将任务标记为逾期（0待执行 → 4已逾期） */
    public int updateTaskOverdue(@Param("taskId") Long taskId);
}
