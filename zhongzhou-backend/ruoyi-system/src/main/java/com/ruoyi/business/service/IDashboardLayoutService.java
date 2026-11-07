package com.ruoyi.business.service;

/**
 * 工作台个性化布局 服务接口
 *
 * @author ruoyi
 */
public interface IDashboardLayoutService
{
    /**
     * 获取当前登录用户的布局配置，未配置返回 null
     */
    String getLayout();

    /**
     * 保存当前登录用户的布局配置
     *
     * @param layoutJson 布局JSON（区块顺序与显隐）
     */
    void saveLayout(String layoutJson);
}
