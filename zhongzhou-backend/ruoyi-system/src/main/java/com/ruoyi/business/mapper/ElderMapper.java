package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.Elder;

/**
 * 老人档案 数据层
 *
 * @author ruoyi
 */
public interface ElderMapper
{
    /**
     * 查询老人列表
     *
     * @param elder 查询条件
     * @return 老人列表
     */
    public List<Elder> selectElderList(Elder elder);

    /**
     * 通过老人ID查询老人
     *
     * @param elderId 老人ID
     * @return 老人信息
     */
    public Elder selectElderById(Long elderId);

    /**
     * 新增老人档案
     *
     * @param elder 老人信息
     * @return 结果
     */
    public int insertElder(Elder elder);
}
