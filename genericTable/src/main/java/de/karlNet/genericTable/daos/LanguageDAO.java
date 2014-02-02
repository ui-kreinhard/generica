package de.karlNet.genericTable.daos;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

import javax.faces.context.FacesContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.karlNet.genericTable.TableBean;

@Component
public class LanguageDAO {
	@Autowired
	private DataDAO dataDAO;
	@Autowired
	private TableBean tableBean;

	public String changeLanguage(String language_name)
			throws NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, SQLException {
		this.dataDAO.create(new Language(language_name), "current_language");
		String viewName = this.tableBean.getViewName();
		this.tableBean.setViewName("");
		this.tableBean.setViewName(viewName);
		return viewName;
	}

	private class Language {
		private String language_name;

		public String getLanguage_name() {
			return language_name;
		}

		public void setLanguage_name(String language_name) {
			this.language_name = language_name;
		}

		public Language(String language_name) {
			super();
			this.language_name = language_name;
		}
	}
}
