package com.ruoyi.web.controller.business;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.domain.ServiceTask;
import com.ruoyi.business.service.IServiceTaskService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;

/**
 * 护理任务 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/business/serviceTask")
public class ServiceTaskController extends BaseController
{
    @Autowired
    private IServiceTaskService serviceTaskService;

    /** 查询护理任务列表 */
    @PreAuthorize("@ss.hasPermi('business:serviceTask:list')")
    @GetMapping("/list")
    public TableDataInfo list(ServiceTask serviceTask)
    {
        startPage();
        List<ServiceTask> list = serviceTaskService.selectServiceTaskList(serviceTask);
        return getDataTable(list);
    }

    /** 更新任务状态（开始执行 / 完成 / 取消） */
    @PreAuthorize("@ss.hasPermi('business:serviceTask:edit')")
    @Log(title = "护理任务", businessType = BusinessType.UPDATE)
    @PutMapping("/status/{taskId}")
    public AjaxResult updateStatus(@PathVariable Long taskId, @RequestParam String taskStatus,
                                   @RequestParam(required = false) String feedback)
    {
        return toAjax(serviceTaskService.updateTaskStatus(taskId, taskStatus, feedback));
    }
}
