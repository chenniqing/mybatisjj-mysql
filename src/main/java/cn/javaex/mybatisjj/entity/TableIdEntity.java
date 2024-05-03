package cn.javaex.mybatisjj.entity;

/**
 * 表主键实体
 * 
 * @author 陈霓清
 */
public class TableIdEntity {
	private String column;	// 数据库字段名
	private String field;	// 实体属性
	private String idType;	// 主键生成策略
	
	public String getColumn() {
		return column;
	}
	public void setColumn(String column) {
		this.column = column;
	}
	public String getField() {
		return field;
	}
	public void setField(String field) {
		this.field = field;
	}
	public String getIdType() {
		return idType;
	}
	public void setIdType(String idType) {
		this.idType = idType;
	}
	
}