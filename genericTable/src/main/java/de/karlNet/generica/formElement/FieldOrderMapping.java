package de.karlNet.generica.formElement;

public class FieldOrderMapping {
	@Override
	public String toString() {
		return "FieldOrderMapping [id=" + id + ", column_name=" + column_name
				+ ", table_name=" + table_name + "]";
	}
	private Integer id;
	private String column_name;
	private String table_name;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getColumn_name() {
		return column_name;
	}
	public void setColumn_name(String column_name) {
		this.column_name = column_name;
	}
	public String getTable_name() {
		return table_name;
	}
	public void setTable_name(String table_name) {
		this.table_name = table_name;
	}
}
