package cn.javaex.mybatisjj.provider;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.builder.annotation.ProviderMethodResolver;
import org.apache.ibatis.jdbc.SQL;

import cn.javaex.mybatisjj.basic.annotation.Version;
import cn.javaex.mybatisjj.model.entity.EntityMeta;
import cn.javaex.mybatisjj.model.entity.TableColumnEntity;
import cn.javaex.mybatisjj.model.entity.TableEntity;
import cn.javaex.mybatisjj.model.entity.TableIdEntity;
import cn.javaex.mybatisjj.model.query.Wrapper;
import cn.javaex.mybatisjj.util.SqlStringUtils;
import cn.javaex.mybatisjj.util.UpdateEntity;

/**
 * Update构建
 * 
 * @author 陈霓清
 */
public class SqlUpdateProvider extends EntityProvider implements ProviderMethodResolver {
	
	/**
	 * 根据主键更新实体信息
	 * @param providerContext
	 * @param entity
	 * @return
	 */
	public String updateById(ProviderContext providerContext, Object entity) {
		EntityMeta meta = super.extractEntityMeta(providerContext);
		Class<?> entityType = super.getEntityType(providerContext.getMapperType());
		TableEntity tableEntity = super.getTableEntity(entityType);
		TableIdEntity tableIdEntity = tableEntity.getTableIdEntity();
		if (tableIdEntity == null) {
			throw new RuntimeException("No field marked with @TableId or field named 'id' found.");
		}
		
		BiFunction<String, Object, Boolean> shouldUpdate;
		BiFunction<String, Object, Boolean> isNullSetter;
		
		if (entity instanceof UpdateEntity.Updatable) {
			// 只更新updatable.set过的字段，并且set过为null就更新为null，否则为占位符
			Set<String> modifiedFieldNames = ((UpdateEntity.Updatable) entity).getModifiedFields().keySet();
			shouldUpdate = (fieldName, value) -> modifiedFieldNames.contains(fieldName);
			isNullSetter = (fieldName, value) -> value == null;
		} else {
			// 普通object，只更新非null字段
			shouldUpdate = (fieldName, value) -> value != null;
			// isNullSetter参数不会实际调用
			isNullSetter = (fieldName, value) -> false;
		}
		
		return this.buildUpdateSql(
			meta, tableIdEntity, entity, tableEntity,
			shouldUpdate,
			isNullSetter,
			null, ""
		);
	}
	
	/**
	 * 根据主键更新实体信息（未设置值的，更新为NULL）
	 * @param providerContext
	 * @param entity
	 * @return
	 */
	public String updateByIdWithNull(ProviderContext providerContext, Object entity) {
		EntityMeta meta = super.extractEntityMeta(providerContext);
		Class<?> entityType = super.getEntityType(providerContext.getMapperType());
		TableEntity tableEntity = super.getTableEntity(entityType);
		TableIdEntity tableIdEntity = tableEntity.getTableIdEntity();
		if (tableIdEntity == null) {
			throw new RuntimeException("No field marked with @TableId or field named 'id' found.");
		}
		
		// 全字段都更新，为 null 也更新为 null
		return this.buildUpdateSql(
			meta, tableIdEntity, entity, tableEntity,
			(fieldName, value) -> true, // 全都要更新
			(fieldName, value) -> value == null, // null 值置为 null
			null, ""
		);
	}
	
	/**
	 * 根据主键批量更新实体信息
	 * @param providerContext
	 * @param list
	 * @return
	 */
	public String updateBatch(ProviderContext providerContext, @Param("list") List<?> list) {
		if (list == null || list.isEmpty()) {
			throw new IllegalArgumentException("Parameter 'list' must not be empty.");
		}
		
		EntityMeta meta = super.extractEntityMeta(providerContext);
		Class<?> entityType = super.getEntityType(providerContext.getMapperType());
		TableEntity tableEntity = super.getTableEntity(entityType);
		TableIdEntity tableIdEntity = tableEntity.getTableIdEntity();
		if (tableIdEntity == null) {
			throw new RuntimeException("No field marked with @TableId or field named 'id' found.");
		}
		
		return this.buildBatchUpdateSql(meta, tableIdEntity, list, tableEntity, false);
	}
	
