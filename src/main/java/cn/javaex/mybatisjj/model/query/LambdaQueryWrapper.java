package cn.javaex.mybatisjj.model.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
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
	private Map<String, Object> paramNameValuePairs = new LinkedHashMap<>();
	private AtomicInteger paramIndex = new AtomicInteger(0);
	
	public LambdaQueryWrapper() {
		
	}
	
	private LambdaQueryWrapper(Map<String, Object> paramNameValuePairs, AtomicInteger paramIndex) {
		this.paramNameValuePairs = paramNameValuePairs;
		this.paramIndex = paramIndex;
	}

	// 等值
	public <R> LambdaQueryWrapper<T> eq(SFunction<T, R> column, Object value) {
		addCondition(getColumnName(column) + " = " + formatParam(value));
		return this;
	}

	// 不等
	public <R> LambdaQueryWrapper<T> ne(SFunction<T, R> column, Object value) {
		addCondition(getColumnName(column) + " <> " + formatParam(value));
		return this;
	}

	// 大于
	public <R> LambdaQueryWrapper<T> gt(SFunction<T, R> column, Object value) {
		addCondition(getColumnName(column) + " > " + formatParam(value));
		return this;
	}

	// 大于等于
	public <R> LambdaQueryWrapper<T> ge(SFunction<T, R> column, Object value) {
		addCondition(getColumnName(column) + " >= " + formatParam(value));
		return this;
	}

	// 小于
	public <R> LambdaQueryWrapper<T> lt(SFunction<T, R> column, Object value) {
		addCondition(getColumnName(column) + " < " + formatParam(value));
		return this;
	}

	// 小于等于
	public <R> LambdaQueryWrapper<T> le(SFunction<T, R> column, Object value) {
		addCondition(getColumnName(column) + " <= " + formatParam(value));
		return this;
	}

	// like
	public <R> LambdaQueryWrapper<T> like(SFunction<T, R> column, String value) {
		addCondition(getColumnName(column) + " LIKE " + formatParam("%" + value + "%"));
		return this;
	}
	// not like
	public <R> LambdaQueryWrapper<T> notLike(SFunction<T, R> column, String value) {
		addCondition(getColumnName(column) + " NOT LIKE " + formatParam("%" + value + "%"));
		return this;
	}
	// like left
	public <R> LambdaQueryWrapper<T> likeLeft(SFunction<T, R> column, String value) {
		addCondition(getColumnName(column) + " LIKE " + formatParam("%" + value));
		return this;
	}
	// like right
	public <R> LambdaQueryWrapper<T> likeRight(SFunction<T, R> column, String value) {
		addCondition(getColumnName(column) + " LIKE " + formatParam(value + "%"));
		return this;
	}

	// is null
	public <R> LambdaQueryWrapper<T> isNull(SFunction<T, R> column) {
		addCondition(getColumnName(column) + " IS NULL");
		return this;
	}
	public <R> LambdaQueryWrapper<T> isNotNull(SFunction<T, R> column) {
		addCondition(getColumnName(column) + " IS NOT NULL");
		return this;
	}

	// in
	public <R> LambdaQueryWrapper<T> in(SFunction<T, R> column, Collection<?> values) {
		if(values != null && !values.isEmpty()) {
			String inSql = String.join(", ", wrapParams(values));
			addCondition(getColumnName(column) + " IN (" + inSql + ")");
		} else {
			addCondition("1 = 0");
		}
		return this;
	}
	// not in
	public <R> LambdaQueryWrapper<T> notIn(SFunction<T, R> column, Collection<?> values) {
		if(values != null && !values.isEmpty()) {
			String inSql = String.join(", ", wrapParams(values));
			addCondition(getColumnName(column) + " NOT IN (" + inSql + ")");
		} else {
			addCondition("1 = 1");
		}
		return this;
	}

	// between
	public <R> LambdaQueryWrapper<T> between(SFunction<T, R> column, Object start, Object end) {
		addCondition(getColumnName(column) + " BETWEEN " + formatParam(start) + " AND " + formatParam(end));
		return this;
	}
	public <R> LambdaQueryWrapper<T> notBetween(SFunction<T, R> column, Object start, Object end) {
		addCondition(getColumnName(column) + " NOT BETWEEN " + formatParam(start) + " AND " + formatParam(end));
		return this;
	}

	// 嵌套and
	public LambdaQueryWrapper<T> and(Consumer<LambdaQueryWrapper<T>> consumer) {
		LambdaQueryWrapper<T> nested = new LambdaQueryWrapper<>(paramNameValuePairs, paramIndex);
		consumer.accept(nested);
		if(!nested.conditions.isEmpty()) {
			addCondition("(" + nested.getWhereClause() + ")", "AND");
		}
		return this;
	}
	// 嵌套or
	public LambdaQueryWrapper<T> or(Consumer<LambdaQueryWrapper<T>> consumer) {
		LambdaQueryWrapper<T> nested = new LambdaQueryWrapper<>(paramNameValuePairs, paramIndex);
		consumer.accept(nested);
		if(!nested.conditions.isEmpty()) {
			addCondition("(" + nested.getWhereClause() + ")", "OR");
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
	public final <R> LambdaQueryWrapper<T> orderByAsc(boolean condition, SFunction<T, R>... columns) {
		// 条件不满足时跳过排序，方便调用方按需拼接动态排序条件
		if(condition == false) {
			return this;
		}
		return orderByAsc(columns);
	}

	@SafeVarargs
	public final <R> LambdaQueryWrapper<T> orderByDesc(SFunction<T, R>... columns) {
		for(SFunction<T, R> c : columns) {
			orderByColumns.add(getColumnName(c) + " DESC");
		}
		return this;
	}

	@SafeVarargs
	public final <R> LambdaQueryWrapper<T> orderByDesc(boolean condition, SFunction<T, R>... columns) {
		// 条件不满足时跳过排序，字段为空但 condition=true 时仍交给字段解析快速失败
		if(condition == false) {
			return this;
		}
		return orderByDesc(columns);
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
		// having 是高级原生 SQL 入口，只应传入可信 SQL，普通条件请优先使用 eq/gt/in 等参数化方法
		this.havingClause = sqlHaving;
		return this;
	}

	public LambdaQueryWrapper<T> apply(String sql) {
		// apply 是高级原生 SQL 入口，只应传入可信 SQL，普通条件请优先使用 eq/gt/in 等参数化方法
		this.applySql = sql;
		return this;
	}
	public LambdaQueryWrapper<T> last(String sql) {
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

	// 字符串与数字的安全包装
	private List<String> wrapParams(Collection<?> vals) {
		List<String> result = new ArrayList<>();
		for (Object v : vals) {
			result.add(formatParam(v));
		}
		return result;
	}

	// 内部调用 字段名工具
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
	
	private <R> String getColumnName(SFunction<T, R> column) {
		return LambdaUtils.resolveFieldName(column);
	}
}
