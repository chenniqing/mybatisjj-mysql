package cn.javaex.mybatisjj.config.datasource;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.sql.DataSource;

import cn.javaex.mybatisjj.config.customize.DataSourceContextHolder;
import cn.javaex.mybatisjj.util.SqlStringUtils;

/**
 * 动态数据源路由
 * 
 * @author 陈霓清
 */
public class DynamicDataSource implements DataSource {
	
	/**
	 * 默认数据源名称，未指定数据源时使用该数据源
	 */
	private String defaultDataSourceName;
	
	/**
	 * 是否严格匹配数据源，true表示找不到数据源时直接抛出异常
	 */
	private boolean strict = true;
	
	/**
	 * 数据源集合，key为数据源名称，value为真实DataSource
	 */
	private final Map<String, DataSource> dataSourceMap = new ConcurrentHashMap<String, DataSource>();
	
	public DynamicDataSource() {
		
	}
	
	public DynamicDataSource(String defaultDataSourceName, Map<String, DataSource> dataSourceMap) {
		this.defaultDataSourceName = defaultDataSourceName;
		this.setDataSourceMap(dataSourceMap);
	}
	
	@Override
	public Connection getConnection() throws SQLException {
		return this.determineDataSource().getConnection();
	}
	
	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		return this.determineDataSource().getConnection(username, password);
	}
	
	@Override
	public PrintWriter getLogWriter() throws SQLException {
		return this.determineDataSource().getLogWriter();
	}
	
	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
		for (DataSource dataSource : dataSourceMap.values()) {
			dataSource.setLogWriter(out);
		}
	}
	
	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
		for (DataSource dataSource : dataSourceMap.values()) {
			dataSource.setLoginTimeout(seconds);
		}
	}
	
	@Override
	public int getLoginTimeout() throws SQLException {
		return this.determineDataSource().getLoginTimeout();
	}
	
	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return this.determineDataSource().getParentLogger();
	}
	
	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		if (iface.isInstance(this)) {
			return iface.cast(this);
		}
		return this.determineDataSource().unwrap(iface);
	}
	
	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return iface.isInstance(this) || this.determineDataSource().isWrapperFor(iface);
	}
	
	/**
	 * 根据当前上下文选择真实数据源
	 * @return
	 */
	public DataSource determineDataSource() {
		String dataSourceName = DataSourceContextHolder.peek();
		if (SqlStringUtils.isEmpty(dataSourceName)) {
			return this.determineDefaultDataSource();
		}
		
		DataSource dataSource = dataSourceMap.get(dataSourceName);
		if (dataSource != null) {
			return dataSource;
		}
		
		if (strict) {
			throw new IllegalStateException("Cannot find data source: " + dataSourceName);
		}
		return this.determineDefaultDataSource();
	}
	
	/**
	 * 获取默认数据源
	 * @return
	 */
	private DataSource determineDefaultDataSource() {
		if (SqlStringUtils.isNotEmpty(defaultDataSourceName)) {
			DataSource dataSource = dataSourceMap.get(defaultDataSourceName);
			if (dataSource == null) {
				throw new IllegalStateException("Cannot find default data source: " + defaultDataSourceName);
			}
			return dataSource;
		}
		
		if (dataSourceMap.size() == 1) {
			return dataSourceMap.values().iterator().next();
		}
		
		throw new IllegalStateException("Default data source is not configured.");
	}
	
	/**
	 * 添加数据源
	 * @param dataSourceName
	 * @param dataSource
	 */
	public void addDataSource(String dataSourceName, DataSource dataSource) {
		if (SqlStringUtils.isEmpty(dataSourceName)) {
			throw new IllegalArgumentException("Data source name cannot be empty.");
		}
		if (dataSource == null) {
			throw new IllegalArgumentException("Data source cannot be null.");
		}
		dataSourceMap.put(dataSourceName, dataSource);
	}
	
	/**
	 * 移除数据源
	 * @param dataSourceName
	 */
	public void removeDataSource(String dataSourceName) {
		dataSourceMap.remove(dataSourceName);
	}
	
	/**
	 * 批量设置数据源集合
	 * @param dataSourceMap
	 */
	public void setDataSourceMap(Map<String, DataSource> dataSourceMap) {
		this.dataSourceMap.clear();
		if (dataSourceMap != null) {
			for (Map.Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
				this.addDataSource(entry.getKey(), entry.getValue());
			}
		}
	}
	
	/**
	 * 获取只读的数据源集合
	 * @return
	 */
	public Map<String, DataSource> getDataSourceMap() {
		return Collections.unmodifiableMap(new LinkedHashMap<String, DataSource>(dataSourceMap));
	}
	
	public String getDefaultDataSourceName() {
		return defaultDataSourceName;
	}
	
	public void setDefaultDataSourceName(String defaultDataSourceName) {
		this.defaultDataSourceName = defaultDataSourceName;
	}
	
	public boolean isStrict() {
		return strict;
	}
	
	public void setStrict(boolean strict) {
		this.strict = strict;
	}
	
}
