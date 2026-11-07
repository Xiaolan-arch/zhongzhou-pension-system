package com.ruoyi.framework.tenant;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.SecurityUtils;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.update.Update;

/**
 * 多租户行级隔离拦截器
 *
 * 对配置了 tenant_id 的业务表，自动在 SELECT/UPDATE/DELETE 语句中追加 tenant_id 条件。
 * 超级管理员（userId = 1）与未登录上下文不做过滤；INSERT 的租户归属由业务层写入。
 */
@Intercepts({ @Signature(type = StatementHandler.class, method = "prepare", args = { Connection.class, Integer.class }) })
public class TenantSqlInterceptor implements Interceptor
{
    /** 需要按租户隔离的表 */
    private static final Set<String> TENANT_TABLES = new HashSet<String>(Arrays.asList(
            "sys_user", "sys_dept", "sys_role", "sys_post",
            "biz_elder", "biz_room", "biz_bed", "biz_nurse", "biz_bed_nurse",
            "biz_checkin", "biz_checkout", "biz_visit",
            "biz_service_item", "biz_service_order", "biz_service_task", "biz_bill", "biz_bill_item",
            "biz_payment_record", "biz_deposit", "biz_audit_log", "biz_service_evaluation",
            "biz_health_report", "biz_health_record", "biz_user_dashboard_layout", "biz_message_log"));

    @Override
    public Object intercept(Invocation invocation) throws Throwable
    {
        StatementHandler handler = (StatementHandler) invocation.getTarget();
        BoundSql boundSql = handler.getBoundSql();
        if (boundSql == null || boundSql.getSql() == null)
        {
            return invocation.proceed();
        }

        Long tenantId = resolveTenantId();
        String sql = boundSql.getSql();
        if (tenantId == null || tenantId <= 0 || !isTenantSql(sql))
        {
            return invocation.proceed();
        }

        try
        {
            Statement statement = CCJSqlParserUtil.parse(sql);
            boolean changed = false;
            if (statement instanceof Select)
            {
                // 递归处理所有嵌套的 PlainSelect（FROM 子查询、WHERE 子查询、SELECT 列表子查询）
                List<PlainSelect> allBodies = new ArrayList<PlainSelect>();
                collectAllPlainSelects(((Select) statement).getSelectBody(), allBodies);
                for (PlainSelect body : allBodies)
                {
                    changed = appendTenantCondition(body, tenantId) || changed;
                }
            }
            else if (statement instanceof Update)
            {
                Update update = (Update) statement;
                Table table = update.getTable();
                if (isTenantTable(table) && notFiltered(update.getWhere(), table))
                {
                    Expression condition = new EqualsTo(new Column(table, "tenant_id"), new LongValue(tenantId));
                    update.setWhere(update.getWhere() == null ? condition
                            : new AndExpression(update.getWhere(), condition));
                    changed = true;
                }
                // UPDATE 的 WHERE 中若有子查询也追加
                List<PlainSelect> subs = new ArrayList<PlainSelect>();
                collectSubSelects(update.getWhere(), subs);
                for (PlainSelect body : subs)
                {
                    changed = appendTenantCondition(body, tenantId) || changed;
                }
            }
            else if (statement instanceof Delete)
            {
                Delete delete = (Delete) statement;
                Table table = delete.getTable();
                if (isTenantTable(table) && notFiltered(delete.getWhere(), table))
                {
                    Expression condition = new EqualsTo(new Column(table, "tenant_id"), new LongValue(tenantId));
                    delete.setWhere(delete.getWhere() == null ? condition
                            : new AndExpression(delete.getWhere(), condition));
                    changed = true;
                }
                List<PlainSelect> subs = new ArrayList<PlainSelect>();
                collectSubSelects(delete.getWhere(), subs);
                for (PlainSelect body : subs)
                {
                    changed = appendTenantCondition(body, tenantId) || changed;
                }
            }

            if (changed)
            {
                MetaObject metaObject = SystemMetaObject.forObject(boundSql);
                metaObject.setValue("sql", statement.toString());
            }
        }
        catch (Exception e)
        {
            // SQL 解析失败时不改写，避免影响正常业务
            return invocation.proceed();
        }
        return invocation.proceed();
    }

    /** 递归收集所有 PlainSelect（包含 SetOperationList 里的 UNION 分支） */
    private void collectAllPlainSelects(SelectBody body, List<PlainSelect> result)
    {
        if (body == null) return;
        if (body instanceof PlainSelect)
        {
            PlainSelect ps = (PlainSelect) body;
            result.add(ps);
            // 继续收集 FROM 子查询
            collectFromItemSubSelects(ps.getFromItem(), result);
            if (ps.getJoins() != null)
            {
                for (Join join : ps.getJoins())
                {
                    collectFromItemSubSelects(join.getRightItem(), result);
                }
            }
            // 继续收集 WHERE 子查询
            collectSubSelects(ps.getWhere(), result);
        }
        else if (body instanceof net.sf.jsqlparser.statement.select.SetOperationList)
        {
            List<SelectBody> bodies = ((net.sf.jsqlparser.statement.select.SetOperationList) body).getSelects();
            for (SelectBody item : bodies)
            {
                collectAllPlainSelects(item, result);
            }
        }
    }

