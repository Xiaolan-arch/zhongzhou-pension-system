package com.ruoyi.business.mapper;

import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.Checkin;

/**
 * 入住/退住涉及的床位与老人 数据层
 *
 * 独立于 BedMapper，避免与其他模块的 Mapper 冲突。
 *
 * @author ruoyi
 */
public interface CheckinBedMapper
{
    /**
     * 条件占用床位
     *
     * @param bedId 床位ID
     * @param elderId 老人ID
     * @param tenantId 机构ID
     * @return 影响行数
     */
    public int occupyBed(@Param("bedId") Long bedId, @Param("elderId") Long elderId, @Param("tenantId") Long tenantId);

    /**
     * 按老人释放床位
     *
     * @param elderId 老人ID
     * @param tenantId 机构ID
     * @return 影响行数
     */
    public int releaseBedByElder(@Param("elderId") Long elderId, @Param("tenantId") Long tenantId);

    /**
     * 根据身份证查询老人ID
     *
     * @param idCard 身份证号
     * @param tenantId 机构ID
     * @return 老人ID
     */
    public Long selectElderIdByIdCard(@Param("idCard") String idCard, @Param("tenantId") Long tenantId);

    /**
     * 根据姓名查询老人ID
     *
     * @param elderName 老人姓名
     * @param tenantId 机构ID
     * @return 老人ID
     */
    public Long selectElderIdByName(@Param("elderName") String elderName, @Param("tenantId") Long tenantId);

    /**
     * 按入住申请快照新增老人（待入住）
     *
     * @param checkin 入住申请
     * @return 结果
     */
    public int insertElderByCheckin(Checkin checkin);

    /**
     * 按入住申请更新老人入住信息
     *
     * @param elderId 老人ID
     * @param checkin 入住申请
     * @return 结果
     */
    public int updateElderByCheckin(@Param("elderId") Long elderId, @Param("checkin") Checkin checkin);

    /**
     * 更新老人状态
     *
     * @param elderId 老人ID
     * @param status 状态
     * @param tenantId 机构ID
     * @return 结果
     */
    public int updateElderStatus(@Param("elderId") Long elderId, @Param("status") String status,
            @Param("tenantId") Long tenantId);
}
