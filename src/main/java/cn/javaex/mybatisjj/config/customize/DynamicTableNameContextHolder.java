package cn.javaex.mybatisjj.config.customize;

import java.io.Closeable;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import cn.javaex.mybatisjj.util.SqlStringUtils;

/**
 * 动态表名上下文
 * 
 * @author 陈霓清
 */
public class DynamicTableNameContextHolder {
	
	/**
	 * 使用栈结构保存动态表名处理器，支持方法嵌套切换表名
	 */
	private static final ThreadLocal<Deque<DynamicTableNameHandler>> TABLE_NAME_HANDLER_HOLDER = new ThreadLocal<Deque<DynamicTableNameHandler>>() {
		@Override
		protected Deque<DynamicTableNameHandler> initialValue() {
			return new ArrayDeque<DynamicTableNameHandler>();
		}
	};
	
	/**
	 * 压入动态表名处理器
	 * @param handler
	 */
	public static void push(DynamicTableNameHandler handler) {
		if (handler == null) {
			throw new IllegalArgumentException("Dynamic table name handler cannot be null.");
		}
		TABLE_NAME_HANDLER_HOLDER.get().push(handler);
	}
	
	/**
	 * 压入逻辑表名和真实表名的映射关系
	 * @param tableNameMap
	 */
	public static void push(Map<String, String> tableNameMap) {
		if (tableNameMap == null || tableNameMap.isEmpty()) {
			throw new IllegalArgumentException("Dynamic table name map cannot be empty.");
		}
		final Map<String, String> finalTableNameMap = Collections.unmodifiableMap(new HashMap<String, String>(tableNameMap));
		push(new DynamicTableNameHandler() {
			@Override
			public String dynamicTableName(String sql, String tableName) {
				return finalTableNameMap.get(tableName);
			}
		});
	}
	
	/**
	 * 压入单个逻辑表名和真实表名的映射关系
	 * @param tableName
	 * @param dynamicTableName
	 */
	public static void push(String tableName, String dynamicTableName) {
		if (SqlStringUtils.isEmpty(tableName) || SqlStringUtils.isEmpty(dynamicTableName)) {
			throw new IllegalArgumentException("Table name and dynamic table name cannot be empty.");
		}
		Map<String, String> tableNameMap = new HashMap<String, String>();
		tableNameMap.put(tableName, dynamicTableName);
		push(tableNameMap);
	}
	
	/**
	 * 根据当前上下文获取真实表名
	 * @param sql 当前执行的SQL
	 * @param tableName 原始逻辑表名
	 * @return
	 */
	public static String resolveTableName(String sql, String tableName) {
		if (SqlStringUtils.isEmpty(tableName)) {
			return null;
		}
		Deque<DynamicTableNameHandler> deque = TABLE_NAME_HANDLER_HOLDER.get();
		for (DynamicTableNameHandler handler : deque) {
			String dynamicTableName = handler.dynamicTableName(sql, tableName);
			if (SqlStringUtils.isNotEmpty(dynamicTableName)) {
				return dynamicTableName;
			}
		}
		return null;
	}
	
	/**
	 * 判断当前线程是否存在动态表名处理器
	 * @return
	 */
	public static boolean hasTableNameHandler() {
		return TABLE_NAME_HANDLER_HOLDER.get().isEmpty() == false;
	}
	
	/**
	 * 弹出当前线程最近一次压入的动态表名处理器
	 */
	public static void poll() {
		Deque<DynamicTableNameHandler> deque = TABLE_NAME_HANDLER_HOLDER.get();
		if (deque.isEmpty() == false) {
			deque.poll();
		}
		if (deque.isEmpty()) {
			TABLE_NAME_HANDLER_HOLDER.remove();
		}
	}
	
	/**
	 * 清空当前线程的动态表名上下文
	 */
	public static void clear() {
		TABLE_NAME_HANDLER_HOLDER.remove();
	}
	
	/**
	 * 使用资源自动关闭方式临时切换动态表名
	 * @param handler
	 * @return
	 */
	public static DynamicTableNameScope use(DynamicTableNameHandler handler) {
		push(handler);
		return new DynamicTableNameScope();
	}
	
	/**
	 * 使用资源自动关闭方式临时切换动态表名
	 * @param tableName
	 * @param dynamicTableName
	 * @return
	 */
	public static DynamicTableNameScope use(String tableName, String dynamicTableName) {
		push(tableName, dynamicTableName);
		return new DynamicTableNameScope();
	}
	
	/**
	 * 使用资源自动关闭方式临时切换动态表名
	 * @param tableNameMap
	 * @return
	 */
	public static DynamicTableNameScope use(Map<String, String> tableNameMap) {
		push(tableNameMap);
		return new DynamicTableNameScope();
	}
	
	/**
	 * 动态表名作用域，关闭时自动恢复到上一层表名规则
	 */
	public static class DynamicTableNameScope implements Closeable {
		
		@Override
		public void close() {
			DynamicTableNameContextHolder.poll();
		}
		
	}
	
}
