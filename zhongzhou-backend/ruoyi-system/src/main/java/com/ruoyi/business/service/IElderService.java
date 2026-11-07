package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.Elder;

/**
 * 老人档案 服务层
 *
 * @author ruoyi
 */
public interface IElderService
{
    public List<Elder> selectElderList(Elder elder);

    public Elder selectElderById(Long elderId);
}
