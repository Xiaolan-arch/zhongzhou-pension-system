package com.ruoyi.business.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.HealthRecord;

/**
 * 老人体征记录 Mapper 接口
 */
public interface HealthRecordMapper
{
    /** 查询列表 */
    public List<HealthRecord> selectHealthRecordList(HealthRecord healthRecord);

    /** 按ID查 */
    public HealthRecord selectHealthRecordById(Long recordId);

    /** 按老人查最近 N 条（AI 报告用） */
    public List<HealthRecord> selectRecentByElderId(@Param("elderId") Long elderId,
                                                    @Param("limit") int limit);

    /** 插入 */
    public int insertHealthRecord(HealthRecord healthRecord);

    /** 更新 */
    public int updateHealthRecord(HealthRecord healthRecord);

    /** 删除 */
    public int deleteHealthRecordById(Long recordId);

    /** 批量删除 */
    public int deleteHealthRecordByIds(Long[] recordIds);

    /** 异常体征数量（Dashboard 用） */
    public Long countAbnormalHealth();
}
