package cn.javaex.mybatisjj.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.UpdateProvider;

import cn.javaex.mybatisjj.model.query.Wrapper;
import cn.javaex.mybatisjj.provider.SqlUpdateProvider;

/**
 * 更新Mapper
 * 
 * @author 陈霓清
 * @param <T>
 */
public interface UpdateMapper<T> {

	/**
	 * 根据主键更新实体信息
	 * @param entity
	 * @return
	 */
	@UpdateProvider(type = SqlUpdateProvider.class, method = "updateById")
	int updateById(T entity);

	/**
	 * 根据主键批量更新实体信息
	 * @param list
	 * @return
	 */
	@UpdateProvider(type = SqlUpdateProvider.class, method = "updateBatch")
	int updateBatch(@Param("list") List<T> list);
	
	/**
	 * 根据主键批量更新实体信息（未设置值的，更新为NULL）
	 * @param list
	 * @return
	 */
	@UpdateProvider(type = SqlUpdateProvider.class, method = "updateBatchWithNull")
	int updateBatchWithNull(@Param("list") List<T> list);
	
	/**
	 * 根据主键更新实体信息（未设置值的，更新为NULL）
	 * @param entity
	 * @return
	 */
	@UpdateProvider(type = SqlUpdateProvider.class, method = "updateByIdWithNull")
	int updateByIdWithNull(T entity);
	
	/**
	 * 根据查询条件更新数据
	 * @param entity
	 * @param wrapper
	 * @return
	 */
	@UpdateProvider(type = SqlUpdateProvider.class, method = "updateByCondition")
	int updateByCondition(@Param("entity") T entity, @Param("wrapper") Wrapper<T> wrapper);
	
	/**
	 * 根据查询条件更新数据（未设置值的，更新为NULL）
	 * @param entity
	 * @param wrapper
	 * @return
	 */
	@UpdateProvider(type = SqlUpdateProvider.class, method = "updateByConditionWithNull")
	int updateByConditionWithNull(@Param("entity") T entity, @Param("wrapper") Wrapper<T> wrapper);
	
}
