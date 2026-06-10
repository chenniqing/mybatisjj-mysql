package cn.javaex.mybatisjj.config.customize;

import java.io.Closeable;
import java.util.ArrayDeque;
import java.util.Deque;

import cn.javaex.mybatisjj.util.SqlStringUtils;

/**
 * 多数据源上下文
 * 
 * @author 陈霓清
 */
public class DataSourceContextHolder {
	
	/**
	 * 使用栈结构保存数据源名称，支持方法嵌套切换数据源
	 */
	private static final ThreadLocal<Deque<String>> DATA_SOURCE_HOLDER = new ThreadLocal<Deque<String>>() {
		@Override
		protected Deque<String> initialValue() {
			return new ArrayDeque<String>();
		}
	};
	
	/**
	 * 压入当前线程要使用的数据源名称
	 * @param dataSourceName
	 */
	public static void push(String dataSourceName) {
		if (SqlStringUtils.isEmpty(dataSourceName)) {
			throw new IllegalArgumentException("Data source name cannot be empty.");
		}
		DATA_SOURCE_HOLDER.get().push(dataSourceName);
	}
	
	/**
	 * 获取当前线程正在使用的数据源名称
	 * @return
	 */
	public static String peek() {
		Deque<String> deque = DATA_SOURCE_HOLDER.get();
		return deque.isEmpty() ? null : deque.peek();
	}
	
	/**
	 * 弹出当前线程最近一次压入的数据源名称
	 */
	public static void poll() {
		Deque<String> deque = DATA_SOURCE_HOLDER.get();
		if (deque.isEmpty() == false) {
			deque.poll();
		}
		if (deque.isEmpty()) {
			DATA_SOURCE_HOLDER.remove();
		}
	}
	
	/**
	 * 清空当前线程的数据源上下文
	 */
	public static void clear() {
		DATA_SOURCE_HOLDER.remove();
	}
	
	/**
	 * 使用资源自动关闭方式临时切换数据源
	 * @param dataSourceName
	 * @return
	 */
	public static DataSourceScope use(String dataSourceName) {
		push(dataSourceName);
		return new DataSourceScope();
	}
	
	/**
	 * 数据源作用域，关闭时自动恢复到上一层数据源
	 */
	public static class DataSourceScope implements Closeable {
		
		@Override
		public void close() {
			DataSourceContextHolder.poll();
		}
		
	}
	
}
