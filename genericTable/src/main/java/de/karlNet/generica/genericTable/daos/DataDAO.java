package de.karlNet.generica.genericTable.daos;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.primefaces.model.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import de.karlNet.dbhandler.DBHandler;

@Controller
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class DataDAO {
	@Autowired
	private DBHandler dbHandler;
	@Autowired
	private SchemaDAO schemaDAO;

	public int countTableRows(String tableName, Map<String, String> filterMap)
			throws SQLException {
		String query = String.format("SELECT COUNT(*) FROM %s WHERE 1=1 ",
				tableName);
		query += this.createFilter(filterMap);
		PreparedStatement preparedStatement = this.dbHandler
				.prepareStatement(query);
		ResultSet resultSet = this.dbHandler.executeQuery(preparedStatement);
		resultSet.next();
		return resultSet.getInt(1);
	}

	private String createFilter(Map<String, String> filterMap) {
		String filter = "";
		if (filterMap != null) {
			for (String key : filterMap.keySet()) {
				filter += " AND " + key;
				String value = filterMap.get(key);
				if (value.endsWith("*")) {
					value = value.replace('*', '%');
					filter += " LIKE '" + value + "'";
				} else {
					filter += "='" + value + "'";
				}
			}
		}
		return filter;
	}

	public void create(Object objectToBeInserted, String tableName)
			throws SQLException, NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		ArrayList<String> columNames = new ArrayList<String>();
		Method[] methods = objectToBeInserted.getClass().getDeclaredMethods();
		for (Method method : methods) {
			if (method.getName().startsWith("get")) {
				String attributeName = StringUtils.uncapitalize(method
						.getName().replaceAll("get", ""));
				columNames.add(attributeName);
			}
		}
		String primaryKeyOfTable = this.schemaDAO
				.getPrimaryKeyOfTable(tableName);
		String query = "";
		if (this.schemaDAO.getPrimaryKeyValueOfObject(tableName,
				objectToBeInserted) == null) {
			columNames.remove(primaryKeyOfTable);
			query = "INSERT INTO " + tableName + "("
					+ StringUtils.join(columNames, ',') + ") VALUES (";
			query += StringUtils.repeat("?", ",", columNames.size());
			query += ")";
		} else {
			query = "UPDATE " + tableName + " SET ";
			query += StringUtils.join(columNames, "=?,");
			query += "=? WHERE " + primaryKeyOfTable + "=?";
		}
		PreparedStatement preparedStatement = this.dbHandler
				.prepareStatement(query);
		int i = 1;
		for (String columnName : columNames) {
			Method method = objectToBeInserted.getClass().getMethod(
					"get" + Character.toUpperCase(columnName.charAt(0))
							+ columnName.substring(1), null);
			Object value = method.invoke(objectToBeInserted, null);
			preparedStatement.setObject(i, value);
			i++;
		}
		if (this.schemaDAO.getPrimaryKeyValueOfObject(tableName,
				objectToBeInserted) != null) {
			preparedStatement.setObject(i, this.schemaDAO
					.getPrimaryKeyValueOfObject(tableName, objectToBeInserted));
		}
		this.dbHandler.executeUpdate(preparedStatement);
	}

	public void delete(String tableName, HashMap<String, String> filter)
			throws SQLException {
		String query = "DELETE FROM " + tableName + " WHERE 1=1 ";

		for (String attributeOfFilter : filter.keySet()) {
			query += " AND " + attributeOfFilter + "=?";
		}

		PreparedStatement preparedStatement = this.dbHandler
				.prepareStatement(query);
		int i = 1;
		for (String attributeOfFilter : filter.keySet()) {
			String value = filter.get(attributeOfFilter);
			preparedStatement.setObject(i, value);
			i++;
		}

		this.dbHandler.executeUpdate(preparedStatement);
	}

	public void delete(String tableName, Object objectToBeDeleted)
			throws SQLException {
		this.delete(tableName, new Object[] { objectToBeDeleted });
	}

	public void delete(String tableName, Object[] objectsToBeDeleted)
			throws SQLException {
		if (objectsToBeDeleted.length < 1) {
			return;
		}
		String query = "DELETE FROM " + tableName + " WHERE "
				+ this.schemaDAO.getPrimaryKeyOfTable(tableName) + " IN (";
		query += StringUtils.repeat("?", ",", objectsToBeDeleted.length);
		query += ")";
		PreparedStatement preparedStatement = this.dbHandler
				.prepareStatement(query);
		for (int i = 0; i < objectsToBeDeleted.length; i++) {
			preparedStatement.setObject(i + 1, objectsToBeDeleted[i]);
		}
		this.dbHandler.executeUpdate(preparedStatement);
	}

	public List<?> readOutViewOrTable(String tableName) throws Exception {
		Class<?> mappingClass = this.schemaDAO
				.getColumnWithResolvedMapping(tableName);
		return this.readOutViewOrTable(tableName, mappingClass, null,
				SortOrder.ASCENDING, 0, 1000, null);
	}

	public List<?> readOutViewOrTableWithoutMapping(String tableName,
			Class<?> mappingClass, HashMap<String, String> filter)
			throws Exception {
		return this.readOutViewOrTable(tableName, mappingClass, null,
				SortOrder.ASCENDING, 0, 1000, filter, false);
	}

	public List<?> readOutViewOrTableWithoutMapping(String tableName,
			Class<?> mappingClass) throws Exception {
		return this.readOutViewOrTable(tableName, mappingClass, null,
				SortOrder.ASCENDING, 0, 1000, null, false);
	}

	public List<?> readOutViewOrTable(String tableName, Class<?> mappingClass)
			throws Exception {
		return this.readOutViewOrTable(tableName, mappingClass, null,
				SortOrder.ASCENDING, 0, 1000, null);
	}

	public List<?> readOutViewOrTable(String tableName, String sortField,
			SortOrder sortOrder, int start, int pageSize,
			Map<String, String> filter) throws SQLException, Exception {
		Class<?> mappingClass = this.schemaDAO
				.getColumnWithResolvedMapping(tableName);
		return this.readOutViewOrTable(tableName, mappingClass, sortField,
				sortOrder, start, pageSize, filter);
	}

	private String getAttributeNamesFromMethod(Method[] methods) {
		ArrayList<String> toJoin = new ArrayList<String>();
		for (Method method : methods) {
			if (method.getName().startsWith("get")) {
				toJoin.add(StringUtils.uncapitalize(method.getName().replace(
						"get", "")));
			}
		}
		return StringUtils.join(toJoin, ",");
	}

	public List<?> readOutViewOrTable(String tableName, Class<?> mappingClass,
			String sortField, SortOrder sortOrder, int start, int pageSize,
			Map<String, String> filter) throws SQLException, Exception {
		return this.readOutViewOrTable(tableName, mappingClass, sortField,
				sortOrder, start, pageSize, filter, true);
	}

	private String getColumns(String tableName, Class<?> mappingClass,
			String sortField, SortOrder sortOrder, int start, int pageSize,
			Map<String, String> filter, boolean doMapping) throws SQLException,
			Exception {
		String primaryKeyOfTable = this.schemaDAO
				.getPrimaryKeyOfTable(tableName);
		// if so we have to join it
		String joinString = "";
		ArrayList<String> columnsToReadOut = new ArrayList<String>();
		for (Method method : mappingClass.getDeclaredMethods()) {
			if (method.getName().startsWith("get")) {
				// check if we do have a mapping table
				String attributeName = StringUtils.uncapitalize(method
						.getName().replace("get", ""));
				if (doMapping
						&& this.schemaDAO
								.existMapping(tableName, attributeName)) {
					String mappingName = this.schemaDAO.getMappingName(
							tableName, attributeName);
					columnsToReadOut.add(mappingName + ".label AS "
							+ attributeName);
					joinString += " INNER JOIN " + mappingName + " ON "
							+ mappingName + ".value = " + attributeName;
				} else {
					if (primaryKeyOfTable.toLowerCase().equals(
							attributeName.toLowerCase())) {
						columnsToReadOut.add(tableName + "." + attributeName
								+ " as id");
					} else {
						columnsToReadOut.add(attributeName);
					}
				}
			}
		}
		String query = String.format(
				"SELECT " + StringUtils.join(columnsToReadOut, ",")
						+ " FROM %s", tableName);

		query += joinString;
		query += " WHERE 1=1 AND " + tableName + "." + primaryKeyOfTable
				+ "!=?";
		query += this.createFilter(filter);
		if (sortField != null && sortField != "") {
			String ascDesc = sortOrder == SortOrder.ASCENDING ? " asc"
					: " desc";
			query += " ORDER BY " + sortField + ascDesc;
		}
		query += " LIMIT " + pageSize + " OFFSET " + (start) + " ";
		System.out.println(query);
		return query;
	}

	public List<?> readOutViewOrTable(String tableName, Class<?> mappingClass,
			String sortField, SortOrder sortOrder, int start, int pageSize,
			Map<String, String> filter, boolean doMapping) throws SQLException,
			Exception {
		String queryToBeExecuted = this.getColumns(tableName, mappingClass,
				sortField, sortOrder, start, pageSize, filter, doMapping);
		return this.readOutViewOrTable(mappingClass, sortField,
				sortOrder, start, pageSize, filter, queryToBeExecuted);
	}

	public List<?> readOutViewOrTable(Class<?> mappingClass,
			String sortField, SortOrder sortOrder, int start, int pageSize,
			Map<String, String> filter,
			String queryToBeExecuted) throws SQLException, Exception {
		List resultOfQuery = new ArrayList();

		String query = queryToBeExecuted;
		PreparedStatement preparedStatement = this.dbHandler
				.prepareStatement(query);
		preparedStatement.setString(1, "1");
		ResultSet resultSet = this.dbHandler.executeQuery(preparedStatement);
		ResultSetMetaData rsmd = resultSet.getMetaData();

		while (resultSet.next()) {
			Object o = mappingClass.newInstance();
			for (int i = 1; i <= rsmd.getColumnCount(); i++) {
				String columnName = rsmd.getColumnLabel(i);
				Object value = resultSet.getObject(i);
				Class<?> type = mappingClass.getMethod(
						"get" + Character.toUpperCase(columnName.charAt(0))
								+ columnName.substring(1), null)
						.getReturnType();

				Method method = mappingClass.getDeclaredMethod("set"
						+ Character.toUpperCase(columnName.charAt(0))
						+ columnName.substring(1), type);
				method.invoke(o, value);
			}
			resultOfQuery.add(o);
		}

		return resultOfQuery;
	}

	public Object getByPK(String tableName, Class<?> mappingClass, Object pk)
			throws SQLException, InstantiationException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {
		String query = String.format("SELECT * FROM %s WHERE 1=1 AND "
				+ this.schemaDAO.getPrimaryKeyOfTable(tableName) + "=?",
				tableName);
		PreparedStatement preparedStatement = this.dbHandler
				.prepareStatement(query);
		preparedStatement.setObject(1, pk);
		ResultSet resultSet = this.dbHandler.executeQuery(preparedStatement);
		ResultSetMetaData rsmd = resultSet.getMetaData();
		resultSet.next();
		Object o = mappingClass.newInstance();
		for (int i = 1; i <= rsmd.getColumnCount(); i++) {
			String columnName = rsmd.getColumnName(i);
			Object value = resultSet.getObject(i);
			Class<?> type = mappingClass.getMethod(
					"get" + StringUtils.capitalize(columnName), null)
					.getReturnType();

			Method method = mappingClass.getDeclaredMethod(
					"set" + Character.toUpperCase(columnName.charAt(0))
							+ columnName.substring(1), type);
			method.invoke(o, value);
		}
		return o;
	}

	public Object executeStoredProcedure(String storedProcedureName)
			throws SQLException {
		String query = "SELECT " + storedProcedureName;
		PreparedStatement preparedStatement = this.dbHandler
				.prepareStatement(query);
		ResultSet resultSet = this.dbHandler.executeQuery(query);
		Object ret = null;
		if (resultSet.next()) {
			ret = resultSet.getObject(1);
		}
		return ret;
	}

	public Object getByPK(String viewName, Object pk)
			throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException, SQLException {
		return this.getByPK(viewName, this.schemaDAO.getColumnClass(viewName),
				pk);
	}
}
