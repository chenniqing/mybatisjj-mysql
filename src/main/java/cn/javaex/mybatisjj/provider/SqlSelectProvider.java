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

import cn.javaex.mybatisjj.basic.annotation.ExcludeTableColumn;
import cn.javaex.mybatisjj.basic.annotation.TableColumn;
import cn.javaex.mybatisjj.basic.annotation.TableLogic;
import cn.javaex.mybatisjj.basic.annotation.TenantId;
import cn.javaex.mybatisjj.model.entity.EntityMeta;
import cn.javaex.mybatisjj.model.query.Wrapper;
import cn.javaex.mybatisjj.util.ReflectiveUtils;
import cn.javaex.mybatisjj.util.SqlStringUtils;

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
		EntityMeta meta = super.extractEntityMeta(providerContext);
		
		return new SQL() {{
			SELECT("*");
			FROM(meta.tableName);
			WHERE(meta.tableId + " = #{id}");
			appendCommonWhere(this, meta.logicDeleteFieldOpt, meta.tenantFieldOpt);
		}}.toString();
	}
	
	/**
	 * 根据主键集合批量查询
	 * @param providerContext
	 * @param ids
	 * @return
	 */
	public String selectListByIds(ProviderContext providerContext, @Param("ids") Collection<? extends Serializable> ids) {
		EntityMeta meta = super.extractEntityMeta(providerContext);
		
		return new SQL() {{
			SELECT("*");
			FROM(meta.tableName);
			WHERE(meta.tableId + " IN (" + parameterize(ids) + ")");
			appendCommonWhere(this, meta.logicDeleteFieldOpt, meta.tenantFieldOpt);
		}}.toString();
	}
	
	/**
	 * 根据Wrapper条件查询列表
	 * @param providerContext
	 * @param wrapper
	 * @return
	 */
	public String selectListByWrapper(ProviderContext providerContext, @Param("wrapper") Wrapper<?> wrapper) {
		Class<?> entityType = super.getEntityType(providerContext.getMapperType());
		String fromClause = this.buildSqlFromClause(entityType, wrapper);
		return "SELECT * " + fromClause;
	}
	
	/**
	 * 根据Wrapper条件查询单个
	 * @param providerContext
	 * @param wrapper
	 * @return
	 */
	public String selectOneByWrapper(ProviderContext providerContext, @Param("wrapper") Wrapper<?> wrapper) {
		return selectListByWrapper(providerContext, wrapper);
	}
	
	/**
	 * 根据Wrapper条件查询总数
	 * @param providerContext
	 * @param wrapper
	 * @return
	 */
	public String selectCountByWrapper(ProviderContext providerContext, @Param("wrapper") Wrapper<?> wrapper) {
		Class<?> entityType = super.getEntityType(providerContext.getMapperType());
		String fromClause = this.buildSqlFromClause(entityType, wrapper);
		return "SELECT COUNT(1) " + fromClause;
	}
	
	/**
	 * 查询指定字段的最大值
	 * @param providerContext
	 * @param fieldName
	 * @param wrapper
	 * @return
	 */
	public String selectMaxByWrapper(ProviderContext providerContext, @Param("fieldName") String fieldName, @Param("wrapper") Wrapper<?> wrapper) {
		Class<?> entityType = super.getEntityType(providerContext.getMapperType());
		// fieldName安全校验，防止SQL注入
		this.validateFieldName(entityType, fieldName);
		
		String fromClause = this.buildSqlFromClause(entityType, wrapper);
		return "SELECT MAX(" + fieldName + ") " + fromClause;
	}
	
	/**
	 * 查询指定字段的最小值
	 * @param providerContext
	 * @param wrapper
	 * @param fieldName
	 * @return
	 */
	public String selectMinByWrapper(ProviderContext providerContext, @Param("fieldName") String fieldName, @Param("wrapper") Wrapper<?> wrapper) {
		Class<?> entityType = super.getEntityType(providerContext.getMapperType());
		// fieldName安全校验，防止SQL注入
		this.validateFieldName(entityType, fieldName);
		
		String fromClause = this.buildSqlFromClause(entityType, wrapper);
		return "SELECT MIN(" + fieldName + ") " + fromClause;
	}
	 
	/**
	 * 查询指定字段的平均值
	 * @param providerContext
	 * @param wrapper
	 * @param fieldName
	 * @return
	 */
	public String selectAvgByWrapper(ProviderContext providerContext, @Param("fieldName") String fieldName, @Param("wrapper") Wrapper<?> wrapper) {
		Class<?> entityType = super.getEntityType(providerContext.getMapperType());
		// fieldName安全校验，防止SQL注入
		this.validateFieldName(entityType, fieldName);
		
		String fromClause = this.buildSqlFromClause(entityType, wrapper);
		return "SELECT AVG(" + fieldName + ") " + fromClause;
	}
	 
	/**
	 * 查询指定字段的总和
	 * @param providerContext
	 * @param wrapper
	 * @param fieldName
	 * @return
	 */
	public String selectSumByWrapper(ProviderContext providerContext, @Param("fieldName") String fieldName, @Param("wrapper") Wrapper<?> wrapper) {
		Class<?> entityType = super.getEntityType(providerContext.getMapperType());
		// fieldName安全校验，防止SQL注入
		this.validateFieldName(entityType, fieldName);
		
		String fromClause = this.buildSqlFromClause(entityType, wrapper);
		return "SELECT SUM(" + fieldName + ") " + fromClause;
	}
	
	/**
	 * fieldName安全校验，防止SQL注入
	 * @param entityType
	 * @param fieldName
	 */
	private void validateFieldName(Class<?> entityType, String fieldName) {
		List<Field> allFields = ReflectiveUtils.getAllFields(entityType);
		boolean exists = allFields.stream()
				.filter(ReflectiveUtils::isTableField)
				.filter(field -> !field.isAnnotationPresent(ExcludeTableColumn.class))
				.anyMatch(field -> {
		            TableColumn tableColumnAnnotation = field.getAnnotation(TableColumn.class);
		            String columnName = null;
		            if (tableColumnAnnotation != null && !SqlStringUtils.isEmpty(tableColumnAnnotation.value())) {
		                columnName = tableColumnAnnotation.value();
		            } else {
		                columnName = SqlStringUtils.toUnderlineName(field.getName());
		            }
		            return columnName.equals(fieldName);
		        });
		if (!exists) {
			throw new IllegalArgumentException("字段不存在: " + fieldName);
		}
	}

	/**
	 * 公共where条件拼接（逻辑删除，租户）
	 */
	private void appendCommonWhere(SQL sql, Optional<Field> logicDeleteFieldOpt, Optional<Field> tenantFieldOpt) {
		if (logicDeleteFieldOpt.isPresent()) {
			sql.WHERE(super.getLogicDeleteCondition(logicDeleteFieldOpt.get()));
		}
		if (tenantFieldOpt.isPresent()) {
			sql.WHERE(super.getTenantCondition(tenantFieldOpt.get()));
		}
	}
	
	/**
	 * 提取公共SQL片段组装
	 * @param entityType
	 * @param wrapper
	 * @param includeOrderByAndLast
	 * @param includeLogicDelete
	 * @return
	 */
	private String buildSqlFromClause(Class<?> entityType, Wrapper<?> wrapper) {
		String tableName = super.getTableName(entityType);
		
		String where = wrapper != null && wrapper.getWhereClause() != null ? wrapper.getWhereClause().trim() : "";
		String apply = wrapper != null && wrapper.getApplyClause() != null ? wrapper.getApplyClause().trim() : "";
		String orderBy = wrapper != null ? wrapper.getOrderByClause() : "";
		String groupBy = wrapper != null ? wrapper.getGroupByClause() : "";
		String having = wrapper != null ? wrapper.getHavingClause() : "";
		String last = wrapper != null ? wrapper.getLastClause() : "";
		
		// 获取所有属性
		List<Field> allFields = ReflectiveUtils.getAllFields(entityType);
		// 逻辑删除处理
		String logicDeleteCondition = "";
		Optional<Field> logicDeleteFieldOpt = allFields.stream()
				.filter(ReflectiveUtils::isTableField)
				.filter(field -> field.isAnnotationPresent(TableLogic.class))
				.findFirst();
		if (logicDeleteFieldOpt.isPresent()) {
			logicDeleteCondition = super.getLogicDeleteCondition(logicDeleteFieldOpt.get());
		}
		// 租户处理
		String tenantCondition = "";
		Optional<Field> tenantFieldOpt = allFields.stream()
				.filter(ReflectiveUtils::isTableField)
				.filter(field -> field.isAnnotationPresent(TenantId.class))
				.findFirst();
		if (tenantFieldOpt.isPresent()) {
			tenantCondition = super.getTenantCondition(tenantFieldOpt.get());
		}
		
		List<String> whereList = new ArrayList<>();
		if (!where.isEmpty()) whereList.add(where);
		if (!apply.isEmpty()) whereList.add(apply);
		if (!logicDeleteCondition.isEmpty()) whereList.add(logicDeleteCondition);
		if (!tenantCondition.isEmpty()) whereList.add(tenantCondition);
		
		String whereSql = whereList.isEmpty() ? "" : " WHERE " + String.join(" AND ", whereList);
		
		StringBuilder sb = new StringBuilder();
		sb.append(" FROM ").append(tableName);
		sb.append(whereSql);
		if (!groupBy.isEmpty()) sb.append(" ").append(groupBy);
		if (!having.isEmpty()) sb.append(" ").append(having);
		if (!orderBy.isEmpty()) sb.append(" ").append(orderBy);
		if (!last.isEmpty()) sb.append(" ").append(last);
		return sb.toString().trim();
	}

}
