package cn.javaex.mybatisjj.mapper;

import java.util.List;

import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Param;

import cn.javaex.mybatisjj.provider.SqlInsertProvider;

/**
 * 插入Mapper
 * 
 * @author 陈霓清
 * @param <T>
 */
public interface InsertMapper<T> {

	/**
	 * 插入实体信息
	 * @param entity
	 * @return
	 */
	@InsertProvider(type = SqlInsertProvider.class, method = "insert")
	int insert(T entity);

	/**
	 * 批量插入实体信息
	 * @param list
	 * @return
	 */
	@InsertProvider(type = SqlInsertProvider.class, method = "batchInsert")
	int batchInsert(@Param("list") List<?> list);
	
}
