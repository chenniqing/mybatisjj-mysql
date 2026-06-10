package cn.javaex.mybatisjj.config.customize;

import java.io.Closeable;
import java.util.ArrayDeque;
import java.util.Deque;

import cn.javaex.mybatisjj.basic.common.DbType;

/**
 * 数据库类型上下文
 * 
 * @author 陈霓清
 */
public class DbTypeContextHolder {
	
	/**
	 * 使用栈结构保存数据库类型，支持方法嵌套切换数据库类型
	 */
	private static final ThreadLocal<Deque<DbType>> DB_TYPE_HOLDER = new ThreadLocal<Deque<DbType>>() {
		@Override
		protected Deque<DbType> initialValue() {
			return new ArrayDeque<DbType>();
		}
	};
	
	/**
	 * 全局默认数据库类型，无法从JDBC元信息识别时使用
	 */
	private static volatile DbType defaultDbType;
	
	/**
	 * 压入当前线程要使用的数据库类型
	 * @param dbType
	 */
	public static void push(DbType dbType) {
		if (dbType == null) {
			throw new IllegalArgumentException("DbType cannot be null.");
		}
		DB_TYPE_HOLDER.get().push(dbType);
	}
	
	/**
	 * 压入当前线程要使用的数据库类型
	 * @param dbType
	 */
	public static void push(String dbType) {
		push(DbType.fromCode(dbType));
	}
	
	/**
	 * 获取当前线程正在使用的数据库类型
	 * @return
	 */
	public static DbType peek() {
		Deque<DbType> deque = DB_TYPE_HOLDER.get();
		return deque.isEmpty() ? null : deque.peek();
	}
	
	/**
	 * 弹出当前线程最近一次压入的数据库类型
	 */
	public static void poll() {
		Deque<DbType> deque = DB_TYPE_HOLDER.get();
		if (deque.isEmpty() == false) {
			deque.poll();
		}
		if (deque.isEmpty()) {
			DB_TYPE_HOLDER.remove();
		}
	}
	
	/**
	 * 清空当前线程的数据库类型上下文
	 */
	public static void clear() {
		DB_TYPE_HOLDER.remove();
	}
	
	/**
	 * 使用资源自动关闭方式临时切换数据库类型
	 * @param dbType
	 * @return
	 */
	public static DbTypeScope use(DbType dbType) {
		push(dbType);
		return new DbTypeScope();
	}
	
	/**
	 * 使用资源自动关闭方式临时切换数据库类型
	 * @param dbType
	 * @return
	 */
	public static DbTypeScope use(String dbType) {
		push(dbType);
		return new DbTypeScope();
	}
	
	public static DbType getDefaultDbType() {
		return defaultDbType;
	}
	
	public static void setDefaultDbType(DbType defaultDbType) {
		DbTypeContextHolder.defaultDbType = defaultDbType;
	}
	
	public static void setDefaultDbType(String defaultDbType) {
		DbTypeContextHolder.defaultDbType = DbType.fromCode(defaultDbType);
	}
	
	/**
	 * 数据库类型作用域，关闭时自动恢复到上一层数据库类型
	 */
	public static class DbTypeScope implements Closeable {
		
		@Override
		public void close() {
			DbTypeContextHolder.poll();
		}
		
	}
	
}
