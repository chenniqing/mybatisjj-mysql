package cn.javaex.mybatisjj.model.query;

import java.util.Collections;
import java.util.Map;

/**
 * 通用 Wrapper 接口
 * 
 * @author 陈霓清
 * @Date 2026年2月7日
 * @param <T>
 */
public interface Wrapper<T> {
	/** 只返回where部分，不带WHERE关键字，比如：sex = 1 AND name = 'Tom'，无条件时返回空字符串 */
	String getWhereClause();

	/** 返回 ORDER BY 片段，比如：ORDER BY create_time DESC，无时返回"" */
	String getOrderByClause();

	/** 返回 GROUP BY 片段，比如：GROUP BY user_id，无时返回"" */
	String getGroupByClause();

	/** HAVING 片段，比如：HAVING count(1)>1，无时返回"" */
	String getHavingClause();

	/** 返回 SQL 结尾拼接片段，如 limit、for update，无时返回"" */
	String getLastClause();

	/** 返回自定义apply片段，如果有，无时返回"" */
	String getApplyClause();
	/**
	 * 获取 Wrapper 内部的参数集合，SQL 片段通过 #{wrapper.paramNameValuePairs.xxx} 进行安全绑定
	 * @return 参数集合
	 */
	default Map<String, Object> getParamNameValuePairs() {
		return Collections.emptyMap();
	}
}
