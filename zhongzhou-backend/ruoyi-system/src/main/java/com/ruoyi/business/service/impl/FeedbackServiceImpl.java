package com.ruoyi.business.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.FamilyToken;
import com.ruoyi.business.domain.Feedback;
import com.ruoyi.business.domain.vo.FamilyElderVO;
import com.ruoyi.business.mapper.FamilyMapper;
import com.ruoyi.business.mapper.FeedbackMapper;
import com.ruoyi.business.service.IFeedbackService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;

/**
 * 家属投诉建议 服务层处理
 *
 * @author ruoyi
 */
@Service
public class FeedbackServiceImpl implements IFeedbackService
{
    @Autowired
    private FeedbackMapper feedbackMapper;

    @Autowired
    private FamilyMapper familyMapper;

    @Override
    public List<Feedback> selectFeedbackList(Feedback feedback)
    {
        return feedbackMapper.selectFeedbackList(feedback);
    }

    @Override
    public Feedback selectFeedbackById(Long feedbackId)
    {
        return feedbackMapper.selectFeedbackById(feedbackId);
    }

    @Override
    public int insertFeedback(Feedback feedback)
    {
        if (StringUtils.isNull(feedback.getTenantId()))
        {
            feedback.setTenantId(SecurityUtils.getTenantId());
        }
        if (StringUtils.isEmpty(feedback.getStatus()))
        {
            feedback.setStatus("0");
        }
        feedback.setCreateBy(SecurityUtils.getUsername());
        feedback.setCreateTime(DateUtils.getNowDate());
        return feedbackMapper.insertFeedback(feedback);
    }

    @Override
    public int updateFeedback(Feedback feedback)
    {
        feedback.setUpdateBy(SecurityUtils.getUsername());
        return feedbackMapper.updateFeedback(feedback);
    }

    @Override
    public int deleteFeedbackByIds(Long[] feedbackIds)
    {
        return feedbackMapper.deleteFeedbackByIds(feedbackIds);
    }

    @Override
    public int replyFeedback(Long feedbackId, String reply)
    {
        if (StringUtils.isEmpty(reply))
        {
            throw new ServiceException("请填写回复内容");
        }
        if (feedbackMapper.selectFeedbackById(feedbackId) == null)
        {
            throw new ServiceException("反馈记录不存在");
        }
        return feedbackMapper.replyFeedback(feedbackId, reply.trim(), SecurityUtils.getUsername());
    }

    // ====================== 家属端 ======================

    @Override
    public List<Feedback> selectFamilyFeedbacks(FamilyToken family)
    {
        Feedback query = new Feedback();
        query.setFamilyPhone(family.getPhone());
        query.setTenantId(family.getTenantId());
        return feedbackMapper.selectFeedbackList(query);
    }

    @Override
    public Feedback createFamilyFeedback(FamilyToken family, Feedback feedback)
    {
        if (feedback == null || StringUtils.isEmpty(feedback.getContent()))
        {
            throw new ServiceException("请填写反馈内容");
        }
        if (feedback.getContent().trim().length() > 1000)
        {
            throw new ServiceException("反馈内容不能超过 1000 字");
        }
        if (feedback.getElderId() != null)
        {
            FamilyElderVO elder = familyMapper.selectElderDetail(family.getPhone(), family.getTenantId(),
                    feedback.getElderId());
            if (elder == null)
            {
                throw new ServiceException("无权查看该老人信息");
            }
            feedback.setElderName(elder.getElderName());
        }
        if (StringUtils.isEmpty(feedback.getFeedbackType()))
        {
            feedback.setFeedbackType("1");
        }
        feedback.setTenantId(family.getTenantId());
        feedback.setFamilyPhone(family.getPhone());
        feedback.setFamilyName(family.getFamilyName());
        feedback.setStatus("0");
        feedback.setCreateBy(family.getPhone());
        feedbackMapper.insertFeedback(feedback);
        return feedback;
    }
}
