package cn.javaex.mybatisjj.entity;

/**
 * 表字段实体
 * 
 * @author 陈霓清
 */
public class TableColumnEntity {
	private String column;	// 数据库字段名
	private String field;	// 实体属性
	
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
}