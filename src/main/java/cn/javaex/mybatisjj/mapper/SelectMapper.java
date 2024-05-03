package cn.javaex.mybatisjj.mapper;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import cn.javaex.mybatisjj.provider.SqlSelectProvider;

/**
 * 查询Mapper
 * 
 * @author 陈霓清
 * @param <T>
 */
public interface SelectMapper<T> {

	/**
	 * 根据主键查询实体信息
	 * @param id
	 * @return
	 */
	@SelectProvider(type = SqlSelectProvider.class, method = "selectById")
	T selectById(Serializable id);
	
	/**
	 * 根据主键集合批量查询
	 * @param ids
	 * @return
	 */
	@SelectProvider(type = SqlSelectProvider.class, method = "selectListByIds")
	List<T> selectListByIds(@Param("ids") Collection<? extends Serializable> ids);

	/**
	 * 根据指定字段批量查询
	 * @param column
	 * @param columnValue
	 * @return
	 */
	@SelectProvider(type = SqlSelectProvider.class, method = "selectListByColumn")
	List<T> selectListByColumn(@Param("column") String column, @Param("columnValue") Object columnValue);

	/**
	 * 根据指定字段查询统计数量
	 * @param column
	 * @param columnValue
	 * @return
	 */
	@SelectProvider(type = SqlSelectProvider.class, method = "selectCountByColumn")
	int selectCountByColumn(@Param("column") String column, @Param("columnValue") Object columnValue);
	
}
