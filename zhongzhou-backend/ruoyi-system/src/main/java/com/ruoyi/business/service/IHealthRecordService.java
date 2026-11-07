package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.HealthRecord;

/**
 * 老人体征记录 Service 接口
 */
public interface IHealthRecordService
{
    public List<HealthRecord> selectHealthRecordList(HealthRecord healthRecord);

    public HealthRecord selectHealthRecordById(Long recordId);

    /** 查询老人最近 limit 条体征（AI 报告用） */
    public List<HealthRecord> selectRecentByElderId(Long elderId, int limit);

    public int insertHealthRecord(HealthRecord healthRecord);

    public int updateHealthRecord(HealthRecord healthRecord);

    public int deleteHealthRecordByIds(Long[] recordIds);

    public int deleteHealthRecordById(Long recordId);

    /** Dashboard：今日异常体征老人数 */
    public Long countAbnormalHealth();
}