	/**
	 * 根据主键批量更新实体信息（未设置值的，更新为NULL）
	 * @param providerContext
	 * @param list
	 * @return
	 */
	public String updateBatchWithNull(ProviderContext providerContext, @Param("list") List<?> list) {
		if (list == null || list.isEmpty()) {
			throw new IllegalArgumentException("Parameter 'list' must not be empty.");
		}
		
		EntityMeta meta = super.extractEntityMeta(providerContext);
		Class<?> entityType = super.getEntityType(providerContext.getMapperType());
		TableEntity tableEntity = super.getTableEntity(entityType);
		TableIdEntity tableIdEntity = tableEntity.getTableIdEntity();
		if (tableIdEntity == null) {
			throw new RuntimeException("No field marked with @TableId or field named 'id' found.");
		}
		
		return this.buildBatchUpdateSql(meta, tableIdEntity, list, tableEntity, true);
	}
	
	/**
	 * 根据查询条件更新数据
	 * @param providerContext
	 * @param entity
	 * @param wrapper
	 * @return
	 */
	public String updateByCondition(ProviderContext providerContext, @Param("entity") Object entity, @Param("wrapper") Wrapper<?> wrapper) {
		EntityMeta meta = super.extractEntityMeta(providerContext);
		Class<?> entityType = super.getEntityType(providerContext.getMapperType());
		TableEntity tableEntity = super.getTableEntity(entityType);
		TableIdEntity tableIdEntity = tableEntity.getTableIdEntity();
		if (tableIdEntity == null) {
			throw new RuntimeException("No field marked with @TableId or field named 'id' found.");
		}
		
		BiFunction<String, Object, Boolean> shouldUpdate;
		BiFunction<String, Object, Boolean> isNullSetter;
		
		if (entity instanceof UpdateEntity.Updatable) {
			// 只更新updatable.set过的字段，并且set过为null就更新为null，否则为占位符
			Set<String> modifiedFieldNames = ((UpdateEntity.Updatable) entity).getModifiedFields().keySet();
			shouldUpdate = (fieldName, value) -> modifiedFieldNames.contains(fieldName);
			isNullSetter = (fieldName, value) -> value == null;
		} else {
			// 普通object，只更新非null字段
			shouldUpdate = (fieldName, value) -> value != null;
			// isNullSetter参数不会实际调用
			isNullSetter = (fieldName, value) -> false;
		}
		
		// 更新条件
		String whereClause = this.buildWhereClause(entityType, wrapper);
		
		return this.buildUpdateSql(
			meta, tableIdEntity, entity, tableEntity,
			shouldUpdate,
			isNullSetter,
			whereClause, "entity."
		);
	}
	
	/**
	 * 根据查询条件更新数据（未设置值的，更新为NULL）
	 * @param providerContext
	 * @param entity
	 * @param wrapper
	 * @return
	 */
	public String updateByConditionWithNull(ProviderContext providerContext, @Param("entity") Object entity, @Param("wrapper") Wrapper<?> wrapper) {
		EntityMeta meta = super.extractEntityMeta(providerContext);
		Class<?> entityType = super.getEntityType(providerContext.getMapperType());
		TableEntity tableEntity = super.getTableEntity(entityType);
		TableIdEntity tableIdEntity = tableEntity.getTableIdEntity();
		if (tableIdEntity == null) {
			throw new RuntimeException("No field marked with @TableId or field named 'id' found.");
		}
		
		// 更新条件
		String whereClause = this.buildWhereClause(entityType, wrapper);
		
		// 全字段都更新，为 null 也更新为 null
		return this.buildUpdateSql(
			meta, tableIdEntity, entity, tableEntity,
			(fieldName, value) -> true, // 全都要更新
			(fieldName, value) -> value == null, // null 值置为 null
			whereClause, "entity."
		);
	}
	
