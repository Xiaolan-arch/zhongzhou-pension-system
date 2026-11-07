-- ============================================================
-- 清理历史遗留的「孤儿绑定」：绑定记录指向的老人档案不存在或已删除
-- 说明：家属端绑定列表查询已改为 INNER JOIN，界面不会再展示这类脏数据；
--       本脚本用于在数据库中彻底清理，可按需执行。
-- ============================================================

-- 1) 先查询：列出所有孤儿绑定，确认无误后再执行删除
SELECT f.family_id, f.family_name, f.phone, f.elder_id, f.status, f.tenant_id
FROM biz_family f
LEFT JOIN biz_elder e
       ON e.elder_id = f.elder_id
      AND e.tenant_id = f.tenant_id
      AND e.del_flag = '0'
WHERE e.elder_id IS NULL;

-- 2) 通用清理：删除所有老人档案已不存在或已删除的绑定
DELETE f
FROM biz_family f
LEFT JOIN biz_elder e
       ON e.elder_id = f.elder_id
      AND e.tenant_id = f.tenant_id
      AND e.del_flag = '0'
WHERE e.elder_id IS NULL;

-- 3) 定向清理（示例数据：family_id = 8 / elder_id = 356）
--    如只需处理这一条，可单独执行下面语句。
-- DELETE FROM biz_family
-- WHERE family_id = 8 AND elder_id = 356;
