package de.karlNet.genericForm;

public class ValidationResult {
	@Override
	public String toString() {
		return "ValidationResult [is_valid=" + is_valid + ", table_name="
				+ table_name + ", column_name=" + column_name + "]";
	}
	private int is_valid;
	private String table_name;
	private String column_name;
	public int getIs_valid() {
		return is_valid;
	}
	public void setIs_valid(int is_valid) {
		this.is_valid = is_valid;
	}
	public String getTable_name() {
		return table_name;
	}
	public void setTable_name(String table_name) {
		this.table_name = table_name;
	}
	public String getColumn_name() {
		return column_name;
	}
	public void setColumn_name(String column_name) {
		this.column_name = column_name;
	}
}
