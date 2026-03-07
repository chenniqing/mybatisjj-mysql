package cn.javaex.mybatisjj.provider;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.ibatis.builder.annotation.ProviderContext;

import cn.javaex.mybatisjj.basic.annotation.ExcludeTableColumn;
import cn.javaex.mybatisjj.basic.annotation.TableColumn;
import cn.javaex.mybatisjj.basic.annotation.TableId;
import cn.javaex.mybatisjj.basic.annotation.TableLogic;
import cn.javaex.mybatisjj.basic.annotation.TableName;
import cn.javaex.mybatisjj.basic.annotation.TenantId;
import cn.javaex.mybatisjj.basic.annotation.Version;
import cn.javaex.mybatisjj.basic.common.IdTypeConstant;
import cn.javaex.mybatisjj.config.customize.IdGeneratorRegistry;
import cn.javaex.mybatisjj.config.customize.TenantContext;
import cn.javaex.mybatisjj.config.interceptor.IdGeneratorInterceptor;
import cn.javaex.mybatisjj.model.entity.EntityMeta;
import cn.javaex.mybatisjj.model.entity.TableColumnEntity;
import cn.javaex.mybatisjj.model.entity.TableEntity;
import cn.javaex.mybatisjj.model.entity.TableIdEntity;
import cn.javaex.mybatisjj.util.ReflectiveUtils;
import cn.javaex.mybatisjj.util.SqlStringUtils;

/**
 * Entity
 * 
 * @author 陈霓清
 */
public class EntityProvider {
	
	/**
	 * 用于缓存Mapper接口类型到实体类的映射
	 */
	private static final ConcurrentHashMap<Class<?>, Class<?>> ENTITY_TYPE_CACHE = new ConcurrentHashMap<>();
	
	/**
	 * 获取逻辑删除的查询条件
	 * @param field
	 * @return
	 */
	protected String getLogicDeleteCondition(Field field) {
		TableLogic annotation = field.getAnnotation(TableLogic.class);
		
		String logicColumnName = SqlStringUtils.isEmpty(annotation.value())
				? SqlStringUtils.toUnderlineName(field.getName())
				: annotation.value();
		String notDeletedValue = annotation.notDeletedValue();
		
		return logicColumnName + " = '" + notDeletedValue + "'";
	}
	
	/**
	 * 获取租户的查询条件
	 * @param field
	 * @return
	 */
	protected String getTenantCondition(Field field) {
		String tenantColumnName = SqlStringUtils.getTableOrColumnName(field, TenantId.class);
		
		return tenantColumnName + " = '" + TenantContext.getTenantId() + "'";
	}
	
	/**
	 * 获取乐观锁版本的查询条件
	 * @param field
	 * @param paramPrefix
	 * @return
	 */
	protected String getVersionCondition(Field field, String paramPrefix) {
		String versionColumnName = SqlStringUtils.getTableOrColumnName(field, Version.class);
		
		return versionColumnName + " = #{" + paramPrefix + field.getName() + "}";
	}
	
	/**
	 * 抽取元数据
	 * @param providerContext
	 * @return
	 */
	protected EntityMeta extractEntityMeta(ProviderContext providerContext) {
		Class<?> entityType = this.getEntityType(providerContext.getMapperType());
		String tableName = this.getTableName(entityType);    // 获取表名
		String tableId = this.getTableId(entityType);        // 获取主键字段名
		
		List<Field> allFields = ReflectiveUtils.getAllFields(entityType);
		// 逻辑删除
		Optional<Field> logicDeleteFieldOpt = allFields.stream()
				.filter(field -> field.isAnnotationPresent(TableLogic.class))
				.findFirst();
		// 租户处理
		Optional<Field> tenantFieldOpt = allFields.stream()
				.filter(field -> field.isAnnotationPresent(TenantId.class))
				.findFirst();
		// 乐观锁处理
		Optional<Field> versionOpt = allFields.stream()
				.filter(field -> field.isAnnotationPresent(Version.class))
				.findFirst();
		return new EntityMeta(tableName, tableId, logicDeleteFieldOpt, tenantFieldOpt, versionOpt);
	}
	
	/**
	 * 将ids列表转换为参数化字符串
	 * @param ids
	 * @return
	 */
	protected String parameterize(Collection<? extends Serializable> ids) {
		if (ids == null || ids.isEmpty()) {
			throw new IllegalArgumentException("The 'ids' parameter cannot be null or empty.");
		}
		
		return IntStream.range(0, ids.size())
		        .mapToObj(index -> "#{ids[" + index + "]}")
		        .collect(Collectors.joining(", "));
	}
	
