package cn.javaex.mybatisjj.provider;

import java.io.Serializable;
import java.util.Collection;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.builder.annotation.ProviderMethodResolver;
import org.apache.ibatis.jdbc.SQL;

/**
 * Delete构建
 * 
 * @author 陈霓清
 */
public class SqlDeleteProvider extends EntityProvider implements ProviderMethodResolver {

	/**
	 * 根据主键删除数据
	 * @param providerContext
	 * @return
	 */
	public String deleteById(ProviderContext providerContext) {
		Class<?> entityType = super.getEntityType(providerContext.getMapperType());
		
		// 获取表名
		String tableName = super.getTableName(entityType);
		// 获取主键字段名
		String tableId = super.getTableId(entityType);
		
		return new SQL() {{
			DELETE_FROM("`" + tableName + "`");
			WHERE("`" + tableId + "` = #{id}");
		}}.toString();
	}
	
	/**
	 * 根据主键集合批量删除数据
	 * @param providerContext
	 * @return
	 */
	public String deleteByIds(ProviderContext providerContext, @Param("ids") Collection<? extends Serializable> ids) {
		if (ids == null || ids.isEmpty()) {
			throw new IllegalArgumentException("The 'ids' parameter cannot be null or empty.");
		}
		
		Class<?> entityType = super.getEntityType(providerContext.getMapperType());
		
		// 获取表名
		String tableName = super.getTableName(entityType);
		// 获取主键字段名
		String tableId = super.getTableId(entityType);
		
		// 开始构建SQL语句
		StringBuffer sql = new StringBuffer();
		sql.append("DELETE FROM ");
		sql.append("`" + tableName + "`");
		// 拼接包含占位符的 IN 子句
		sql.append(" WHERE `" + tableId + "` IN (");
		for (int i = 0; i < ids.size(); i++) {
			if (i > 0) {
				sql.append(", ");
			}
			sql.append("#{ids[").append(i).append("]}");
		}
		sql.append(")");
		
		return sql.toString();
	}
	
	/**
	 * 根据某个字段删除数据
	 * @param providerContext
	 * @param column
	 * @param columnValue
	 * @return
	 */
	public String deleteByColumn(ProviderContext providerContext, @Param("column") String column, @Param("columnValue") Object columnValue) {
		Class<?> entityType = super.getEntityType(providerContext.getMapperType());
		
		// 获取表名
		String tableName = super.getTableName(entityType);
		
		return new SQL() {{
			DELETE_FROM("`" + tableName + "`");
			WHERE("`" + column + "` = #{columnValue}");
		}}.toString();
	}
	
}
