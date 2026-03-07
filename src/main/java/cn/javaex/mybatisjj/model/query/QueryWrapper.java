package cn.javaex.mybatisjj.model.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * 查询封装
 * 
 * @author 陈霓清
 * @Date 2024年5月26日
 * @param <T>
 */
public class QueryWrapper<T> implements Wrapper<T> {
	private List<String> conditions = new ArrayList<>();
	private List<String> orderByColumns = new ArrayList<>();
	private List<String> groupByColumns = new ArrayList<>();
	private String havingClause = null;
	private String applySql = null;
	private String lastSql = null;

	public QueryWrapper<T> eq(String column, Object value) {
		conditions.add(column + " = '" + value + "'");
		return this;
	}

	public QueryWrapper<T> ne(String column, Object value) {
		conditions.add(column + " <> '" + value + "'");
		return this;
	}

	public QueryWrapper<T> gt(String column, Object value) {
		conditions.add(column + " > '" + value + "'");
		return this;
	}

	public QueryWrapper<T> ge(String column, Object value) {
		conditions.add(column + " >= '" + value + "'");
		return this;
	}

	public QueryWrapper<T> lt(String column, Object value) {
		conditions.add(column + " < '" + value + "'");
		return this;
	}

	public QueryWrapper<T> le(String column, Object value) {
		conditions.add(column + " <= '" + value + "'");
		return this;
	}

	public QueryWrapper<T> like(String column, String value) {
		conditions.add(column + " LIKE '%" + value + "%'");
		return this;
	}

	public QueryWrapper<T> notLike(String column, String value) {
		conditions.add(column + " NOT LIKE '%" + value + "%'");
		return this;
	}

	public QueryWrapper<T> likeLeft(String column, String value) {
		conditions.add(column + " LIKE '%" + value + "'");
		return this;
	}

	public QueryWrapper<T> likeRight(String column, String value) {
		conditions.add(column + " LIKE '" + value + "%'");
		return this;
	}

	public QueryWrapper<T> isNull(String column) {
		conditions.add(column + " IS NULL");
		return this;
	}

	public QueryWrapper<T> isNotNull(String column) {
		conditions.add(column + " IS NOT NULL");
		return this;
	}

	public QueryWrapper<T> in(String column, Collection<?> values) {
		if (values != null && !values.isEmpty()) {
			String inSql = String.join(", ", wrapStrings(values));
			conditions.add(column + " IN (" + inSql + ")");
		} else {
			// empty in return false
			conditions.add("1 = 0");
		}
		return this;
	}

	public QueryWrapper<T> notIn(String column, Collection<?> values) {
		if (values != null && !values.isEmpty()) {
			String inSql = String.join(", ", wrapStrings(values));
			conditions.add(column + " NOT IN (" + inSql + ")");
		} else {
			// empty not in return true
			conditions.add("1 = 1");
		}
		return this;
	}

	public QueryWrapper<T> between(String column, Object start, Object end) {
		conditions.add(column + " BETWEEN '" + start + "' AND '" + end + "'");
		return this;
	}

	public QueryWrapper<T> notBetween(String column, Object start, Object end) {
		conditions.add(column + " NOT BETWEEN '" + start + "' AND '" + end + "'");
		return this;
	}

	// 逻辑嵌套and
	public QueryWrapper<T> and(Consumer<QueryWrapper<T>> consumer) {
		QueryWrapper<T> nested = new QueryWrapper<>();
		consumer.accept(nested);
		if (!nested.conditions.isEmpty()) {
			conditions.add("(" + String.join(" AND ", nested.conditions) + ")");
		}
		return this;
	}

	// 逻辑嵌套or
	public QueryWrapper<T> or(Consumer<QueryWrapper<T>> consumer) {
		QueryWrapper<T> nested = new QueryWrapper<>();
		consumer.accept(nested);
		if (!nested.conditions.isEmpty()) {
			conditions.add("(" + String.join(" OR ", nested.conditions) + ")");
		}
		return this;
	}

	public QueryWrapper<T> orderByAsc(String... columns) {
		for (String column : columns) {
			orderByColumns.add(column + " ASC");
		}
		return this;
	}

	public QueryWrapper<T> orderByDesc(String... columns) {
		for (String column : columns) {
			orderByColumns.add(column + " DESC");
		}
		return this;
	}

	public QueryWrapper<T> groupBy(String... columns) {
		groupByColumns.addAll(Arrays.asList(columns));
		return this;
	}

	public QueryWrapper<T> having(String sqlHaving) {
		this.havingClause = sqlHaving;
		return this;
	}

	public QueryWrapper<T> apply(String sql) {
		this.applySql = sql;
		return this;
	}

	public QueryWrapper<T> last(String sql) {
		this.lastSql = sql;
		return this;
	}

	// 生成 SQL 片段
	public String getWhereClause() {
	    if (conditions.isEmpty()) return "";
	    return String.join(" AND ", conditions);
	}

	public String getOrderByClause() {
	    if (orderByColumns.isEmpty()) return "";
	    return "ORDER BY " + String.join(", ", orderByColumns);
	}

	public String getGroupByClause() {
	    if (groupByColumns.isEmpty()) return "";
	    return "GROUP BY " + String.join(", ", groupByColumns);
	}

	public String getHavingClause() {
	    if (havingClause == null || havingClause.isEmpty()) return "";
	    return "HAVING " + havingClause;
	}

	public String getLastClause() {
	    return lastSql == null ? "" : lastSql;
	}

	public String getApplyClause() {
	    return applySql == null ? "" : applySql;
	}

	// SQL值包装，字符串加单引号，数字直接
	private List<String> wrapStrings(Collection<?> vals) {
		List<String> result = new ArrayList<>();
		for (Object v : vals) {
			if (v instanceof Number) {
				result.add(v.toString());
			} else {
				result.add("'" + v + "'");
			}
		}
		return result;
	}
}