	/**
	 * 通用 WHERE 条件拼接（返回条件表达式，不要带WHERE关键字，且多个条件用 AND 连接）
	 * @param entityType
	 * @param wrapper
	 * @return
	 */
	private String buildWhereClause(Class<?> entityType, Wrapper<?> wrapper) {
		return wrapper != null && wrapper.getWhereClause() != null ? wrapper.getWhereClause().trim() : "";
	}
	
	/**
	 * 构建批量 update SQL
	 * @param meta
	 * @param tableIdEntity
	 * @param list
	 * @param tableEntity
	 * @return
	 */
	private String buildBatchUpdateSql(EntityMeta meta, TableIdEntity tableIdEntity, List<?> list, TableEntity tableEntity, boolean updateNull) {
		List<TableColumnEntity> tableColumnEntityList = tableEntity.getTableColumnEntityList();
		Field versionField = meta.versionOpt.orElse(null);
		String versionColumnName = versionField == null ? null : SqlStringUtils.getTableOrColumnName(versionField, Version.class);
		StringBuilder setSql = new StringBuilder();
		
		for (TableColumnEntity columnEntity : tableColumnEntityList) {
			String fieldName = columnEntity.getField();
			String columnName = columnEntity.getColumn();
			if (columnName.equals(tableIdEntity.getColumn())) continue;
			if (versionColumnName != null && columnName.equals(versionColumnName)) continue;
			
			List<BatchUpdateItem> updateItemList = this.getBatchUpdateItemList(list, fieldName, updateNull);
			if (updateItemList.isEmpty()) {
				continue;
			}
			
			setSql.append(columnName).append(" = CASE ").append(tableIdEntity.getColumn()).append(" ");
			for (BatchUpdateItem updateItem : updateItemList) {
				setSql.append("WHEN #{list[").append(updateItem.getIndex()).append("].").append(tableIdEntity.getField()).append("} THEN ");
				if (updateItem.isNullSetter()) {
					setSql.append("NULL ");
				} else {
					setSql.append("#{list[").append(updateItem.getIndex()).append("].").append(fieldName).append("} ");
				}
			}
			setSql.append("ELSE ").append(columnName).append(" END, ");
		}
		
		List<Integer> versionIndexList = this.getVersionIndexList(list, versionField);
		if (versionColumnName != null && versionIndexList.isEmpty() == false) {
			setSql.append(versionColumnName).append(" = CASE ").append(tableIdEntity.getColumn()).append(" ");
			for (Integer index : versionIndexList) {
				setSql.append("WHEN #{list[").append(index).append("].").append(tableIdEntity.getField()).append("} THEN ")
						.append(versionColumnName).append(" + 1 ");
			}
			setSql.append("ELSE ").append(versionColumnName).append(" END, ");
		}
		
		int lastIndex = setSql.lastIndexOf(",");
		if (lastIndex != -1) {
			setSql.delete(lastIndex, lastIndex + 2);
		}
		if (setSql.length() == 0) {
			throw new RuntimeException("No field to update.");
		}
		
		String whereSql = this.buildBatchWhereSql(meta, tableIdEntity, list, versionField, versionColumnName, versionIndexList);
		return "UPDATE " + meta.tableName + " SET " + setSql + " WHERE " + whereSql;
	}
	
	/**
	 * 获取指定字段需要更新的行
	 * @param list
	 * @param fieldName
	 * @return
	 */
	private List<BatchUpdateItem> getBatchUpdateItemList(List<?> list, String fieldName, boolean updateNull) {
		List<BatchUpdateItem> updateItemList = new ArrayList<BatchUpdateItem>();
		for (int i = 0; i < list.size(); i++) {
			Object entity = list.get(i);
			Object value = this.getFieldValue(entity, fieldName);
			if (this.shouldUpdateField(entity, fieldName, value, updateNull)) {
				updateItemList.add(new BatchUpdateItem(i, value == null));
			}
		}
		return updateItemList;
	}
	
