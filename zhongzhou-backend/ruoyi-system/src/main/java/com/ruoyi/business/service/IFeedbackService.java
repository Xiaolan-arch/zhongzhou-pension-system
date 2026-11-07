package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.FamilyToken;
import com.ruoyi.business.domain.Feedback;

/**
 * 家属投诉建议 服务层
 *
 * @author ruoyi
 */
public interface IFeedbackService
{
    public List<Feedback> selectFeedbackList(Feedback feedback);

    public Feedback selectFeedbackById(Long feedbackId);

    public int insertFeedback(Feedback feedback);

    public int updateFeedback(Feedback feedback);

    public int deleteFeedbackByIds(Long[] feedbackIds);

    /**
     * 机构回复家属反馈
     */
    public int replyFeedback(Long feedbackId, String reply);

    // ====================== 家属端 ======================

    public List<Feedback> selectFamilyFeedbacks(FamilyToken family);

    public Feedback createFamilyFeedback(FamilyToken family, Feedback feedback);
}
