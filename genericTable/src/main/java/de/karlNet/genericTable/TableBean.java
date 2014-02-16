package de.karlNet.genericTable;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlDataTable;
import javax.faces.context.FacesContext;

import org.primefaces.component.api.DynamicColumn;
import org.primefaces.event.data.SortEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Controller;

import de.karlNet.genericForm.GenericFormBeanDynamic;
import de.karlNet.genericTable.daos.ColumnModel;
import de.karlNet.genericTable.daos.DataDAO;
import de.karlNet.genericTable.daos.SchemaDAO;

@Controller
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class TableBean implements Serializable {
	private List objects;
	private List<ColumnModel> columns = new ArrayList<ColumnModel>();
	@Autowired
	private GenericFormBeanDynamic genericFormBeanDynamic;
	@Autowired
	private DataDAO dataDAO;

	private List filteredCars;

	@Autowired
	private LazyModel lazyModel;
	@Autowired
	private SchemaDAO schemaDAO;

	private Object[] selectedObjects;
	private Integer sortBy = null;

	private boolean sortDirection = false;

	private String viewName = "";
	private String oldViewName = "";

	public void setViewName(String viewName) {
		this.oldViewName = this.viewName;
		this.viewName = viewName;

	}

	public TableBean() throws Exception {

	}

	public void deleteSelectedObjects() throws SQLException {
		this.dataDAO.delete(this.viewName, this.selectedObjects);
	}

	private boolean checkViewName() throws Exception {

		if (!this.oldViewName.equals(this.viewName)) {

			this.columns.clear();
			this.columns.addAll(this.schemaDAO.getColumnModels(this.viewName));
			this.oldViewName = this.viewName;
			return true;
		}
		return false;
	}

	public LazyModel getObjects() throws SQLException, Exception {
		this.checkViewName();
		return this.lazyModel;
	}

	public List getActions() throws Exception {
		this.checkViewName();
		HashMap<String, String> filter = new HashMap<String, String>();
		filter.put("table_actions_table_name_rel_mapping.label", this.viewName);
		return this.dataDAO.readOutViewOrTable("table_actions", null, null, 0,
				1000, filter);
	}

	public List<ColumnModel> getColumns() throws Exception {
		if (this.checkViewName()) {
			this.columns.addAll(this.schemaDAO.getColumnModels(this.viewName));
		}
		return this.columns;
	}

	public List<?> getFilteredCars() {
		return filteredCars;
	}

	public Object[] getSelectedObjects() {
		return selectedObjects;
	}

	public String getViewName() {
		return viewName;
	}

	public void setFilteredCars(List filteredCars) {
		this.filteredCars = filteredCars;
	}

	public void setSelectedObjects(Object[] selectedObjects) {
		this.selectedObjects = selectedObjects;
	}

	public String navigateTo(String link) throws SecurityException, Exception {
		String[] split = link.split("\\?");
		if (split.length > 1) {
			String[] split2 = split[1].split("viewname=");
			if(split2.length > 1) {
				this.genericFormBeanDynamic.setViewName(split2[1]);
				this.genericFormBeanDynamic.checkViewName();
			}
		}
		
		return link;
	}
}
