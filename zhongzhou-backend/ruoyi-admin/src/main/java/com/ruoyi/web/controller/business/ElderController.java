package com.ruoyi.web.controller.business;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.domain.Elder;
import com.ruoyi.business.service.IElderService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 老人档案
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/business/elder")
public class ElderController extends BaseController
{
    @Autowired
    private IElderService elderService;

    /**
     * 查询老人列表
     */
    @GetMapping("/list")
    public TableDataInfo list(Elder elder)
    {
        startPage();
        List<Elder> list = elderService.selectElderList(elder);
        return getDataTable(list);
    }

    /**
     * 获取老人详情
     */
    @GetMapping("/{elderId}")
    public AjaxResult getInfo(@PathVariable Long elderId)
    {
        return success(elderService.selectElderById(elderId));
    }
}
