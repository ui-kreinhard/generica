package de.karlNet.generica.genericForm;

public class check_constraints {

	private String table_name;
	private String column_name;
	private String check_clause;

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

	public String getCheck_clause() {
		return check_clause;
	}

	public void setCheck_clause(String check_clause) {
		this.check_clause = check_clause;
	}
}
