package cn.javaex.mybatisjj.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import cn.javaex.mybatisjj.basic.common.DbType;
import cn.javaex.mybatisjj.config.customize.DbTypeContextHolder;

/**
 * 数据库类型工具类
 * 
 * @author 陈霓清
 */
public class DbTypeUtils {
	
	/**
	 * 缓存JDBC元信息到数据库类型的解析结果
	 */
	private static final ConcurrentMap<String, DbType> DB_TYPE_CACHE = new ConcurrentHashMap<String, DbType>();
	
	private DbTypeUtils() {
		
	}
	
	/**
	 * 获取当前连接对应的数据库类型
	 * @param connection
	 * @return
	 */
	public static DbType getDbType(Connection connection) {
		DbType contextDbType = DbTypeContextHolder.peek();
		if (contextDbType != null && DbType.OTHER.equals(contextDbType) == false) {
			return contextDbType;
		}
		
		DbType jdbcDbType = resolveDbTypeFromConnection(connection);
		if (jdbcDbType != null && DbType.OTHER.equals(jdbcDbType) == false) {
			return jdbcDbType;
		}
		
		DbType defaultDbType = DbTypeContextHolder.getDefaultDbType();
		if (defaultDbType != null && DbType.OTHER.equals(defaultDbType) == false) {
			return defaultDbType;
		}
		return DbType.OTHER;
	}
	
	/**
	 * 优先根据真实 JDBC 连接识别数据库类型，避免多数据源场景中全局默认值覆盖当前连接的真实方言
	 * @param connection JDBC连接
	 * @return 数据库类型
	 */
	private static DbType resolveDbTypeFromConnection(Connection connection) {
		if (connection == null) {
			return DbType.OTHER;
		}
		try {
			DatabaseMetaData metaData = connection.getMetaData();
			String productName = metaData.getDatabaseProductName();
			String url = metaData.getURL();
			int majorVersion = metaData.getDatabaseMajorVersion();
			String cacheKey = (productName == null ? "" : productName) + "|" + (url == null ? "" : url) + "|" + majorVersion;
			return DB_TYPE_CACHE.computeIfAbsent(cacheKey, key -> DbType.fromJdbc(productName, url, majorVersion));
		} catch (SQLException e) {
			throw new RuntimeException("Get database type failed", e);
		}
	}
	
}
