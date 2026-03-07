package cn.javaex.mybatisjj.provider;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.builder.annotation.ProviderMethodResolver;
import org.apache.ibatis.jdbc.SQL;

import cn.javaex.mybatisjj.model.entity.TableColumnEntity;
import cn.javaex.mybatisjj.model.entity.TableEntity;
import cn.javaex.mybatisjj.util.ReflectiveUtils;

/**
 * Insert构建
 * 
 * @author 陈霓清
 */
public class SqlInsertProvider extends EntityProvider implements ProviderMethodResolver {

	/**
	 * 插入实体信息
	 * @param providerContext
	 * @param entity
	 * @return
	 */
	public String insert(ProviderContext providerContext, Object entity) {
		Class<?> clazz = super.getEntityType(providerContext.getMapperType());
		
		// 获取表实体信息
		TableEntity tableEntity = super.getTableEntity(clazz);
		// 获取表的所有字段
		List<TableColumnEntity> tableColumnEntityList = tableEntity.getTableColumnEntityList();
		
		return new SQL() {{
			INSERT_INTO(tableEntity.getTableName());
			for (TableColumnEntity tableColumnEntity : tableColumnEntityList) {
				String column = tableColumnEntity.getColumn();
				String field = tableColumnEntity.getField();
				try {
					// 使用反射从实体对象中获取字段值
					Field entityField = ReflectiveUtils.findField(clazz, field);
					entityField.setAccessible(true);
					Object fieldValue = entityField.get(entity);
					
					// 如果字段值不为 null，则添加到插入语句
					if (fieldValue != null) {
						VALUES(column, "#{" + field + "}");
					}
				} catch (NoSuchFieldException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}}.toString();
	}
	
	/**
	 * 批量插入实体信息
	 * @param providerContext
	 * @param list
	 * @return
	 */
	public String batchInsert(ProviderContext providerContext, @Param("list") List<?> list) {
		// 不能进行空或空列表的插入操作
		if (list == null || list.isEmpty()) {
			throw new IllegalArgumentException("Parameter 'list' must not be empty.");
		}
		
		Class<?> clazz = list.get(0).getClass();
		// 获取表实体信息
		TableEntity tableEntity = super.getTableEntity(clazz);
		// 获取表的所有字段
		List<TableColumnEntity> tableColumnEntityList = tableEntity.getTableColumnEntityList();
		
		// 构造所有列名
		List<String> columnNames = new ArrayList<>();
		for (TableColumnEntity tableColumnEntity : tableColumnEntityList) {
			columnNames.add(tableColumnEntity.getColumn());
		}
		
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("INSERT INTO ").append(tableEntity.getTableName()).append(" ");
		sqlBuilder.append("(").append(String.join(", ", columnNames)).append(") VALUES ");
		
		// 构造每条记录的值
		List<String> valueRows = new ArrayList<>();
		for (int i = 0; i < list.size(); i++) {
			Object entity = list.get(i);
			List<String> valuePlaceholders = new ArrayList<>();
			for (TableColumnEntity tableColumnEntity : tableColumnEntityList) {
				String field = tableColumnEntity.getField();
				try {
					// 使用反射获取当前字段值
					Field entityField = ReflectiveUtils.findField(clazz, field);
					entityField.setAccessible(true);
					Object fieldValue = entityField.get(entity);
					if (fieldValue != null) {
						valuePlaceholders.add("#{list[" + i + "]." + field + "}");
					} else {
						valuePlaceholders.add("null");
					}
				} catch (NoSuchFieldException | IllegalAccessException e) {
					e.printStackTrace();
					throw new RuntimeException("Error processing field: " + field, e);
				}
			}
			valueRows.add("(" + String.join(", ", valuePlaceholders) + ")");
		}
		
		sqlBuilder.append(String.join(", ", valueRows));
		return sqlBuilder.toString();
	}

}
