package cn.javaex.mybatisjj.mapper;

import java.io.Serializable;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.UpdateProvider;

import cn.javaex.mybatisjj.provider.SqlUpdateProvider;

/**
 * 更新Mapper
 * 
 * @author 陈霓清
 * @param <T>
 */
public interface UpdateMapper<T> {

	/**
	 * 根据主键更新实体信息（为null的字段不更新）
	 * @param entity
	 * @return
	 */
	@UpdateProvider(type = SqlUpdateProvider.class, method = "updateById")
	int updateById(T entity);

	/**
	 * 根据主键，将指定字段的值更新为null值
	 * @param column
	 * @param id
	 * @return
	 */
	@UpdateProvider(type = SqlUpdateProvider.class, method = "updateNullColumnById")
	int updateNullColumnById(@Param("column") String column, @Param("id") Serializable id);
	
}
