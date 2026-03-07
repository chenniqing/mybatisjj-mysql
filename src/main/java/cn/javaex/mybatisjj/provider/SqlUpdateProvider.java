package cn.javaex.mybatisjj.provider;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
	
}
