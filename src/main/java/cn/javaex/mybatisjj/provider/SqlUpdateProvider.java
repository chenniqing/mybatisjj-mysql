package cn.javaex.mybatisjj.provider;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Stream;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.builder.annotation.ProviderMethodResolver;
import org.apache.ibatis.jdbc.SQL;

import cn.javaex.mybatisjj.entity.TableColumnEntity;
import cn.javaex.mybatisjj.entity.TableEntity;
import cn.javaex.mybatisjj.entity.TableIdEntity;
import cn.javaex.mybatisjj.util.SqlStringUtils;

/**
 * Update构建
 * 
 * @author 陈霓清
 */
public class SqlUpdateProvider extends EntityProvider implements ProviderMethodResolver {
	
	/**
	 * 根据主键更新实体信息（为null的字段不更新）
	 * @param providerContext
	 * @param entity
	 * @return
	 */
	public String updateById(ProviderContext providerContext, Object entity) {
		Class<?> entityType = super.getEntityType(providerContext.getMapperType());
		
		// 获取表实体信息
		TableEntity tableEntity = super.getTableEntity(entityType);
		// 获取表的所有字段
		List<TableColumnEntity> tableColumnEntityList = tableEntity.getTableColumnEntityList();
		
		Stream<TableColumnEntity> stream = tableColumnEntityList.stream();
		// 如果tableIdEntity不为null，则需要过滤掉主键对应的列，否则说明该表没有主键
		TableIdEntity tableIdEntity = tableEntity.getTableIdEntity();
		if (tableIdEntity == null) {
			new RuntimeException("No field marked with @TableId or field named 'id' found.");
		} else {
			String tableIdColumn = tableIdEntity.getColumn();
			stream = stream.filter(tableColumnEntity -> !tableColumnEntity.getColumn().equals(tableIdColumn));
		}
		
		StringBuffer sql = new StringBuffer();
		stream.forEach(tableColumnEntity -> {
		    try {
		        // 不更新值为null的字段
		        // Method method = entity.getClass().getMethod("getAnotherField");
		        Method method = entity.getClass().getMethod("get" + SqlStringUtils.capitalize(tableColumnEntity.getField()));
		        Object value = method.invoke(entity);
		        if (value != null) {
		        	sql.append("`").append(tableColumnEntity.getColumn()).append("`").append(" = #{").append(tableColumnEntity.getField()).append("}, ");
		        }
		    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
		        throw new RuntimeException(e);
		    }
		});
		
		// 删除最后一个逗号
		int lastIndex = sql.lastIndexOf(",");
		if (lastIndex != -1) {
			sql.deleteCharAt(lastIndex);
		}
		
		return new SQL() {{
			UPDATE("`" + tableEntity.getTableName() + "`");
			SET(sql.toString());	// SET("列1 = #{属性1}, 列2 = #{属性2}");
			WHERE("`" + tableIdEntity.getColumn() + "` = #{" + tableIdEntity.getField() + "}");	// WHERE("主键字段 = #{主键属性}");
		}}.toString();
	}

	/**
	 * 根据主键，将指定字段的值更新为null值
	 * @param providerContext
	 * @param column
	 * @param id
	 * @return
	 */
	public String updateNullColumnById(ProviderContext providerContext, @Param("column") String column, @Param("id") String id) {
		Class<?> entityType = super.getEntityType(providerContext.getMapperType());
		
		// 获取表名
		String tableName = super.getTableName(entityType);
		// 获取主键字段名
		String tableId = super.getTableId(entityType);
		
		return new SQL() {{
			UPDATE("`" + tableName + "`");
			SET("`" + column + "` = null");	// SET("列1 = #{属性1}, 列2 = #{属性2}");
			WHERE("`" + tableId + "` = #{id}");	// WHERE("主键字段 = #{主键属性}");
		}}.toString();
	}
	
}
