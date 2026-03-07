package cn.javaex.mybatisjj.model.function;

import java.io.Serializable;
import java.util.function.Function;

/**
 * 为了能把lambda getter（比如User::getName）传进来，且能被反序列化提取目标方法名
 * 
 * @author 陈霓清
 * @Date 2026年2月6日
 * @param <T>
 * @param <R>
 */
@FunctionalInterface
public interface SFunction<T, R> extends Function<T, R>, Serializable {
	
}
