package de.karlNet.genericTable;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SelectableDataModel;
import org.primefaces.model.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Controller;

import de.karlNet.genericTable.daos.DataDAO;
import de.karlNet.genericTable.daos.SchemaDAO;

@Controller
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class LazyModel extends LazyDataModel<Object> implements
		SelectableDataModel<Object> {
	@Autowired
	private DataDAO dataDAO;
	@Autowired
	private TableBean tableBean;

	@Autowired
	private SchemaDAO schemaDAO;

	private String oldSortField;

	@Override
	public List load(int first, int pageSize, String sortField,
			SortOrder sortOrder, Map<String, String> filters) {
		try {
			if (sortField != null) {
				// workaround for primefaces bug
				if (sortField.endsWith("]")) {
					sortField = this.oldSortField;
				} else {
					this.oldSortField = sortField;
				}
			}
			String tableName = this.tableBean.getViewName();
			Class<?> mappingClass = this.schemaDAO.getColumnWithResolvedMapping(tableName);
			List data = this.dataDAO.readOutViewOrTable(tableName,
					mappingClass, sortField, sortOrder, first, pageSize,
					filters);
			this.setRowCount(this.dataDAO.countTableRows(tableName, filters));
			return data;
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Object getRowKey(Object objectToReadout) {
		try {
			return this.schemaDAO.getPrimaryKeyValue(
					this.tableBean.getViewName(), objectToReadout);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Object getRowData(String pk) {
		return pk;
	}
}
