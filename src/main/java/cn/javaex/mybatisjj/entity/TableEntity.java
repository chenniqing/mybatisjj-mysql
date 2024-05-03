package cn.javaex.mybatisjj.entity;

import java.util.List;

/**
 * 表实体
 * 
 * @author 陈霓清
 */
public class TableEntity {
	private String tableName;								// 表名
	private TableIdEntity tableIdEntity;					// 表主键实体
	private List<TableColumnEntity> tableColumnEntityList;	// 表列实体集合
	
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public TableIdEntity getTableIdEntity() {
		return tableIdEntity;
	}
	public void setTableIdEntity(TableIdEntity tableIdEntity) {
		this.tableIdEntity = tableIdEntity;
	}
	public List<TableColumnEntity> getTableColumnEntityList() {
		return tableColumnEntityList;
	}
	public void setTableColumnEntityList(List<TableColumnEntity> tableColumnEntityList) {
		this.tableColumnEntityList = tableColumnEntityList;
	}
	
}
