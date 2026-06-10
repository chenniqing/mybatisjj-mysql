package cn.javaex.mybatisjj.model.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * 查询封装
 * 
 * @author 陈霓清
 * @Date 2024年5月26日
 * @param <T>
 */
public class QueryWrapper<T> implements Wrapper<T> {
	/**
	 * 普通列名只允许安全标识符，复杂 SQL 请显式走 apply/having/last，避免把外部输入误当列名拼进去
	 */
	private static final Pattern SAFE_COLUMN_PATTERN = Pattern.compile(
			"^(`?[a-zA-Z_][a-zA-Z0-9_$]*`?)(\\.(`?[a-zA-Z_][a-zA-Z0-9_$]*`?))*$");
	
	private List<String> conditions = new ArrayList<>();
	private List<String> orderByColumns = new ArrayList<>();
	private List<String> groupByColumns = new ArrayList<>();
	private String havingClause = null;
	private String applySql = null;
	private String lastSql = null;
	private Map<String, Object> paramNameValuePairs = new LinkedHashMap<>();
	private AtomicInteger paramIndex = new AtomicInteger(0);
	
	public QueryWrapper() {
		
	}
	
	private QueryWrapper(Map<String, Object> paramNameValuePairs, AtomicInteger paramIndex) {
		this.paramNameValuePairs = paramNameValuePairs;
		this.paramIndex = paramIndex;
	}

	public QueryWrapper<T> eq(String column, Object value) {
		addCondition(safeColumn(column) + " = " + formatParam(value));
		return this;
	}

	public QueryWrapper<T> ne(String column, Object value) {
		addCondition(safeColumn(column) + " <> " + formatParam(value));
		return this;
	}

	public QueryWrapper<T> gt(String column, Object value) {
		addCondition(safeColumn(column) + " > " + formatParam(value));
		return this;
	}

	public QueryWrapper<T> ge(String column, Object value) {
		addCondition(safeColumn(column) + " >= " + formatParam(value));
		return this;
	}

	public QueryWrapper<T> lt(String column, Object value) {
		addCondition(safeColumn(column) + " < " + formatParam(value));
		return this;
	}

	public QueryWrapper<T> le(String column, Object value) {
		addCondition(safeColumn(column) + " <= " + formatParam(value));
		return this;
	}

	public QueryWrapper<T> like(String column, String value) {
		addCondition(safeColumn(column) + " LIKE " + formatParam("%" + value + "%"));
		return this;
	}

	public QueryWrapper<T> notLike(String column, String value) {
		addCondition(safeColumn(column) + " NOT LIKE " + formatParam("%" + value + "%"));
		return this;
	}

	public QueryWrapper<T> likeLeft(String column, String value) {
		addCondition(safeColumn(column) + " LIKE " + formatParam("%" + value));
		return this;
	}

	public QueryWrapper<T> likeRight(String column, String value) {
		addCondition(safeColumn(column) + " LIKE " + formatParam(value + "%"));
		return this;
	}

	public QueryWrapper<T> isNull(String column) {
		addCondition(safeColumn(column) + " IS NULL");
		return this;
	}

	public QueryWrapper<T> isNotNull(String column) {
		addCondition(safeColumn(column) + " IS NOT NULL");
		return this;
	}

	public QueryWrapper<T> in(String column, Collection<?> values) {
		if (values != null && !values.isEmpty()) {
			String inSql = String.join(", ", wrapParams(values));
			addCondition(safeColumn(column) + " IN (" + inSql + ")");
		} else {
			// empty in return false
			addCondition("1 = 0");
		}
		return this;
	}

	public QueryWrapper<T> notIn(String column, Collection<?> values) {
		if (values != null && !values.isEmpty()) {
			String inSql = String.join(", ", wrapParams(values));
			addCondition(safeColumn(column) + " NOT IN (" + inSql + ")");
		} else {
			// empty not in return true
			addCondition("1 = 1");
		}
		return this;
	}

	public QueryWrapper<T> between(String column, Object start, Object end) {
		addCondition(safeColumn(column) + " BETWEEN " + formatParam(start) + " AND " + formatParam(end));
		return this;
	}

	public QueryWrapper<T> notBetween(String column, Object start, Object end) {
		addCondition(safeColumn(column) + " NOT BETWEEN " + formatParam(start) + " AND " + formatParam(end));
		return this;
	}

