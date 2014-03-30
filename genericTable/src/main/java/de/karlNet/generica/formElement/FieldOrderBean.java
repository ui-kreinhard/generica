package de.karlNet.generica.formElement;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.faces.context.FacesContext;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Controller;

import de.karlNet.generica.genericTable.TableBean;
import de.karlNet.generica.genericTable.daos.DataDAO;
import de.karlNet.generica.genericTable.daos.SchemaDAO;

@Controller
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class FieldOrderBean {
	@Autowired
	private TableBean tableBean;
	
	@Autowired
	private DataDAO dataDAO;

	@Autowired
	private SchemaDAO schemaDAO;

	private List<FieldOrderMapping> fieldOrderMappings = new ArrayList<FieldOrderMapping>();
	private String selectedField;
	private String viewName = "";

	public FieldOrderBean() {

	}

	public void moveElementUp() {
		if (this.selectedField == null) {
			return;
		}
		int pos = -1;
		int i = 0;
		for (FieldOrderMapping fieldOrderMapping : this.fieldOrderMappings) {
			if (fieldOrderMapping.getColumn_name().equals(this.selectedField)) {
				pos = i;
				break;
			}
			i++;
		}

		if (pos > 0) {
			Collections.swap(this.fieldOrderMappings, pos, pos - 1);
		}
	}

	public void moveElementDown() {
		if (this.selectedField == null) {
			return;
		}
		int pos = -1;
		int i = 0;
		for (FieldOrderMapping fieldOrderMapping : this.fieldOrderMappings) {
			if (fieldOrderMapping.getColumn_name().equals(this.selectedField)) {
				pos = i;
				break;
			}
			i++;
		}

		if (pos < this.fieldOrderMappings.size() - 1) {
			Collections.swap(this.fieldOrderMappings, pos, pos + 1);
		}
	}

	public List<FieldOrderMapping> getFieldOrderMappings() throws Exception {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		String newViewName = (String) facesContext.getExternalContext()
				.getRequestParameterMap().get("viewname");
		if(StringUtils.isBlank(newViewName)) {
			Object[] selectedObjects = this.tableBean.getSelectedObjects();
			Object selectedObject = selectedObjects[0];
			newViewName = (String) selectedObject;
		}
		if (newViewName != null && newViewName != this.viewName) {
			this.fieldOrderMappings.clear();
			this.viewName = newViewName;
			if (this.schemaDAO.tableExists("formelements_order")) {
				HashMap<String, String> filter = new HashMap<String, String>();
				filter.put("table_name", viewName);
				List fieldOrderMappings = this.dataDAO
						.readOutViewOrTableWithoutMapping("formelements_order",
								FieldOrderMapping.class, filter);
				if (fieldOrderMappings.size() > 0) {
					this.fieldOrderMappings.addAll(fieldOrderMappings);
				} else {
					Class<?> clazz = this.schemaDAO
							.getColumnClass(this.viewName);
					for (Method properties : clazz.getDeclaredMethods()) {
						String attributeName = properties.getName();
						if (attributeName.startsWith("get")) {
							attributeName = StringUtils
									.uncapitalize(attributeName.replace("get",
											""));
							FieldOrderMapping newFieldOrderMapping = new FieldOrderMapping();
							newFieldOrderMapping.setColumn_name(attributeName);
							newFieldOrderMapping.setTable_name(this.viewName);
							this.fieldOrderMappings.add(newFieldOrderMapping);
						}
					}
				}
			}
		}
		return fieldOrderMappings;
	}

	public String submit() throws Exception {
		HashMap<String, String> filter=  new HashMap<String, String>();
		filter.put("table_name", this.viewName);
		this.dataDAO.delete("formelements_order", filter);
		for (FieldOrderMapping fieldOrderMapping : this.fieldOrderMappings) {
			System.out.println(fieldOrderMapping);
			fieldOrderMapping.setId(null);
			this.dataDAO.create(fieldOrderMapping, "formelements_order");
		}
		return "/dynamiccolumn.xhtml?viewname=customization_elementOrder";

	}

	public String getSelectedField() {
		return selectedField;
	}

	public void setSelectedField(String selectedField) {
		this.selectedField = selectedField;
	}
}
