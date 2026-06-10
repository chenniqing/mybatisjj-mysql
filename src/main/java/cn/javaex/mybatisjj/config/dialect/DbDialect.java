package cn.javaex.mybatisjj.config.dialect;

/**
 * 数据库方言
 * 
 * @author 陈霓清
 */
public interface DbDialect {
	
	/**
	 * 构建分页SQL
	 * @param sql 原始SQL
	 * @param offset 偏移量
	 * @param limit 每页数量
	 * @return
	 */
	String buildPaginationSql(String sql, long offset, long limit);
	
	/**
	 * 构建统计总数SQL
	 * @param sql 原始SQL
	 * @return
	 */
	String buildCountSql(String sql);
	
	/**
	 * 获取自增主键查询SQL，不支持时返回null
	 * @return
	 */
	String getIdentitySql();
	
}
