package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.Checkin;

/**
 * 入住申请 服务层
 *
 * @author ruoyi
 */
public interface ICheckinService
{
    /**
     * 查询入住申请列表
     *
     * @param checkin 入住申请
     * @return 入住申请集合
     */
    public List<Checkin> selectCheckinList(Checkin checkin);

    /**
     * 查询入住申请详情
     *
     * @param checkinId 入住申请ID
     * @return 入住申请
     */
    public Checkin selectCheckinById(Long checkinId);

    /**
     * 创建入住申请草稿
     *
     * @param checkin 入住申请
     * @return 结果
     */
    public int insertCheckin(Checkin checkin);

    /**
     * 保存当前步骤表单字段（不改变流程状态）
     *
     * @param checkin 入住申请
     * @return 结果
     */
    public int updateCheckin(Checkin checkin);

    /**
     * 提交向导步骤
     *
     * @param checkinId 入住申请ID
     * @param step 步骤（1-6）
     * @param checkin 表单数据
     * @return 结果
     */
    public int submitStep(Long checkinId, Integer step, Checkin checkin);

    /**
     * 入住审核
     *
     * @param checkin 审核信息
     * @return 结果
     */
    public int audit(Checkin checkin);

    /**
     * 完成入住
     *
     * @param checkinId 入住申请ID
     * @return 结果
     */
    public int complete(Long checkinId);

    /**
     * 驳回后重新提交（状态 9 → 2 待审核，清空旧审核字段）
     */
    public int resubmit(Long checkinId);

    /**
     * 批量删除入住申请
     *
     * @param checkinIds 入住申请ID数组
     * @return 结果
     */
    public int deleteCheckinByIds(Long[] checkinIds);
}
