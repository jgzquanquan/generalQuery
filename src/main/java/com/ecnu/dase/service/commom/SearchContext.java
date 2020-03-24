package com.ecnu.dase.service.commom;

import org.apache.commons.lang.StringUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 存储搜索的文境
 * @author jgz
 */
public class SearchContext {
    CommonRowMapper mapper = null;
    Set<String> selectedColumns = new HashSet<>();
    Set<String> aggColumns = new HashSet<>();

    //根据实际需求设置主表
    String primaryTable = "";
    List<String> joinTables = new ArrayList<>();
    Set<String> joinedTable = new HashSet<String>();

    List<String> where = new ArrayList<>();
    List<String> orderBy = new ArrayList<>();
    List<String> groupBy = new ArrayList<>();
    String limit = "";

    /**
     * 增加where后的判断条件
     */
    public void setupLimit(int begin, int end) {
        limit = String.format(" limit %d, %d", begin, end);
    }

    public void addWhere(String expr) {
        where.add(expr);
    }

    public void addWhere(ExpressionBuilder builder) {
        where.add(builder.toString());
    }

    public void dateEqual(String table, String column, String value) {
        where.add(String.format("unix_timestamp(%s.%s)=unix_timestamp(%s)", table, column, value));
    }

    public void isNull(String table, String column) {
        where.add(String.format("(%s.%s is null or %1$s.%2$s = false)", table, column));
    }

    public void isNotNull(String table, String column) {
        where.add(String.format("(%s.%s is not null)", table, column));
    }

    public void numericEqual(String table, String column, String value) {
        where.add(String.format("%s.%s=%s", table, column, value));
    }

    public void numericEqual(String table, String column, String table2, String column2) {
        where.add(String.format("%s.%s=%s.%s", table, column, table2, column2));
    }
    public void numericNoEqual(String table, String column, String value) {
        where.add(String.format("%s.%s!=%s", table, column, value));
    }

    public void numericMoreThan(String table, String column, String table2, String column2) {
        where.add(String.format("%s.%s>%s.%s", table, column, table2, column2));
    }

    public void numericOr(String table, String column, List<String> values) {
        where.add(String.format(" ( %s ) ", StringUtils.join(
                values.stream().map(value -> String.format("%s.%s=%s", table, column, value)).iterator(), " or ")));
    }

    public void numericNotAnd(String table, String column, List<String> values) {
        where.add(String.format(" ( %s ) ", StringUtils.join(
                values.stream().map(value -> String.format("%s.%s!=%s", table, column, value)).iterator(), " and ")));
    }

    /**
     * 数值型小于
     *
     * @param column
     * @param value
     */
    public void numericLT(String column, String value) {
        where.add(String.format("%s<%s", column, value));
    }

    public void booleanEqual(String table, String column, String value) {
        if (value.equals("1"))
            value = "true";
        else if (value.equals("0"))
            value = "false";
        where.add(String.format("%s.%s=%s", table, column, value));
    }

    public void stringEqual(String table, String column, String value) {
        where.add(String.format("%s.%s='%s'", table, column, value));
    }

    public void stringNoEqual(String table, String column, String value) {
        where.add(String.format("%s.%s!='%s'", table, column, value));
    }

    public void stringOr(String table, String column, List<String> strings) {
        where.add(String.format(" ( %s ) ", StringUtils.join(
                strings.stream().map(value -> String.format("%s.%s='%s'", table, column, value)).iterator(), " or ")));

    }

    /**
     * 拼成table.column like %value%
     */
    public void stringLike(String table, String colmun, String value) {
        where.add(String.format("%s.%s like ", table, colmun) + "'%" + value + "%'");
    }

    /**
     * 拼成table.column like value%
     */
    public void stringRightLike(String table, String colmun, String value) {
        where.add(String.format("%s.%s like ", table, colmun) + "'%" + value + "%'");
    }

    /**
     * 拼成table.column like %value
     */
    public void stringLeftLike(String table, String colmun, String value) {
        where.add(String.format("%s.%s like ", table, colmun) + "'%" + value + "%'");
    }

    public void longIn(String table, String column, Set<Long> value) {
        where.add(String.format("%s.%s in (%s)", table, column,
                StringUtils.join(value, ",")));
    }

