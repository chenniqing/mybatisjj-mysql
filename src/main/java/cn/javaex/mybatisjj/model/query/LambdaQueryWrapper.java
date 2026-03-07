package cn.javaex.mybatisjj.model.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import cn.javaex.mybatisjj.model.function.SFunction;
import cn.javaex.mybatisjj.util.LambdaUtils;

/**
 * 查询封装
 * 
 * @author 陈霓清
 * @Date 2026年2月6日
 * @param <T>
 */
public class LambdaQueryWrapper<T> implements Wrapper<T> {
	private List<String> conditions = new ArrayList<>();
	private List<String> orderByColumns = new ArrayList<>();
	private List<String> groupByColumns = new ArrayList<>();
	private String havingClause = null;
	private String applySql = null;
	private String lastSql = null;

	// 等值
	public <R> LambdaQueryWrapper<T> eq(SFunction<T, R> column, Object value) {
		conditions.add(getColumnName(column) + " = '" + value + "'");
		return this;
	}

	// 不等
	public <R> LambdaQueryWrapper<T> ne(SFunction<T, R> column, Object value) {
		conditions.add(getColumnName(column) + " <> '" + value + "'");
		return this;
	}

	// 大于
	public <R> LambdaQueryWrapper<T> gt(SFunction<T, R> column, Object value) {
		conditions.add(getColumnName(column) + " > '" + value + "'");
		return this;
	}

	// 大于等于
	public <R> LambdaQueryWrapper<T> ge(SFunction<T, R> column, Object value) {
		conditions.add(getColumnName(column) + " >= '" + value + "'");
		return this;
	}

	// 小于
	public <R> LambdaQueryWrapper<T> lt(SFunction<T, R> column, Object value) {
		conditions.add(getColumnName(column) + " < '" + value + "'");
		return this;
	}

	// 小于等于
	public <R> LambdaQueryWrapper<T> le(SFunction<T, R> column, Object value) {
		conditions.add(getColumnName(column) + " <= '" + value + "'");
		return this;
	}

	// like
	public <R> LambdaQueryWrapper<T> like(SFunction<T, R> column, String value) {
		conditions.add(getColumnName(column) + " LIKE '%" + value + "%'");
		return this;
	}
	// not like
	public <R> LambdaQueryWrapper<T> notLike(SFunction<T, R> column, String value) {
		conditions.add(getColumnName(column) + " NOT LIKE '%" + value + "%'");
		return this;
	}
	// like left
	public <R> LambdaQueryWrapper<T> likeLeft(SFunction<T, R> column, String value) {
		conditions.add(getColumnName(column) + " LIKE '%" + value + "'");
		return this;
	}
	// like right
	public <R> LambdaQueryWrapper<T> likeRight(SFunction<T, R> column, String value) {
		conditions.add(getColumnName(column) + " LIKE '" + value + "%'");
		return this;
	}

	// is null
	public <R> LambdaQueryWrapper<T> isNull(SFunction<T, R> column) {
		conditions.add(getColumnName(column) + " IS NULL");
		return this;
	}
	public <R> LambdaQueryWrapper<T> isNotNull(SFunction<T, R> column) {
		conditions.add(getColumnName(column) + " IS NOT NULL");
		return this;
	}

	// in
	public <R> LambdaQueryWrapper<T> in(SFunction<T, R> column, Collection<?> values) {
		if(values != null && !values.isEmpty()) {
			String inSql = String.join(", ", wrapStrings(values));
			conditions.add(getColumnName(column) + " IN (" + inSql + ")");
		} else {
			conditions.add("1 = 0");
		}
		return this;
	}
	// not in
	public <R> LambdaQueryWrapper<T> notIn(SFunction<T, R> column, Collection<?> values) {
		if(values != null && !values.isEmpty()) {
			String inSql = String.join(", ", wrapStrings(values));
			conditions.add(getColumnName(column) + " NOT IN (" + inSql + ")");
		} else {
			conditions.add("1 = 1");
		}
		return this;
	}

	// between
	public <R> LambdaQueryWrapper<T> between(SFunction<T, R> column, Object start, Object end) {
		conditions.add(getColumnName(column) + " BETWEEN '" + start + "' AND '" + end + "'");
		return this;
	}
	public <R> LambdaQueryWrapper<T> notBetween(SFunction<T, R> column, Object start, Object end) {
		conditions.add(getColumnName(column) + " NOT BETWEEN '" + start + "' AND '" + end + "'");
		return this;
	}

	// 嵌套and
	public LambdaQueryWrapper<T> and(Consumer<LambdaQueryWrapper<T>> consumer) {
		LambdaQueryWrapper<T> nested = new LambdaQueryWrapper<>();
		consumer.accept(nested);
		if(!nested.conditions.isEmpty()) {
			conditions.add("(" + String.join(" AND ", nested.conditions) + ")");
		}
		return this;
	}
	// 嵌套or
	public LambdaQueryWrapper<T> or(Consumer<LambdaQueryWrapper<T>> consumer) {
		LambdaQueryWrapper<T> nested = new LambdaQueryWrapper<>();
		consumer.accept(nested);
		if(!nested.conditions.isEmpty()) {
			conditions.add("(" + String.join(" OR ", nested.conditions) + ")");
		}
		return this;
	}

	// 排序
	@SafeVarargs
	public final <R> LambdaQueryWrapper<T> orderByAsc(SFunction<T, R>... columns) {
		for(SFunction<T, R> c : columns) {
			orderByColumns.add(getColumnName(c) + " ASC");
		}
		return this;
	}
	@SafeVarargs
	public final <R> LambdaQueryWrapper<T> orderByDesc(SFunction<T, R>... columns) {
		for(SFunction<T, R> c : columns) {
			orderByColumns.add(getColumnName(c) + " DESC");
		}
		return this;
	}

	// 分组
	@SafeVarargs
	public final <R> LambdaQueryWrapper<T> groupBy(SFunction<T, R>... columns) {
		for(SFunction<T, R> c : columns) {
			groupByColumns.add(getColumnName(c));
		}
		return this;
	}

	// having
	public LambdaQueryWrapper<T> having(String sqlHaving) {
		this.havingClause = sqlHaving;
		return this;
	}

	public LambdaQueryWrapper<T> apply(String sql) {
		this.applySql = sql;
		return this;
	}
	public LambdaQueryWrapper<T> last(String sql) {
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

	// 字符串与数字的安全包装
	private List<String> wrapStrings(Collection<?> vals) {
		List<String> result = new ArrayList<>();
		for (Object v : vals) {
			if (v instanceof Number) {
				result.add(String.valueOf(v));
			} else {
				result.add("'" + v + "'");
			}
		}
		return result;
	}

	// 内部调用 字段名工具
	private <R> String getColumnName(SFunction<T, R> column) {
		return LambdaUtils.resolveFieldName(column);
	}
}