	/**
	 * 获取数据库表名
	 * @param entityType
	 * @return
	 */
	protected String getTableName(Class<?> entityType) {
		return Optional.ofNullable(entityType.getAnnotation(TableName.class))
		                           .map(TableName::value)
		                           .orElseGet(() -> SqlStringUtils.toUnderlineName(entityType.getSimpleName()));
	}
	
	/**
	 * 获取表主键实体
	 * @param entityType
	 * @return
	 */
	protected TableIdEntity getTableIdEntity(Class<?> entityType) {
		// 1.获取本类及其所有父类的所有字段
		List<Field> allFields = ReflectiveUtils.getAllFields(entityType);
		
		// 2.流式查找
		return allFields.stream()
				.filter(field -> field.isAnnotationPresent(TableId.class) || "id".equals(field.getName()))	// 过滤具有TableId注解的字段或名称等于“id”的字段。
				.findFirst()
				.map(field -> {
					TableIdEntity newTableIdEntity = new TableIdEntity();
					if (field.isAnnotationPresent(TableId.class)) {
						newTableIdEntity.setColumn(SqlStringUtils.getTableOrColumnName(field, TableId.class));
					} else {
						newTableIdEntity.setColumn("id");	// 默认主键字段为“id”
					}
					newTableIdEntity.setField(field.getName());
					return newTableIdEntity;
				})
				.orElse(null);
	}
	
	/**
	 * 获取主键字段名
	 * @param entityType
	 * @return
	 */
	protected String getTableId(Class<?> entityType) {
		return Optional.ofNullable(this.getTableIdEntity(entityType))
		    .map(TableIdEntity::getColumn)
		    .orElseThrow(() -> new RuntimeException("No field marked with @TableId or field named 'id' found."));
	}
	
	/**
	 * 获取表字段实体
	 * @param entityType
	 * @return
	 */
	protected List<TableColumnEntity> getTableColumnEntitys(Class<?> entityType) {
		List<TableColumnEntity> list = new ArrayList<TableColumnEntity>();
		
		// 获取当前实体类及其所有父类的属性
		List<Field> declaredFields = ReflectiveUtils.getAllFields(entityType);
		
		// 第一次遍历，判断是否根据@TableColumn注解获取表字段
		boolean hasTableColumnAnnotation = declaredFields.stream()
		        .anyMatch(field -> field.isAnnotationPresent(TableColumn.class));
		
		// 根据@TableColumn注解获取表字段
		if (hasTableColumnAnnotation) {
			TableIdEntity tableIdEntity = this.getTableIdEntity(entityType);
			if (tableIdEntity != null) {
				TableColumnEntity tableIdColumnEntity = new TableColumnEntity();
				tableIdColumnEntity.setColumn(tableIdEntity.getColumn());
				tableIdColumnEntity.setField(tableIdEntity.getField());
				list.add(tableIdColumnEntity);
			}
			
			for (Field field : declaredFields) {
				if (field.isAnnotationPresent(TableColumn.class)) {
					TableColumnEntity tableColumnEntity = new TableColumnEntity();
					tableColumnEntity.setColumn(SqlStringUtils.getTableOrColumnName(field, TableColumn.class));
					tableColumnEntity.setField(field.getName());
					list.add(tableColumnEntity);
				}
			}
		}
		// 获取实体的所有属性作为表字段，排除@ExcludeTableColumn注解的属性
		else {
			for (Field field : declaredFields) {
				if (field.isAnnotationPresent(ExcludeTableColumn.class) == false) {
					TableColumnEntity tableColumnEntity = new TableColumnEntity();
					tableColumnEntity.setColumn(SqlStringUtils.toUnderlineName(field.getName()));
					tableColumnEntity.setField(field.getName());
					list.add(tableColumnEntity);
				}
			}
		}
		
		if (list==null || list.size()==0) {
			throw new RuntimeException("No field marked with @TableColumn or field found.");
		}
		
		return list;
	}
	
	/**
	 * 获取数据库表实体
	 * @param entityType
	 * @return
	 */
	protected TableEntity getTableEntity(Class<?> entityType) {
		String tableName = this.getTableName(entityType);
		TableIdEntity tableIdEntity = this.getTableIdEntity(entityType);
		List<TableColumnEntity> tableColumnEntityList = this.getTableColumnEntitys(entityType);
		
		TableEntity tableEntity = new TableEntity();
		tableEntity.setTableName(tableName);
		tableEntity.setTableIdEntity(tableIdEntity);
		tableEntity.setTableColumnEntityList(tableColumnEntityList);
		
		return tableEntity;
	}
	
