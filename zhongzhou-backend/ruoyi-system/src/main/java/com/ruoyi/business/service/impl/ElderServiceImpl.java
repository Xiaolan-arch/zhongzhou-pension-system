package com.ruoyi.business.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.Elder;
import com.ruoyi.business.mapper.ElderMapper;
import com.ruoyi.business.service.IElderService;

/**
 * 老人档案 服务层处理
 *
 * @author ruoyi
 */
@Service
public class ElderServiceImpl implements IElderService
{
    @Autowired
    private ElderMapper elderMapper;

    @Override
    public List<Elder> selectElderList(Elder elder)
    {
        return elderMapper.selectElderList(elder);
    }

    @Override
    public Elder selectElderById(Long elderId)
    {
        return elderMapper.selectElderById(elderId);
    }
}