	// 逻辑嵌套and
	public QueryWrapper<T> and(Consumer<QueryWrapper<T>> consumer) {
		QueryWrapper<T> nested = new QueryWrapper<>(paramNameValuePairs, paramIndex);
		consumer.accept(nested);
		if (!nested.conditions.isEmpty()) {
			addCondition("(" + nested.getWhereClause() + ")", "AND");
		}
		return this;
	}

	// 逻辑嵌套or
	public QueryWrapper<T> or(Consumer<QueryWrapper<T>> consumer) {
		QueryWrapper<T> nested = new QueryWrapper<>(paramNameValuePairs, paramIndex);
		consumer.accept(nested);
		if (!nested.conditions.isEmpty()) {
			addCondition("(" + nested.getWhereClause() + ")", "OR");
		}
		return this;
	}

	public QueryWrapper<T> orderByAsc(String... columns) {
		for (String column : columns) {
			orderByColumns.add(safeColumn(column) + " ASC");
		}
		return this;
	}

	public QueryWrapper<T> orderByAsc(boolean condition, String... columns) {
		// 条件不满足时跳过排序，方便调用方按需拼接动态排序条件
		if (condition == false) {
			return this;
		}
		return orderByAsc(columns);
	}

	public QueryWrapper<T> orderByDesc(String... columns) {
		for (String column : columns) {
			orderByColumns.add(safeColumn(column) + " DESC");
		}
		return this;
	}

	public QueryWrapper<T> orderByDesc(boolean condition, String... columns) {
		// 条件不满足时跳过排序，列名为空但 condition=true 时仍交给 safeColumn 快速失败
		if (condition == false) {
			return this;
		}
		return orderByDesc(columns);
	}

	public QueryWrapper<T> groupBy(String... columns) {
		for (String column : Arrays.asList(columns)) {
			groupByColumns.add(safeColumn(column));
		}
		return this;
	}

	public QueryWrapper<T> having(String sqlHaving) {
		// having 是高级原生 SQL 入口，只应传入可信 SQL，普通条件请优先使用 eq/gt/in 等参数化方法
		this.havingClause = sqlHaving;
		return this;
	}

	public QueryWrapper<T> apply(String sql) {
		// apply 是高级原生 SQL 入口，只应传入可信 SQL，普通条件请优先使用 eq/gt/in 等参数化方法
		this.applySql = sql;
		return this;
	}

	public QueryWrapper<T> last(String sql) {
		// last 会直接拼接到 SQL 末尾，只应传入可信 SQL，避免把外部输入透传进来
		this.lastSql = sql;
		return this;
	}

	// 生成 SQL 片段
	public String getWhereClause() {
	    if (conditions.isEmpty()) return "";
	    return String.join(" ", conditions);
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
	private List<String> wrapParams(Collection<?> vals) {
		List<String> result = new ArrayList<>();
		for (Object v : vals) {
			result.add(formatParam(v));
		}
		return result;
	}

	@Override
	public Map<String, Object> getParamNameValuePairs() {
		return paramNameValuePairs;
	}
	
	/**
	 * 添加条件时显式记录连接符，避免 or(...) 被顶层 AND 重新拼回去
	 * @param condition 条件 SQL
	 */
	private void addCondition(String condition) {
		addCondition(condition, "AND");
	}
	
	private void addCondition(String condition, String connector) {
		if (condition == null || condition.trim().isEmpty()) {
			return;
		}
		if (conditions.isEmpty()) {
			conditions.add(condition);
		} else {
			conditions.add(connector + " " + condition);
		}
	}
	
	/**
	 * 生成 MyBatis 参数占位符，真实值放入 Wrapper 参数 Map，避免 SQL 注入和单引号破坏 SQL
	 * @param value 参数值
	 * @return MyBatis 占位符
	 */
	private String formatParam(Object value) {
		String paramName = "MPJ_PARAM_" + paramIndex.incrementAndGet();
		paramNameValuePairs.put(paramName, value);
		return "#{wrapper.paramNameValuePairs." + paramName + "}";
	}
	
	/**
	 * 校验列名只包含常见安全标识符，复杂表达式请使用 apply/having/last 明确表达原生 SQL 意图
	 * @param column 列名
	 * @return 原列名
	 */
	private String safeColumn(String column) {
		if (column == null || SAFE_COLUMN_PATTERN.matcher(column).matches() == false) {
			throw new IllegalArgumentException("Illegal column name: " + column);
		}
		return column;
	}
}
