package cn.javaex.mybatisjj.provider;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.builder.annotation.ProviderMethodResolver;
import org.apache.ibatis.jdbc.SQL;

import cn.javaex.mybatisjj.entity.TableColumnEntity;
import cn.javaex.mybatisjj.entity.TableEntity;

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
		Class<?> entityType = super.getEntityType(providerContext.getMapperType());
		
		// 获取表实体信息
		TableEntity tableEntity = super.getTableEntity(entityType);
		// 获取表的所有字段
		List<TableColumnEntity> tableColumnEntityList = tableEntity.getTableColumnEntityList();
		
		return new SQL() {{
			INSERT_INTO("`" + tableEntity.getTableName() + "`");
			for (TableColumnEntity tableColumnEntity : tableColumnEntityList) {
				String column = tableColumnEntity.getColumn();
				String field = tableColumnEntity.getField();
				VALUES("`" + column + "`", "#{" + field + "}");
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
		// 提取columns
		List<String> columns = tableColumnEntityList.stream()
				.map(TableColumnEntity::getColumn)
				.collect(Collectors.toList());
		 
		// 提取fields
		List<String> fields = tableColumnEntityList.stream()
				.map(TableColumnEntity::getField)
				.collect(Collectors.toList());
		
		// 构建SQL
		SQL sql = new SQL().INSERT_INTO("`" + tableEntity.getTableName() + "`");
		// 添加列名
		String columnNames = columns.stream()
				.map(column -> "`" + column + "`")
				.collect(Collectors.joining(", "));
		sql.INTO_COLUMNS(columnNames);
		
		// 构建插入SQL的值部分
		StringBuffer valuesBuffer = new StringBuffer();
		for (int i = 0; i < list.size(); i++) {
			if (i > 0) {
				valuesBuffer.append(",");
			}
			valuesBuffer.append("(");
			final int index = i;
			String valueTemplate = fields.stream()
				.map(field -> "#{list[" + index + "]." + field + "}")
				.collect(Collectors.joining(","));
			valuesBuffer.append(valueTemplate);
			valuesBuffer.append(")");
		}
		
		return sql.toString() + " VALUES " + valuesBuffer.toString();
	}
	
}
