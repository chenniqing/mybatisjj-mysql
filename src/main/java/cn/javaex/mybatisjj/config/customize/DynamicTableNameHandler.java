package cn.javaex.mybatisjj.config.customize;

/**
 * 动态表名处理器
 * 
 * @author 陈霓清
 */
public interface DynamicTableNameHandler {
	
	/**
	 * 根据当前SQL和逻辑表名返回真实表名
	 * @param sql 当前执行的SQL
	 * @param tableName 原始逻辑表名
	 * @return 真实表名，返回null或空字符串时表示不替换
	 */
	String dynamicTableName(String sql, String tableName);
	
}
