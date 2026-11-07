package com.ruoyi.web.task;

import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.ruoyi.business.domain.ServiceTask;
import com.ruoyi.business.service.IServiceTaskService;
import com.ruoyi.common.utils.DateUtils;

/**
 * 护理任务到期提醒任务
 *
 * 每 10 分钟扫描「服务日期=今天、状态=待执行、计划时间已过」的任务，
 * 标记为已逾期并在日志中输出逾期清单（后续可接入微信推送）。
 *
 * @author ruoyi
 */
@Component
public class ServiceTaskOverdueTask
{
    private static final Logger log = LoggerFactory.getLogger(ServiceTaskOverdueTask.class);

    @Autowired
    private IServiceTaskService serviceTaskService;

    @Scheduled(cron = "0 */10 * * * ?")
    public void scanOverdueTasks()
    {
        Date now = DateUtils.getNowDate();
        // 今天 00:00:00，用于按服务日期过滤
        Date today = DateUtils.parseDate(DateUtils.getDate());
        List<ServiceTask> tasks = serviceTaskService.selectOverdueTasks(today, now);
        if (tasks.isEmpty())
        {
            return;
        }

        int marked = 0;
        for (ServiceTask task : tasks)
        {
            marked += serviceTaskService.markTaskOverdue(task.getTaskId());
            log.warn("护理任务逾期：taskId={}，orderNo={}，老人={}，项目={}，护理员={}，计划时间={}",
                    task.getTaskId(), task.getOrderNo(), task.getElderName(), task.getItemName(),
                    task.getNurseName(), DateUtils.parseDateToStr("yyyy-MM-dd HH:mm:ss", task.getPlanTime()));
        }
        log.warn("护理任务逾期扫描完成，共 {} 条，已标记 {} 条", tasks.size(), marked);
    }
}