	/**
	 * 获取启用乐观锁的行下标
	 * @param list
	 * @param versionField
	 * @return
	 */
	private List<Integer> getVersionIndexList(List<?> list, Field versionField) {
		List<Integer> versionIndexList = new ArrayList<Integer>();
		if (versionField == null) {
			return versionIndexList;
		}
		
		for (int i = 0; i < list.size(); i++) {
			Object versionValue = this.getFieldValue(list.get(i), versionField.getName());
			if (versionValue != null && (!(versionValue instanceof CharSequence) || ((CharSequence) versionValue).length() > 0)) {
				versionIndexList.add(i);
			}
		}
		return versionIndexList;
	}
	
	/**
	 * 构建批量更新的WHERE条件
	 * @param meta
	 * @param tableIdEntity
	 * @param list
	 * @param versionField
	 * @param versionColumnName
	 * @param versionIndexList
	 * @return
	 */
	private String buildBatchWhereSql(EntityMeta meta, TableIdEntity tableIdEntity, List<?> list, Field versionField,
			String versionColumnName, List<Integer> versionIndexList) {
		StringBuilder whereSql = new StringBuilder();
		for (int i = 0; i < list.size(); i++) {
			if (i > 0) {
				whereSql.append(" OR ");
			}
			whereSql.append("(").append(tableIdEntity.getColumn()).append(" = #{list[").append(i).append("].")
					.append(tableIdEntity.getField()).append("}");
			if (versionField != null && versionColumnName != null && versionIndexList.contains(i)) {
				whereSql.append(" AND ").append(versionColumnName).append(" = #{list[").append(i).append("].")
						.append(versionField.getName()).append("}");
			}
			whereSql.append(")");
		}
		
		StringBuilder commonWhereSql = new StringBuilder();
		if (meta.logicDeleteFieldOpt.isPresent()) {
			commonWhereSql.append(" AND ").append(super.getLogicDeleteCondition(meta.logicDeleteFieldOpt.get()));
		}
		if (meta.tenantFieldOpt.isPresent()) {
			commonWhereSql.append(" AND ").append(super.getTenantCondition(meta.tenantFieldOpt.get()));
		}
		return "(" + whereSql + ")" + commonWhereSql;
	}
	
	/**
	 * 判断字段是否需要更新
	 * @param entity
	 * @param fieldName
	 * @param value
	 * @return
	 */
	private boolean shouldUpdateField(Object entity, String fieldName, Object value, boolean updateNull) {
		if (entity instanceof UpdateEntity.Updatable) {
			Set<String> modifiedFieldNames = ((UpdateEntity.Updatable) entity).getModifiedFields().keySet();
			return modifiedFieldNames.contains(fieldName);
		}
		return updateNull || value != null;
	}
	