	/**
	 * 获取EntityType
	 * @param mapperType
	 * @return
	 */
	protected Class<?> getEntityType(Class<?> mapperType) {
		return ENTITY_TYPE_CACHE.computeIfAbsent(mapperType, mt -> resolveEntityType(mt));
	}
	
	/**
	 * 获取EntityType
	 * @param mapperType
	 * @return
	 */
	private Class<?> resolveEntityType(Class<?> mapperType) {
		Type[] genericInterfaces = mapperType.getGenericInterfaces();
		
		for (Type genericInterface : genericInterfaces) {
			if (genericInterface instanceof ParameterizedType) {
				ParameterizedType parameterizedType = (ParameterizedType) genericInterface;
				Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
				if (actualTypeArguments.length > 0) {
					return (Class<?>) actualTypeArguments[0];
				}
			}
		}
		
		throw new IllegalStateException("Cannot resolve entity type for mapper " + mapperType.getName());
	}

	/**
	 * 自动生成主键
	 * @param parameter
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	public Object autoGeneratedKey(Object parameter) throws IllegalArgumentException, IllegalAccessException {
		if (parameter instanceof Map) {
			Object value = ((Map<?, ?>) parameter).get("list");
			List<?> list = (List<?>) value;
			
			return this.autoGeneratedKeyForList(list);
		} else if (parameter != null) {
			// 如果参数本身就是一个实体对象
			return this.autoGeneratedKeyForEntity(parameter);
		}
		
		return null;
	}

	/**
	 * 自动生成主键（单条数据插入时）
	 * @param parameter
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	private TableIdEntity autoGeneratedKeyForEntity(Object parameter) throws IllegalArgumentException, IllegalAccessException {
		Class<?> clazz = parameter.getClass();
		
		TableIdEntity tableIdEntity = this.getTableIdEntity(clazz);
		if (tableIdEntity == null) {
			return null;
		} else {
			tableIdEntity.setIdType(IdTypeConstant.AUTO);
		}
		
		Object primaryKeyValue = null;
		List<Field> allFields = ReflectiveUtils.getAllFields(clazz);
		// 遍历所有字段以找到具有@TableId注解的字段。
		for (Field field : allFields) {
			field.setAccessible(true);
			
			// 记录当前传参中主键ID的值
			if (tableIdEntity.getField().equals(field.getName())) {
				primaryKeyValue = field.get(parameter);
			}
			
			// 根据注解设置主键ID的值
			TableId tableIdAnnotation = field.getAnnotation(TableId.class);
			if (tableIdAnnotation != null) {
				// 如果主键ID有值的话，则不做处理
				String idType = tableIdAnnotation.type();
				
				// 如果主键已经有值，不生成新值。只更新tableIdEntity内的idType描述
				if (primaryKeyValue != null) {
					tableIdEntity.setIdType(idType);
					
					field.setAccessible(false);
					break;
				}
				
				// 主键自增，不需要赋值
				if (IdTypeConstant.AUTO.equals(idType)) {
					// 不做处理
				} else {
					// 尝试查找注册中心
					IdGeneratorInterceptor generator = IdGeneratorRegistry.get(idType);
					if (generator != null) {
						primaryKeyValue = generator.generate(field, parameter);
						tableIdEntity.setIdType(idType);
						field.set(parameter, primaryKeyValue);
					} else {
						// 找不到生成器：可以抛出异常，或提示开发者必须注册
						throw new IllegalArgumentException("Unsupported or unregistered idType: " + idType);
					}
				}
				
				field.setAccessible(false);
				break;
			}
		}
		
		return tableIdEntity;
	}

	/**
	 * 自动生成主键（多条数据插入时）
	 * @param list
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	private List<TableIdEntity> autoGeneratedKeyForList(List<?> list) throws IllegalArgumentException, IllegalAccessException {
		List<TableIdEntity> tableIdEntityList = new ArrayList<>();
		
		for (Object parameter : list) {
			TableIdEntity tableIdEntity = this.autoGeneratedKeyForEntity(parameter);
			if (tableIdEntity != null) {
				tableIdEntityList.add(tableIdEntity);
			}
		}
		
		return tableIdEntityList;
	}

}
