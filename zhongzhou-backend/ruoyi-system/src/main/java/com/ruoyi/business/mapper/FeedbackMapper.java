package com.ruoyi.business.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.Feedback;

/**
 * 家属投诉建议 数据层
 *
 * @author ruoyi
 */
public interface FeedbackMapper
{
    public List<Feedback> selectFeedbackList(Feedback feedback);

    public Feedback selectFeedbackById(Long feedbackId);

    public int insertFeedback(Feedback feedback);

    public int updateFeedback(Feedback feedback);

    public int deleteFeedbackByIds(Long[] feedbackIds);

    public int replyFeedback(@Param("feedbackId") Long feedbackId, @Param("reply") String reply,
            @Param("replyBy") String replyBy);
}
