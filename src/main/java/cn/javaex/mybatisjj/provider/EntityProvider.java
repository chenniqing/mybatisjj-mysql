package cn.javaex.mybatisjj.provider;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import cn.javaex.mybatisjj.basic.annotation.ExcludeTableColumn;
import cn.javaex.mybatisjj.basic.annotation.TableColumn;
import cn.javaex.mybatisjj.basic.annotation.TableId;
import cn.javaex.mybatisjj.basic.annotation.TableName;
import cn.javaex.mybatisjj.basic.common.IdTypeConstant;
import cn.javaex.mybatisjj.entity.TableColumnEntity;
import cn.javaex.mybatisjj.entity.TableEntity;
import cn.javaex.mybatisjj.entity.TableIdEntity;
import cn.javaex.mybatisjj.util.SqlIdUtils;
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
		Optional<TableIdEntity> tableIdEntityOpt = Arrays.stream(entityType.getDeclaredFields())
			    .filter(field -> field.isAnnotationPresent(TableId.class) || "id".equals(field.getName()))    // 过滤具有TableId注解的字段或名称等于“id”的字段。
			    .findFirst()
			    .map(field -> {
			        field.setAccessible(true);    // 设置类的私有属性可访问
			        
			        TableIdEntity newTableIdEntity = new TableIdEntity();
			        if (field.isAnnotationPresent(TableId.class)) {
			            TableId tableIdAnnotation = field.getAnnotation(TableId.class);
			            newTableIdEntity.setColumn(SqlStringUtils.isEmpty(tableIdAnnotation.value()) ? SqlStringUtils.toUnderlineName(field.getName()) : tableIdAnnotation.value());
			        } else {
			            newTableIdEntity.setColumn("id");    // 默认主键字段为“id”
			        }
			        newTableIdEntity.setField(field.getName());
			        
			        field.setAccessible(false);
			        return newTableIdEntity;
			    });
		
		return tableIdEntityOpt.orElse(null);
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
		
		// 获取当前实体类的所有属性
		Field[] declaredFields = entityType.getDeclaredFields();
		
		// 第一次遍历，判断是否根据@TableColumn注解获取表字段
		boolean hasTableColumnAnnotation = Arrays.stream(declaredFields)
			    .anyMatch(field -> field.isAnnotationPresent(TableColumn.class));
		
		// 根据@TableColumn注解获取表字段
		if (hasTableColumnAnnotation) {
			TableIdEntity tableIdEntity = this.getTableIdEntity(entityType);
			if (tableIdEntity != null) {
				TableColumnEntity tableColumnEntity = new TableColumnEntity();
				tableColumnEntity.setColumn(tableIdEntity.getColumn());
				tableColumnEntity.setField(tableIdEntity.getField());
				list.add(tableColumnEntity);
			}
			
			for (Field field : declaredFields) {
				if (field.isAnnotationPresent(TableColumn.class)) {
					field.setAccessible(true);
					
					TableColumnEntity tableColumnEntity2 = new TableColumnEntity();
					tableColumnEntity2.setColumn(SqlStringUtils.toUnderlineName(field.getName()));
					tableColumnEntity2.setField(field.getName());
					list.add(tableColumnEntity2);
					
					field.setAccessible(false);
				}
			}
		}
		// 获取实体的所有属性作为表字段，排除@ExcludeTableColumn注解的属性
		else {
			for (Field field : declaredFields) {
				if (field.isAnnotationPresent(ExcludeTableColumn.class) == false) {
					field.setAccessible(true);
					
					TableColumnEntity tableColumnEntity = new TableColumnEntity();
					tableColumnEntity.setColumn(SqlStringUtils.toUnderlineName(field.getName()));
					tableColumnEntity.setField(field.getName());
					list.add(tableColumnEntity);
					
					field.setAccessible(false);
				}
			}
		}
		
		if (list==null || list.size()==0) {
			new RuntimeException("No field marked with @TableColumn or field found.");
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
		// 遍历所有字段以找到具有@TableId注解的字段。
		for (Field field : clazz.getDeclaredFields()) {
			field.setAccessible(true);

			// 记录当前传参中主键ID的值
			if (tableIdEntity.getField().equals(field.getName())) {
				primaryKeyValue = field.get(parameter);
			}

			// 根据注解设置主键ID的值
			TableId tableIdAnnotation = field.getAnnotation(TableId.class);
			if (tableIdAnnotation != null) {
				// 如果主键ID有值的话，则不做处理
				if (primaryKeyValue != null) {
					// 防止业务代码中设定主键ID的值时，无法通用entity.getId()获取刚插入的主键ID值的问题
					// 主键自增
					if (IdTypeConstant.AUTO.equals(tableIdAnnotation.type())) {
						tableIdEntity.setIdType(IdTypeConstant.LONG_ID);
					}
					// 32 位 UUID 字符串，不带-
					else if (IdTypeConstant.UUID.equals(tableIdAnnotation.type())) {
						tableIdEntity.setIdType(IdTypeConstant.UUID);
					}
					// 长数字ID
					else if (IdTypeConstant.LONG_ID.equals(tableIdAnnotation.type())) {
						tableIdEntity.setIdType(IdTypeConstant.LONG_ID);
					}
					// 长数字ID 字符串
					else if (IdTypeConstant.LONG_ID_STR.equals(tableIdAnnotation.type())) {
						tableIdEntity.setIdType(IdTypeConstant.LONG_ID_STR);
					}
					
					field.setAccessible(false);
					break;
				}
				
				// 主键自增
				if (IdTypeConstant.AUTO.equals(tableIdAnnotation.type())) {
					// 不做处理
				}
				// 32 位 UUID 字符串，不带-
				else if (IdTypeConstant.UUID.equals(tableIdAnnotation.type())) {
					primaryKeyValue = SqlIdUtils.getUUID();
					tableIdEntity.setIdType(IdTypeConstant.UUID);
				}
				// 长数字ID
				else if (IdTypeConstant.LONG_ID.equals(tableIdAnnotation.type())) {
					primaryKeyValue = SqlIdUtils.getLongId();
					tableIdEntity.setIdType(IdTypeConstant.LONG_ID);
				}
				// 长数字ID 字符串
				else if (IdTypeConstant.LONG_ID_STR.equals(tableIdAnnotation.type())) {
					primaryKeyValue = SqlIdUtils.getLongIdStr();
					tableIdEntity.setIdType(IdTypeConstant.LONG_ID_STR);
				}

				// 设置主键ID的值
				field.set(parameter, primaryKeyValue);

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
