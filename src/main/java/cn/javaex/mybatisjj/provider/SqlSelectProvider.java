package cn.javaex.mybatisjj.provider;

import java.io.Serializable;
import java.util.Collection;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.builder.annotation.ProviderMethodResolver;
import org.apache.ibatis.jdbc.SQL;

/**
 * Select构建
 * 
 * @author 陈霓清
 */
public class SqlSelectProvider extends EntityProvider implements ProviderMethodResolver {
	
	/**
	 * 根据主键查询实体信息
	 * @param providerContext
	 * @return
	 */
	public String selectById(ProviderContext providerContext) {
		Class<?> entityType = super.getEntityType(providerContext.getMapperType());
		
		// 获取表名
		String tableName = super.getTableName(entityType);
		// 获取主键字段名
		String tableId = super.getTableId(entityType);
		
		return new SQL() {{
			SELECT("*");
			FROM("`" + tableName + "`");
			WHERE("`" + tableId + "` = #{id}");
		}}.toString();
	}
	
	/**
	 * 根据主键集合批量查询
	 * @param providerContext
	 * @param ids
	 * @return
	 */
	public String selectListByIds(ProviderContext providerContext, @Param("ids") Collection<? extends Serializable> ids) {
		Class<?> entityType = super.getEntityType(providerContext.getMapperType());
		
		// 获取表名
		String tableName = super.getTableName(entityType);
		// 获取主键字段名
		String tableId = super.getTableId(entityType);
		
		return new SQL() {{
			SELECT("*");
			FROM("`" + tableName + "`");
			WHERE("`" + tableId + "` IN (" + parameterize(ids) + ")");
		}}.toString();
	}
	
	/**
	 * 根据指定字段批量查询
	 * @param providerContext
	 * @param column
	 * @param columnValue
	 * @return
	 */
	public String selectListByColumn(ProviderContext providerContext, @Param("column") String column, @Param("columnValue") Object columnValue) {
		Class<?> entityType = super.getEntityType(providerContext.getMapperType());
		
		// 获取表名
		String tableName = super.getTableName(entityType);
		
		return new SQL() {{
			SELECT("*");
			FROM("`" + tableName + "`");
			WHERE("`" + column + "` = #{columnValue}");
		}}.toString();
	}
	
	/**
	 * 根据指定字段查询统计数量
	 * @param providerContext
	 * @param column
	 * @param columnValue
	 * @return
	 */
	public String selectCountByColumn(ProviderContext providerContext, @Param("column") String column, @Param("columnValue") Object columnValue) {
		Class<?> entityType = super.getEntityType(providerContext.getMapperType());
		
		// 获取表名
		String tableName = super.getTableName(entityType);
		
		return new SQL() {{
			SELECT("COUNT(*)");
			FROM("`" + tableName + "`");
			WHERE("`" + column + "` = #{columnValue}");
		}}.toString();
	}
	
}
