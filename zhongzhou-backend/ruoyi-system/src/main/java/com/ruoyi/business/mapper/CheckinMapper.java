package com.ruoyi.business.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.Checkin;

/**
 * 入住申请 数据层
 *
 * @author ruoyi
 */
public interface CheckinMapper
{
    /**
     * 查询入住申请列表
     *
     * @param checkin 入住申请
     * @return 入住申请集合
     */
    public List<Checkin> selectCheckinList(Checkin checkin);

    /**
     * 通过入住申请ID查询入住申请
     *
     * @param checkinId 入住申请ID
     * @return 入住申请
     */
    public Checkin selectCheckinById(Long checkinId);

    /**
     * 新增入住申请
     *
     * @param checkin 入住申请
     * @return 结果
     */
    public int insertCheckin(Checkin checkin);

    /**
     * 保存当前步骤表单字段
     *
     * @param checkin 入住申请
     * @return 结果
     */
    public int updateCheckin(Checkin checkin);

    /**
     * 条件更新流程状态
     *
     * @param checkinId 入住申请ID
     * @param oldStatus 期望当前状态
     * @param newStatus 目标状态
     * @param updateBy 更新者
     * @return 结果
     */
    public int updateCheckinStatus(@Param("checkinId") Long checkinId, @Param("oldStatus") String oldStatus,
            @Param("newStatus") String newStatus, @Param("updateBy") String updateBy);

    /**
     * 入住审核（仅待审核状态可操作）
     *
     * @param checkin 审核信息
     * @param newStatus 审核后状态
     * @return 结果
     */
    public int updateCheckinAudit(@Param("checkin") Checkin checkin, @Param("newStatus") String newStatus);

    /**
     * 首期缴费状态更新
     *
     * @param checkin 入住申请
     * @return 结果
     */
    public int updateCheckinFirstPay(Checkin checkin);

    /**
     * 完成入住（状态 5 -> 6）
     *
     * @param checkin 入住申请
     * @return 结果
     */
    public int updateCheckinComplete(Checkin checkin);

    /**
     * 驳回后重新提交（状态 9 -> 2 待审核，清空旧审核字段）
     */
    public int updateCheckinResubmit(@Param("checkinId") Long checkinId, @Param("updateBy") String updateBy);

    /**
     * 批量逻辑删除入住申请
     *
     * @param checkinIds 入住申请ID数组
     * @return 结果
     */
    public int deleteCheckinByIds(Long[] checkinIds);
}