    public void longNotIn(String table, String column, Set<Long> value) {
        where.add(String.format("%s.%s not in (%s)", table, column,
                StringUtils.join(value, ",")));
    }

    public void stringIn(String table, String column, Set<String> value) {
        where.add(String.format("%s.%s in (%s)", table, column, StringUtils.join(value, ",")));
    }

    /**
     * 添加in子查询
     *
     * @param table
     * @param column
     * @param subSelect
     */
    public void subSelectNotIn(String table, String column, String subSelect) {
        where.add(String.format("%s.%s not in (%s)", table, column, subSelect));
    }

    public void subSelectIn(String table, String column, String subSelect) {
        where.add(String.format("%s.%s in (%s)", table, column, subSelect));
    }

    public void dateGE(String table, String column, String start) {
        where.add(String.format("unix_timestamp(date(%s.%s))>=unix_timestamp('%s')", table, column, start));
    }

    public void dateLE(String table, String column, String end) {
        where.add(String.format("unix_timestamp(date(%s.%s))<=unix_timestamp('%s')", table, column, end));
    }

    public void dateBetween(String table, String column, String dateStart, String dateEnd) {
        where.add(String.format("%s.%s between '%s' and '%s'", table, column, dateStart, dateEnd));
    }

    public CommonRowMapper getMapper() {
        return mapper;
    }

    public void setMapper(CommonRowMapper mapper) {
        this.mapper = mapper;
    }

    public void selectCount(List<String> columns) {
        selectedColumns.clear();
        aggColumns.add(String.format("count(%s)", StringUtils.join(columns, ",")));
    }

    public void setPrimaryTable(String primaryTable) {
        this.primaryTable = primaryTable;
    }

    public void addColumn(String table, String column) {
        selectedColumns.add(String.format("%s.%s", table, column));
    }

    public void addColumn(String column) {
        selectedColumns.add(column);
    }

    public void addColumnAs(String table, String column, String alias) {
        selectedColumns.add(String.format("%s.%s as %s", table, column, alias));
    }

    /**
     * right join
     *
     * @param table
     * @param column
     * @param joinTable
     * @param joinCol
     */
    public void rightJoin(String table, String column, String joinTable, String joinCol) {
        if (!joinedTable.contains(table)) {
            joinedTable.add(table);
            joinTables.add(String.format("right join %s on %1$s.%s = %s.%s", table, column, joinTable, joinCol));
        } else {
            // 防止join相同的表，join相同的表会报错
            for (String jt : joinTables) {
                if (jt.contains(table)) {
                    jt += String.format(" and %s.%s = %s.%s", table, column, joinTable, joinCol);
                }
            }
        }
    }

    /**
     * 带有别名的right join
     *
     * @param table
     * @param tableAlias
     * @param column
     * @param joinTable
     * @param joinCol
     */
    public void rightJoin(String table, String tableAlias, String column, String joinTable, String joinCol) {
        if (!joinedTable.contains(tableAlias)) {
            joinedTable.add(tableAlias);
            joinTables.add(String.format("right join %s as %s on %2$s.%s = %s.%s", table, tableAlias, column, joinTable,
                    joinCol));
        } else {
            for (String jt : joinTables) {
                if (jt.contains(tableAlias)) {
                    jt += String.format(" and %s.%s = %s.%s", table, column, joinTable, joinCol);
                }
            }
        }
    }

    /**
     * @param newTable
     *            新加入的表
     * @param column
     * @param mainTable
     *            已有的表
     * @param joinCol
     */
    public void leftJoin(String newTable, String column, String mainTable, String joinCol) {
        if (!joinedTable.contains(newTable)) {
            joinedTable.add(newTable);
            joinTables.add(String.format("left join %s on %1$s.%s = %s.%s", newTable, column, mainTable, joinCol));
        } else {
            // 防止join相同的表，join相同的表会报错
            for (String jt : joinTables) {
                if (jt.contains(newTable)) {
                    jt += String.format(" and %s.%s = %s.%s", newTable, column, mainTable, joinCol);
                }
            }
        }
    }

