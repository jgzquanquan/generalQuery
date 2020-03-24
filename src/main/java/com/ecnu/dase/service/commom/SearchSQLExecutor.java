package com.ecnu.dase.service.commom;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;

/**
 * 具体根据<code>ISearchProcessor</code>处理得到的<code>SearchContext</code>，组装SQL语句，并且执行的类
 * @author jgz
 */
public class SearchSQLExecutor {

    final SearchProcessorBuilder builder;
    final SearchContext staticContext;
    final JdbcTemplate jt;

    SearchSQLExecutor(SearchProcessorBuilder builder) {
        this.builder = builder;
        staticContext = builder.processor.getStaticSearchContext();
        this.jt = builder.jt;
    }

    public List execute(Map<String, Object> searchConditions) {
        SearchContext context = new SearchContext();
        context.setMapper(staticContext.getMapper());
        builder.processor.process(searchConditions, context);
        return jt.query(toSQL(context), context.getMapper());
    }

    private String toSQL(SearchContext runtimeContext) {
        StringBuilder builder = new StringBuilder();

        // 构造select
        builder.append("select ");
        if (!staticContext.getAggColumns().isEmpty()
                || !runtimeContext.getAggColumns().isEmpty()) {
            builder.append(StringUtils
                    .join(CollectionUtils.union(staticContext.getAggColumns(),
                            runtimeContext.getAggColumns()), ","));

            //用以统计groupBy情况下总条目数处理
            builder.append(" from ( select ").append(staticContext.getPrimaryTable()).append(".*");

        } else {
            builder.append(
                    StringUtils.join(staticContext.selectedColumns, ","));
            if (!runtimeContext.getSelectedColumns().isEmpty()) {
                builder.append(",").append(
                        StringUtils.join(runtimeContext.selectedColumns, ","));
            }
        }

        // 构造from
        builder.append(" from ").append(staticContext.getPrimaryTable())
                .append(" ");
        builder.append(StringUtils.join(staticContext.getJoinTables(), " "));
        builder.append(" ");
        if (!runtimeContext.getJoinTables().isEmpty()) {
            builder.append(
                    StringUtils.join(runtimeContext.getJoinTables(), " "));
            builder.append(" ");
        }

        // 构造where
        builder.append(whereSQL(runtimeContext));

        if (!staticContext.getAggColumns().isEmpty()
                || !runtimeContext.getAggColumns().isEmpty()) {
            builder.append(" ) a");
        }

        return builder.toString();
    }

    /**
     * 根据被处理器处理过的条件，凭借成where语句后面的条件
     * @param runtimeContext
     * @return
     */
    private String whereSQL(SearchContext runtimeContext) {
        StringBuffer builder = new StringBuffer();
        builder.append("where ");
        boolean whereAdded = false;
        if (!staticContext.getWhere().isEmpty()) {
            if (whereAdded) {
                builder.append(" and ");
            }
            whereAdded = true;
            builder.append(StringUtils.join(staticContext.getWhere(), " and "));
        }

        if (!runtimeContext.getWhere().isEmpty()) {
            if (whereAdded) {
                builder.append(" and ");
            }
            whereAdded = true;
            builder.append(
                    StringUtils.join(runtimeContext.getWhere(), " and "));
        }

        if (!whereAdded) {
            builder.append(" 1=1 ");
        }

        Set<String> groupBySet = new HashSet<String>(
                staticContext.getGroupBy());
        List<String> groupByList = new ArrayList<>(staticContext.getGroupBy());
        for (String groupByCol : runtimeContext.getGroupBy()) {
            if (!groupBySet.contains(groupByCol)) {
                groupByList.add(groupByCol);
            }
        }
        if (!groupByList.isEmpty()) {
            builder.append(String.format(" group by %s",
                    StringUtils.join(groupByList, ",")));
        }

        Set<String> orderBySet = new HashSet<String>(
                staticContext.getOrderBy());
        List<String> orderByList = new ArrayList<>(staticContext.getOrderBy());
        for (String orderByCol : runtimeContext.getOrderBy()) {
            if (!orderBySet.contains(orderByCol)) {
                orderByList.add(orderByCol);
            }
        }
        if (!orderByList.isEmpty()) {
            builder.append(String.format(" order by %s",
                    StringUtils.join(orderByList, ",")));
        }

        if (!runtimeContext.getLimit().isEmpty()) {
            builder.append(runtimeContext.getLimit());
        }
        return builder.toString();
    }


}
