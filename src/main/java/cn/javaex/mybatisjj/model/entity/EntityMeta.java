package cn.javaex.mybatisjj.model.entity;

import java.lang.reflect.Field;
import java.util.Optional;

/**
 * 抽取元数据封装类
 * 
 * @author 陈霓清
 * @Date 2026年2月9日
 */
public class EntityMeta {
	public final String tableName;
	public final String tableId;
	public final Optional<Field> logicDeleteFieldOpt;
	public final Optional<Field> tenantFieldOpt;
	public final Optional<Field> versionOpt;

	public EntityMeta(String tableName, String tableId, Optional<Field> logicDeleteFieldOpt,
			Optional<Field> tenantFieldOpt, Optional<Field> versionOpt) {
		this.tableName = tableName;
		this.tableId = tableId;
		this.logicDeleteFieldOpt = logicDeleteFieldOpt;
		this.tenantFieldOpt = tenantFieldOpt;
		this.versionOpt = versionOpt;
	}
}
