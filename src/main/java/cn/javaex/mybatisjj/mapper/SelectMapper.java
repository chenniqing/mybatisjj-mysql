package cn.javaex.mybatisjj.mapper;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import cn.javaex.mybatisjj.model.query.Wrapper;
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
	 * 根据Wrapper条件查询列表
	 * @param wrapper Wrapper条件
	 * @return 实体列表
	 */
	@SelectProvider(type = SqlSelectProvider.class, method = "selectListByWrapper")
	List<T> selectList(@Param("wrapper") Wrapper<T> wrapper);
	
	/**
	 * 根据Wrapper条件查询单个
	 * @param wrapper Wrapper条件
	 * @return 单个实体
	 */
	@SelectProvider(type = SqlSelectProvider.class, method = "selectOneByWrapper")
	T selectOne(@Param("wrapper") Wrapper<T> wrapper);
	
	/**
	 * 根据Wrapper条件查询总数
	 * @param wrapper Wrapper条件
	 * @return 符合条件的数量
	 */
	@SelectProvider(type = SqlSelectProvider.class, method = "selectCountByWrapper")
	long selectCount(@Param("wrapper") Wrapper<T> wrapper);
	
	/**
	 * 查询指定字段的最大值
	 * @param fieldName
	 * @param wrapper
	 * @return
	 */
	@SelectProvider(type = SqlSelectProvider.class, method = "selectMaxByWrapper")
	BigDecimal selectMax(@Param("fieldName") String fieldName, @Param("wrapper") Wrapper<T> wrapper);
	
	/**
	 * 查询指定字段的最小值
	 * @param fieldName
	 * @param wrapper
	 * @return
	 */
	@SelectProvider(type = SqlSelectProvider.class, method = "selectMinByWrapper")
	BigDecimal selectMin(@Param("fieldName") String fieldName, @Param("wrapper") Wrapper<T> wrapper);
	
	/**
	 * 查询指定字段的平均值
	 * @param fieldName
	 * @param wrapper
	 * @return
	 */
	@SelectProvider(type = SqlSelectProvider.class, method = "selectAvgByWrapper")
	BigDecimal selectAvg(@Param("fieldName") String fieldName, @Param("wrapper") Wrapper<T> wrapper);
	
	/**
	 * 查询指定字段的总和
	 * @param fieldName
	 * @param wrapper
	 * @return
	 */
	@SelectProvider(type = SqlSelectProvider.class, method = "selectSumByWrapper")
	BigDecimal selectSum(@Param("fieldName") String fieldName, @Param("wrapper") Wrapper<T> wrapper);
	
}
