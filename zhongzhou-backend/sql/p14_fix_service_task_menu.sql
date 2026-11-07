-- 修复「服务管理 → 护理任务」菜单打不开：原指向 business/service/task（依赖不存在的 /business/service/task/overview 接口）
-- 改为指向已实现的护理任务管理页面 business/serviceTask/index，并统一权限标识
UPDATE sys_menu
SET component = 'business/serviceTask/index',
    perms = 'business:serviceTask:list',
    remark = '护理任务列表与执行反馈（修复原空白页）'
WHERE menu_id = 4302;

-- 护理员分配按钮保留但仍挂在 4302 下，不影响新页面
