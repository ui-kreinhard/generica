package de.karlNet.generica.actions;

import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Controller;

import de.karlNet.generica.genericTable.daos.DataDAO;
@Controller
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class StoredProcedureBean {
	@Autowired
	private DataDAO dataDAO;
	private String spName;

	public String getSpName() {
		return spName;
	}

	public void setSpName(String spName) {
		this.spName = spName;
	}

	public Object fireSP() throws SQLException {
		return this.dataDAO.executeStoredProcedure(this.spName + "()");
	}
}