	/**
	 * 获取字段值
	 * @param entity
	 * @param fieldName
	 * @return
	 */
	private Object getFieldValue(Object entity, String fieldName) {
		try {
			Method getter = entity.getClass().getMethod("get" + SqlStringUtils.capitalize(fieldName));
			return getter.invoke(entity);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 构建 update SQL
	 * @param meta
	 * @param tableIdEntity
	 * @param entity
	 * @param tableEntity
	 * @param shouldUpdate
	 * @param isNullSetter    是否未赋值也更新为null
	 * @param whereClause     是否追加wrapper生成的where条件
	 * @param paramPrefix     参数前缀，单参情况为""，多参@Param为"entity."
	 * @return                SQL字符串
	 */
	private String buildUpdateSql(EntityMeta meta, TableIdEntity tableIdEntity, Object entity, TableEntity tableEntity,
		BiFunction<String, Object, Boolean> shouldUpdate, BiFunction<String, Object, Boolean> isNullSetter,
		String whereClause, String paramPrefix) {
		List<TableColumnEntity> tableColumnEntityList = tableEntity.getTableColumnEntityList();
		String versionColumnName = null;    // 乐观锁字段
		boolean useVersion = false;         // 是否启用乐观锁（有注解 + 有传值）
		
		if (meta.versionOpt.isPresent()) {
			Field versionField = meta.versionOpt.get();
			versionColumnName = SqlStringUtils.getTableOrColumnName(versionField, Version.class);
			
			// 判断是否“传值”
			try {
				Method getter = entity.getClass().getMethod("get" + SqlStringUtils.capitalize(versionField.getName()));
				Object versionValue = getter.invoke(entity);
				
				// 规则：非 null（如果是 String，再额外判断非空）
				useVersion = versionValue != null && (!(versionValue instanceof CharSequence) || ((CharSequence) versionValue).length() > 0);
			} catch (NoSuchMethodException e) {
				// 没 getter 的话，可以改用 field 反射取值
				try {
					versionField.setAccessible(true);
					Object versionValue = versionField.get(entity);
					useVersion = versionValue != null && (!(versionValue instanceof CharSequence) || ((CharSequence) versionValue).length() > 0);
				} catch (IllegalAccessException ex) {
					throw new RuntimeException(ex);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		StringBuilder sql = new StringBuilder();
		for (TableColumnEntity columnEntity : tableColumnEntityList) {
			String fieldName = columnEntity.getField();
			String columnName = columnEntity.getColumn();
			if (columnName.equals(tableIdEntity.getColumn())) continue;
			if (useVersion && versionColumnName != null && columnName.equals(versionColumnName)) continue;
			
			try {
				Method getter = entity.getClass().getMethod("get" + SqlStringUtils.capitalize(fieldName));
				Object value = getter.invoke(entity);
				
				if (shouldUpdate.apply(fieldName, value)) {
					if (isNullSetter.apply(fieldName, value)) {
						sql.append(columnName).append(" = NULL, ");
					} else {
						sql.append(columnName).append(" = #{").append(paramPrefix).append(fieldName).append("}, ");
					}
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		// 乐观锁：加上 version = version + 1
		if (useVersion && versionColumnName != null) {
			sql.append(versionColumnName).append(" = ").append(versionColumnName).append(" + 1, ");
		}
		
		// 删除最后逗号和空格
		int lastIndex = sql.lastIndexOf(",");
		if (lastIndex != -1) {
			sql.delete(lastIndex, lastIndex + 2);
		}
		
		final boolean useVersionFinal = useVersion;
		return new SQL() {{
			UPDATE(meta.tableName);
			SET(sql.toString());
			if (SqlStringUtils.isNotEmpty(whereClause)) {
				WHERE(whereClause);
			} else {
				WHERE(tableIdEntity.getColumn() + " = #{" + paramPrefix + tableIdEntity.getField() + "}");
			}
			appendCommonWhere(this, meta.logicDeleteFieldOpt, meta.tenantFieldOpt, useVersionFinal ? meta.versionOpt : Optional.empty(), paramPrefix);
		}}.toString();
	}
	
	/**
	 * 公共where条件拼接（逻辑删除，租户，乐观锁）
	 * @param sql
	 * @param logicDeleteFieldOpt
	 * @param tenantFieldOpt
	 * @param versionOpt
	 * @param paramPrefix    参数前缀，单参情况为""，多参@Param为"entity."
	 */
	private void appendCommonWhere(SQL sql, Optional<Field> logicDeleteFieldOpt, Optional<Field> tenantFieldOpt, Optional<Field> versionOpt, String paramPrefix) {
		if (logicDeleteFieldOpt.isPresent()) {
			sql.WHERE(super.getLogicDeleteCondition(logicDeleteFieldOpt.get()));
		}
		if (tenantFieldOpt.isPresent()) {
			sql.WHERE(super.getTenantCondition(tenantFieldOpt.get()));
		}
		if (versionOpt.isPresent()) {
			sql.WHERE(super.getVersionCondition(versionOpt.get(), paramPrefix));
		}
	}
	
	/**
	 * 批量更新字段信息
	 */
	private static class BatchUpdateItem {
		
		private int index;
		private boolean nullSetter;
		
		public BatchUpdateItem(int index, boolean nullSetter) {
			this.index = index;
			this.nullSetter = nullSetter;
		}
		
		public int getIndex() {
			return index;
		}
		
		public boolean isNullSetter() {
			return nullSetter;
		}
		
	}
	
}
