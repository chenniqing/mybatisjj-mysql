package cn.javaex.mybatisjj.mapper;

import java.io.Serializable;
import java.util.Collection;

import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.Param;

import cn.javaex.mybatisjj.provider.SqlDeleteProvider;

/**
 * 删除Mapper
 * 
 * @author 陈霓清
 * @param <T>
 */
public interface DeleteMapper<T> {

	/**
	 * 根据主键删除数据
	 * @param id
	 * @return
	 */
	@DeleteProvider(type = SqlDeleteProvider.class, method = "deleteById")
	int deleteById(Serializable id);

	/**
	 * 根据主键集合批量删除数据
	 * @param ids
	 * @return
	 */
	@DeleteProvider(type = SqlDeleteProvider.class, method = "deleteByIds")
	int deleteByIds(@Param("ids") Collection<? extends Serializable> ids);

	/**
	 * 根据某个字段删除数据
	 * @param column
	 * @param columnValue
	 * @return
	 */
	@DeleteProvider(type = SqlDeleteProvider.class, method = "deleteByColumn")
	int deleteByColumn(@Param("column") String column, @Param("columnValue") Object columnValue);
	
}
