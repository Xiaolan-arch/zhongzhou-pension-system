package com.ruoyi.business.domain;

import java.io.Serializable;

/**
 * 家属端登录凭证（存放于 Redis）
 *
 * @author ruoyi
 */
public class FamilyToken implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 家属ID */
    private Long familyId;

    /** 登录手机号 */
    private String phone;

    /** 家属姓名 */
    private String familyName;

    /** 与老人关系 */
    private String relation;

    /** 机构ID */
    private Long tenantId;

    /** 微信 openid（登录时绑定，用于订阅消息推送） */
    private String openid;

    public Long getFamilyId()
    {
        return familyId;
    }

    public void setFamilyId(Long familyId)
    {
        this.familyId = familyId;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public String getFamilyName()
    {
        return familyName;
    }

    public void setFamilyName(String familyName)
    {
        this.familyName = familyName;
    }

    public String getRelation()
    {
        return relation;
    }

    public void setRelation(String relation)
    {
        this.relation = relation;
    }

    public Long getTenantId()
    {
        return tenantId;
    }

    public void setTenantId(Long tenantId)
    {
        this.tenantId = tenantId;
    }

    public String getOpenid()
    {
        return openid;
    }

    public void setOpenid(String openid)
    {
        this.openid = openid;
    }
}
