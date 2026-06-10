package cn.javaex.mybatisjj.provider;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.builder.annotation.ProviderMethodResolver;
import org.apache.ibatis.jdbc.SQL;

import cn.javaex.mybatisjj.basic.annotation.TableLogic;
import cn.javaex.mybatisjj.basic.annotation.TenantId;
import cn.javaex.mybatisjj.model.entity.EntityMeta;
import cn.javaex.mybatisjj.model.query.Wrapper;
import cn.javaex.mybatisjj.util.ReflectiveUtils;
import cn.javaex.mybatisjj.util.SqlStringUtils;

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
		EntityMeta meta = super.extractEntityMeta(providerContext);
		
		if (meta.logicDeleteFieldOpt.isPresent()) {
			Field logicField = meta.logicDeleteFieldOpt.get();
			TableLogic annotation = logicField.getAnnotation(TableLogic.class);
			
			String logicColumnName = SqlStringUtils.isEmpty(annotation.value())
					? SqlStringUtils.toUnderlineName(logicField.getName())
					: annotation.value();
			String deletedValue = SqlStringUtils.escapeSqlLiteral(annotation.deletedValue());
			
			return new SQL() {{
				UPDATE(meta.tableName);
				SET(logicColumnName + " = '" + deletedValue + "'");
				WHERE(meta.tableId + " = #{id}");
				meta.tenantFieldOpt.ifPresent(field -> {
					WHERE(getTenantCondition(field));
				});
			}}.toString();
		} else {
			return new SQL() {{
				DELETE_FROM(meta.tableName);
				WHERE(meta.tableId + " = #{id}");
				meta.tenantFieldOpt.ifPresent(field -> {
					WHERE(getTenantCondition(field));
				});
			}}.toString();
		}
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
		
		EntityMeta meta = extractEntityMeta(providerContext);
		StringBuilder sql = new StringBuilder();
		
		if (meta.logicDeleteFieldOpt.isPresent()) {
			Field logicField = meta.logicDeleteFieldOpt.get();
			TableLogic annotation = logicField.getAnnotation(TableLogic.class);
			
			String logicColumnName = SqlStringUtils.isEmpty(annotation.value())
					? SqlStringUtils.toUnderlineName(logicField.getName())
					: annotation.value();
			String deletedValue = SqlStringUtils.escapeSqlLiteral(annotation.deletedValue());
			
			sql.append("UPDATE ").append(meta.tableName)
			   .append(" SET ").append(logicColumnName).append(" = '").append(deletedValue).append("'")
			   .append(" WHERE ").append(meta.tableId).append(" IN (");
		} else {
			sql.append("DELETE FROM ").append(meta.tableName)
			   .append(" WHERE ").append(meta.tableId).append(" IN (");
		}
		
		for (int i = 0; i < ids.size(); i++) {
			if (i > 0) {
				sql.append(", ");
			}
			sql.append("#{ids[").append(i).append("]}");
		}
		sql.append(")");
		this.appendTenantCondition(meta.tenantFieldOpt, sql);
		
		return sql.toString();
	}
	
	/**
	 * 根据查询条件删除数据
	 * @param providerContext
	 * @param wrapper
	 * @return
	 */
	public String deleteByCondition(ProviderContext providerContext, @Param("wrapper") Wrapper<?> wrapper) {
		Class<?> entityType = super.getEntityType(providerContext.getMapperType());
		String tableName = super.getTableName(entityType);
		
		// 查找逻辑删除字段
		List<Field> allFields = ReflectiveUtils.getAllFields(entityType);
		Optional<Field> logicDeleteFieldOpt = allFields.stream()
				.filter(ReflectiveUtils::isTableField)
				.filter(field -> field.isAnnotationPresent(TableLogic.class))
				.findFirst();
		
		if (logicDeleteFieldOpt.isPresent()) {
			Field logicField = logicDeleteFieldOpt.get();
			TableLogic annotation = logicField.getAnnotation(TableLogic.class);
			
			String logicColumnName = SqlStringUtils.isEmpty(annotation.value())
					? SqlStringUtils.toUnderlineName(logicField.getName())
					: annotation.value();
			String deletedValue = SqlStringUtils.escapeSqlLiteral(annotation.deletedValue());
			
			String whereClause = this.buildWhereClause(entityType, wrapper, false, true);
			
			return "UPDATE " + tableName + " SET " + logicColumnName + " = '" + deletedValue + "'" + whereClause;
		} else {
			// 物理删除
			String whereClause = this.buildWhereClause(entityType, wrapper, false, true);
			return "DELETE FROM " + tableName + whereClause;
		}
	}
	
	/**
	 * 通用 WHERE 条件拼接
	 * @param entityType
	 * @param wrapper
	 * @param includeLogicDelete
	 * @param includeTenant
	 * @return
	 */
	private String buildWhereClause(Class<?> entityType, Wrapper<?> wrapper, boolean includeLogicDelete, boolean includeTenant) {
		List<Field> allFields = ReflectiveUtils.getAllFields(entityType);
		
		// Wrapper自身where条件
		String where = wrapper != null && wrapper.getWhereClause() != null ? wrapper.getWhereClause().trim() : "";
		
		// 逻辑删除条件
		String logicDeleteCondition = "";
		if (includeLogicDelete) {
			Optional<Field> logicDeleteFieldOpt = allFields.stream()
					.filter(ReflectiveUtils::isTableField)
					.filter(field -> field.isAnnotationPresent(TableLogic.class))
					.findFirst();
			if (logicDeleteFieldOpt.isPresent()) {
				logicDeleteCondition = super.getLogicDeleteCondition(logicDeleteFieldOpt.get());
			}
		}
		
		// 租户条件
		String tenantCondition = "";
		if (includeTenant) {
			Optional<Field> tenantFieldOpt = allFields.stream()
					.filter(ReflectiveUtils::isTableField)
					.filter(field -> field.isAnnotationPresent(TenantId.class))
					.findFirst();
			if (tenantFieldOpt.isPresent()) {
				tenantCondition = super.getTenantCondition(tenantFieldOpt.get());
			}
		}
		
		List<String> whereList = new ArrayList<>();
		if (!where.isEmpty()) whereList.add(where);
		if (!logicDeleteCondition.isEmpty()) whereList.add(logicDeleteCondition);
		if (!tenantCondition.isEmpty()) whereList.add(tenantCondition);
		
		return whereList.isEmpty() ? "" : " WHERE " + String.join(" AND ", whereList);
	}

	/**
	 * 抽取租户条件SQL拼接
	 * @param tenantFieldOpt
	 * @param sql
	 */
	private void appendTenantCondition(Optional<Field> tenantFieldOpt, StringBuilder sql) {
		tenantFieldOpt.ifPresent(field -> {
			sql.append(" AND ").append(super.getTenantCondition(field));
		});
	}

}