    /** 从 FromItem 中递归收集子查询 PlainSelect（JSqlParser 4.5 中 SubSelect 实现 FromItem） */
    private void collectFromItemSubSelects(FromItem fromItem, List<PlainSelect> result)
    {
        if (fromItem == null) return;
        if (fromItem instanceof SubSelect)
        {
            collectAllPlainSelects(((SubSelect) fromItem).getSelectBody(), result);
        }
    }

    /** 从任意 Expression 中递归收集 SubSelect（WHERE / HAVING / JOIN ON 条件里的子查询） */
    private void collectSubSelects(Expression expr, List<PlainSelect> result)
    {
        if (expr == null) return;
        if (expr instanceof SubSelect)
        {
            collectAllPlainSelects(((SubSelect) expr).getSelectBody(), result);
            return;
        }
        // 递归遍历表达式树：And / Or / Parenthesis / ComparisonOperator
        if (expr instanceof net.sf.jsqlparser.expression.Parenthesis)
        {
            collectSubSelects(((net.sf.jsqlparser.expression.Parenthesis) expr).getExpression(), result);
        }
        else if (expr instanceof AndExpression)
        {
            collectSubSelects(((AndExpression) expr).getLeftExpression(), result);
            collectSubSelects(((AndExpression) expr).getRightExpression(), result);
        }
        else if (expr instanceof net.sf.jsqlparser.expression.operators.conditional.OrExpression)
        {
            collectSubSelects(((net.sf.jsqlparser.expression.operators.conditional.OrExpression) expr).getLeftExpression(), result);
            collectSubSelects(((net.sf.jsqlparser.expression.operators.conditional.OrExpression) expr).getRightExpression(), result);
        }
        else if (expr instanceof net.sf.jsqlparser.expression.operators.conditional.XorExpression)
        {
            collectSubSelects(((net.sf.jsqlparser.expression.operators.conditional.XorExpression) expr).getLeftExpression(), result);
            collectSubSelects(((net.sf.jsqlparser.expression.operators.conditional.XorExpression) expr).getRightExpression(), result);
        }
        else if (expr instanceof net.sf.jsqlparser.expression.operators.relational.ComparisonOperator)
        {
            collectSubSelects(((net.sf.jsqlparser.expression.operators.relational.ComparisonOperator) expr).getLeftExpression(), result);
            collectSubSelects(((net.sf.jsqlparser.expression.operators.relational.ComparisonOperator) expr).getRightExpression(), result);
        }
    }

    /**
     * 仅处理查询/更新/删除语句
     */
    private boolean isTenantSql(String sql)
    {
        String trimmed = sql.trim().toLowerCase();
        return trimmed.startsWith("select") || trimmed.startsWith("update") || trimmed.startsWith("delete");
    }

    private boolean isTenantTable(Table table)
    {
        return table != null && table.getName() != null
                && TENANT_TABLES.contains(table.getName().toLowerCase());
    }

    private boolean appendTenantCondition(PlainSelect plainSelect, Long tenantId)
    {
        List<Table> tables = new ArrayList<Table>();
        collectTable(plainSelect.getFromItem(), tables);
        if (plainSelect.getJoins() != null)
        {
            for (Join join : plainSelect.getJoins())
            {
                collectTable(join.getRightItem(), tables);
            }
        }

        boolean changed = false;
        for (Table table : tables)
        {
            if (!isTenantTable(table) || !notFiltered(plainSelect.getWhere(), table))
            {
                continue;
            }
            Expression condition = new EqualsTo(new Column(table, "tenant_id"), new LongValue(tenantId));
            plainSelect.setWhere(plainSelect.getWhere() == null ? condition
                    : new AndExpression(plainSelect.getWhere(), condition));
            changed = true;
        }
        return changed;
    }

    private void collectTable(FromItem fromItem, List<Table> tables)
    {
        if (fromItem instanceof Table)
        {
            tables.add((Table) fromItem);
        }
    }

    /**
     * 判断当前 where 条件中是否已经存在该表的 tenant_id 过滤，避免重复追加
     */
    private boolean notFiltered(Expression where, Table table)
    {
        if (where == null)
        {
            return true;
        }
        String qualifier = table.getAlias() != null && table.getAlias().getName() != null
                ? table.getAlias().getName() : table.getName();
        return !where.toString().toLowerCase().contains(qualifier.toLowerCase() + ".tenant_id");
    }

    /**
     * 获取当前请求所属机构ID（超级管理员返回 null 表示不过滤）
     */
    private Long resolveTenantId()
    {
        try
        {
            LoginUser loginUser = SecurityUtils.getLoginUser();
            if (loginUser == null || SecurityUtils.isAdmin(loginUser.getUserId()))
            {
                return null;
            }
            return loginUser.getUser() == null ? null : loginUser.getUser().getTenantId();
        }
        catch (Exception e)
        {
            return null;
        }
    }

    @Override
    public Object plugin(Object target)
    {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(java.util.Properties properties)
    {
    }
}
