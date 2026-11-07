package com.ruoyi.business.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 入住申请业务对象 biz_checkin
 *
 * @author ruoyi
 */
public class Checkin extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 入住申请ID */
    private Long checkinId;

    /** 申请单号 */
    private String applyNo;

    /** 老人姓名 */
    private String elderName;

    /** 性别（0男 1女） */
    private String gender;

    /** 身份证号 */
    private String idCard;

    /** 出生日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date birthday;

    /** 年龄 */
    private Integer age;

    /** 联系电话 */
    private String phone;

    /** 户籍地址 */
    private String address;

    /** 一寸照片 */
    private String avatar;

    /** 身份证人像面 */
    private String idCardFront;

    /** 身份证国徽面 */
    private String idCardBack;

    /** 家属姓名 */
    private String familyName;

    /** 与老人关系 */
    private String familyRelation;

    /** 家属电话 */
    private String familyPhone;

    /** 状态（0申请中 1待评估 2待审核 3待配置 4待签约 5待缴费 6已入住 9已驳回） */
    private String applyStatus;

    /** 自理能力评估 */
    private String selfCare;

    /** 精神状态评估 */
    private String mentalState;

    /** 疾病情况 */
    private String disease;

    /** 评估结论 */
    private String assessmentResult;

    /** 建议护理等级 */
    private String suggestCareLevel;

    /** 评估人 */
    private String assessor;

    /** 评估时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date assessTime;

    /** 审核结果（0通过 1驳回） */
    private String auditResult;

    /** 审核意见 */
    private String auditOpinion;

    /** 审核人 */
    private String auditor;

    /** 审核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date auditTime;

    /** 房间ID */
    private Long roomId;

    /** 床位ID */
    private Long bedId;

    /** 护理等级 */
    private String careLevel;

    /** 护理员ID串 */
    private String nurseIds;

    /** 费用标准（元/月） */
    private BigDecimal feeStandard;

    /** 合同编号 */
    private String contractNo;

    /** 合同开始日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date contractStart;

    /** 合同结束日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date contractEnd;

    /** 合同文件 */
    private String contractFile;

    /** 签约日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date signDate;

    /** 首期缴费状态（0待支付 1已支付） */
    private String payStatus;

    /** 首期账单ID */
    private Long firstBillId;

    /** 入住日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date checkinDate;

    /** 机构ID */
    private Long tenantId;

    public Long getCheckinId()
    {
        return checkinId;
    }

    public void setCheckinId(Long checkinId)
    {
        this.checkinId = checkinId;
    }

    public String getApplyNo()
    {
        return applyNo;
    }

    public void setApplyNo(String applyNo)
    {
        this.applyNo = applyNo;
    }

    public String getElderName()
    {
        return elderName;
    }

    public void setElderName(String elderName)
    {
        this.elderName = elderName;
    }

    public String getGender()
    {
        return gender;
    }

    public void setGender(String gender)
    {
        this.gender = gender;
    }

    public String getIdCard()
    {
        return idCard;
    }

    public void setIdCard(String idCard)
    {
        this.idCard = idCard;
    }

    public Date getBirthday()
    {
        return birthday;
    }

    public void setBirthday(Date birthday)
    {
        this.birthday = birthday;
    }

    public Integer getAge()
    {
        return age;
    }

    public void setAge(Integer age)
    {
        this.age = age;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public String getAddress()
    {
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    public String getAvatar()
    {
        return avatar;
    }

    public void setAvatar(String avatar)
    {
        this.avatar = avatar;
    }

    public String getIdCardFront()
    {
        return idCardFront;
    }

    public void setIdCardFront(String idCardFront)
    {
        this.idCardFront = idCardFront;
    }

    public String getIdCardBack()
    {
        return idCardBack;
    }

    public void setIdCardBack(String idCardBack)
    {
        this.idCardBack = idCardBack;
    }

    public String getFamilyName()
    {
        return familyName;
    }

    public void setFamilyName(String familyName)
    {
        this.familyName = familyName;
    }

    public String getFamilyRelation()
    {
        return familyRelation;
    }

    public void setFamilyRelation(String familyRelation)
    {
        this.familyRelation = familyRelation;
    }

    public String getFamilyPhone()
    {
        return familyPhone;
    }

    public void setFamilyPhone(String familyPhone)
    {
        this.familyPhone = familyPhone;
    }

    public String getApplyStatus()
    {
        return applyStatus;
    }

    public void setApplyStatus(String applyStatus)
    {
        this.applyStatus = applyStatus;
    }

    public String getSelfCare()
    {
        return selfCare;
    }

    public void setSelfCare(String selfCare)
    {
        this.selfCare = selfCare;
    }

    public String getMentalState()
    {
        return mentalState;
    }

    public void setMentalState(String mentalState)
    {
        this.mentalState = mentalState;
    }

    public String getDisease()
    {
        return disease;
    }

    public void setDisease(String disease)
    {
        this.disease = disease;
    }

    public String getAssessmentResult()
    {
        return assessmentResult;
    }

    public void setAssessmentResult(String assessmentResult)
    {
        this.assessmentResult = assessmentResult;
    }

    public String getSuggestCareLevel()
    {
        return suggestCareLevel;
    }

    public void setSuggestCareLevel(String suggestCareLevel)
    {
        this.suggestCareLevel = suggestCareLevel;
    }

    public String getAssessor()
    {
        return assessor;
    }

    public void setAssessor(String assessor)
    {
        this.assessor = assessor;
    }

    public Date getAssessTime()
    {
        return assessTime;
    }

    public void setAssessTime(Date assessTime)
    {
        this.assessTime = assessTime;
    }

    public String getAuditResult()
    {
        return auditResult;
    }

    public void setAuditResult(String auditResult)
    {
        this.auditResult = auditResult;
    }

    public String getAuditOpinion()
    {
        return auditOpinion;
    }

    public void setAuditOpinion(String auditOpinion)
    {
        this.auditOpinion = auditOpinion;
    }

    public String getAuditor()
    {
        return auditor;
    }

    public void setAuditor(String auditor)
    {
        this.auditor = auditor;
    }

    public Date getAuditTime()
    {
        return auditTime;
    }

    public void setAuditTime(Date auditTime)
    {
        this.auditTime = auditTime;
    }

    public Long getRoomId()
    {
        return roomId;
    }

    public void setRoomId(Long roomId)
    {
        this.roomId = roomId;
    }

    public Long getBedId()
    {
        return bedId;
    }

    public void setBedId(Long bedId)
    {
        this.bedId = bedId;
    }

    public String getCareLevel()
    {
        return careLevel;
    }

    public void setCareLevel(String careLevel)
    {
        this.careLevel = careLevel;
    }

    public String getNurseIds()
    {
        return nurseIds;
    }

    public void setNurseIds(String nurseIds)
    {
        this.nurseIds = nurseIds;
    }

    public BigDecimal getFeeStandard()
    {
        return feeStandard;
    }

    public void setFeeStandard(BigDecimal feeStandard)
    {
        this.feeStandard = feeStandard;
    }

    public String getContractNo()
    {
        return contractNo;
    }

    public void setContractNo(String contractNo)
    {
        this.contractNo = contractNo;
    }

    public Date getContractStart()
    {
        return contractStart;
    }

    public void setContractStart(Date contractStart)
    {
        this.contractStart = contractStart;
    }

    public Date getContractEnd()
    {
        return contractEnd;
    }

    public void setContractEnd(Date contractEnd)
    {
        this.contractEnd = contractEnd;
    }

    public String getContractFile()
    {
        return contractFile;
    }

    public void setContractFile(String contractFile)
    {
        this.contractFile = contractFile;
    }

    public Date getSignDate()
    {
        return signDate;
    }

    public void setSignDate(Date signDate)
    {
        this.signDate = signDate;
    }

    public String getPayStatus()
    {
        return payStatus;
    }

    public void setPayStatus(String payStatus)
    {
        this.payStatus = payStatus;
    }

    public Long getFirstBillId()
    {
        return firstBillId;
    }

    public void setFirstBillId(Long firstBillId)
    {
        this.firstBillId = firstBillId;
    }

    public Date getCheckinDate()
    {
        return checkinDate;
    }

    public void setCheckinDate(Date checkinDate)
    {
        this.checkinDate = checkinDate;
    }

    public Long getTenantId()
    {
        return tenantId;
    }

    public void setTenantId(Long tenantId)
    {
        this.tenantId = tenantId;
    }
}
