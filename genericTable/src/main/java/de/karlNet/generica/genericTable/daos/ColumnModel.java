package de.karlNet.generica.genericTable.daos;

import java.io.Serializable;

public class ColumnModel implements Serializable {

	@Override
	public String toString() {
		return "ColumnModel [header=" + header + ", property=" + property + "]";
	}

	private String header;
	private String property;

	public ColumnModel(String header, String property) {
		this.header = header;
		this.property = property;
	}

	public String getHeader() {
		return header;
	}

	public String getProperty() {
		return property;
	}

	public void setHeader(String header) {
		this.header = header;
	}

}