    /**
     * @param table
     *            新加入的表
     * @param column
     * @param joinTable
     *            已有的表
     * @param joinCol
     */
    public void leftJoin(String table, String tableAlias, String column, String joinTable, String joinCol) {
        if (!joinedTable.contains(tableAlias)) {
            joinedTable.add(tableAlias);
            joinTables.add(String.format("left join %s as %s on %2$s.%s = %s.%s", table, tableAlias, column, joinTable,
                    joinCol));
        } else {
            for (String jt : joinTables) {
                if (jt.contains(tableAlias)) {
                    jt += String.format(" and %s.%s = %s.%s", table, column, joinTable, joinCol);
                }
            }
        }
    }

    /**
     * @param table
     *            新加入的表
     * @param column
     * @param joinTable
     *            已有的表
     * @param joinCol
     */
    public void join(String table, String column, String joinTable, String joinCol) {
        if (!joinedTable.contains(table)) {
            joinedTable.add(table);
            joinTables.add(String.format("join %s on %1$s.%s = %s.%s", table, column, joinTable, joinCol));
        } else {
            for (String jt : joinTables) {
                if (jt.contains(table)) {
                    joinTables.remove(jt);
                    String njt = jt + String.format(" and %s.%s = %s.%s", table, column, joinTable, joinCol);
                    joinTables.add(njt);
                    return;
                }
            }
        }
    }

    /**
     * @param table
     *            新加入的表
     * @param column
     * @param joinTable
     *            已有的表
     * @param joinCol
     */
    public void join(String table, String tableAlias, String column, String joinTable, String joinCol) {
        if (!joinedTable.contains(table)) {
            joinedTable.add(tableAlias);
            joinTables.add(
                    String.format("join %s as %s on %2$s.%s = %s.%s", table, tableAlias, column, joinTable, joinCol));
        }
    }

    /**
     * 例如columns="t_xj.xh, t_xj.grade", order="desc"
     *
     * @param column 列名，使用全称，即 tableName.columnName
     * @param order asc 或 desc
     */
    public void orderby(String column, String order) {
        this.orderBy.add(String.format("%s %s", column, order));
    }

    public List<String> getGroupBy() {
        return groupBy;
    }

    public void groupby(String column){
        this.groupBy.add(String.format("%s",column));
    }

    public Set<String> getSelectedColumns() {
        return selectedColumns;
    }

    public String getPrimaryTable() {
        return primaryTable;
    }

    public List<String> getJoinTables() {
        return joinTables;
    }

    public List<String> getWhere() {
        return where;
    }

    public List<String> getOrderBy() {
        return orderBy;
    }

    public String getLimit() {
        return limit;
    }

    public Set<String> getAggColumns() {
        return aggColumns;
    }

    public void setAggColumns(Set<String> aggColumns) {
        this.aggColumns = aggColumns;
    }

    public static String column(String table, String column) {
        return String.format("%s.%s", table, column);
    }

    public ExpressionBuilder buildExpress() {
        return new ExpressionBuilder();
    }

    public static class ExpressionBuilder {
        StringBuffer buf = new StringBuffer();

        public ExpressionBuilder column(String column) {
            buf.append(column);
            return this;
        }

        public ExpressionBuilder column(String table, String column) {
            buf.append(String.format("%s.%s", table, column));
            return this;
        }

        public ExpressionBuilder quotedValue(String value) {
            buf.append(String.format("'%s'", value));
            return this;
        }

        public ExpressionBuilder value(String value) {
            buf.append(value);
            return this;
        }

        public ExpressionBuilder greaterThan() {
            buf.append(">");
            return this;
        }

        public ExpressionBuilder greaterOrEqual() {
            buf.append(">=");
            return this;
        }

        public ExpressionBuilder lessThan() {
            buf.append("<");
            return this;
        }

        public ExpressionBuilder lessOrEqual() {
            buf.append("<=");
            return this;
        }

        public ExpressionBuilder equal() {
            buf.append("=");
            return this;
        }

        public ExpressionBuilder notEqual() {
            buf.append("<>");
            return this;
        }

        @Override
        public String toString() {
            return String.format("(%s)", buf.toString());
        }
    }

}
