package cn.javaex.mybatisjj.mapper;

/**
 * 通用Mapper
 * 
 * @author 陈霓清
 * @param <T>
 */
public interface CommonMapper<T> extends InsertMapper<T>, DeleteMapper<T>, UpdateMapper<T>, SelectMapper<T> {

}
