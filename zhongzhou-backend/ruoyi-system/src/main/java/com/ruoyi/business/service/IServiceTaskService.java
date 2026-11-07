package com.ruoyi.business.service;

import java.util.Date;
import java.util.List;
import com.ruoyi.business.domain.ServiceTask;
import com.ruoyi.business.domain.vo.FloorVO;

/**
 * 护理任务 服务层
 *
 * @author ruoyi
 */
public interface IServiceTaskService
{
    public List<FloorVO> selectOverview(Integer floorNo);

    /** 查询护理任务列表 */
    public List<ServiceTask> selectServiceTaskList(ServiceTask serviceTask);

    /** 更新任务状态（完成时可填写执行反馈） */
    public int updateTaskStatus(Long taskId, String taskStatus, String feedback);

    /** 新增护理任务（订单创建时自动派发） */
    public int insertServiceTask(ServiceTask serviceTask);

    /** 查询指定服务日期内已过计划时间且仍待执行的任务 */
    public List<ServiceTask> selectOverdueTasks(Date serviceDate, Date now);

    /** 标记任务为逾期 */
    public int markTaskOverdue(Long taskId);
}
